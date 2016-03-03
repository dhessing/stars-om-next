(ns stars.parsers.turn
  (:require [stars.reconciler :refer [read mutate parser]]
            [datascript.core :as d]))

(defmethod read :current-player
  [{:keys [state query]} _ _]
  {:value (first (d/q '[:find [(pull ?e ?selector)]
                        :in $ ?selector
                        :where [_ :turn/current-player ?e]]
                      (d/db state) query))})

(defmethod read :turn
  [{:keys [state query]} _ _]
  {:value (first (d/q '[:find [(pull ?e ?selector) ...]
                        :in $ ?selector
                        :where [?e :db/ident :turn]]
                      (d/db state) query))})

(defmethod mutate 'turn/roll
  [{:keys [state]} _ {:keys [chosen]}]
  (let [dice (- 8 (count chosen))
        roll (repeatedly dice #(rand-nth [1 2 3 4 5 :*]))
        dead? (empty? (clojure.set/difference (set roll) (set chosen)))]
    {:action (fn [] (d/transact! state [{:db/id      [:db/ident :turn]
                                         :turn/roll  (apply hash-map (flatten (map-indexed vector roll)))
                                         :turn/phase (if dead? :dead :pick)}]))}))

(defmethod mutate 'turn/pick
  [{:keys [state]} _ {:keys [:face :roll :chosen]}]
  (let [new-chosen (filter #(= (second %) face) roll)]
    {:action (fn [] (d/transact! state [{:db/id       [:db/ident :turn]
                                         :turn/roll   (apply dissoc roll (map first new-chosen))
                                         :turn/chosen (concat chosen (map second new-chosen))
                                         :turn/phase  :roll}]))}))

(defmethod mutate 'turn/lose-tile
  [{:keys [state]} _ {:keys [:player]}]
  (let [{:keys [:db/id :player/tiles]} player]
    {:action (fn [] (when (not-empty tiles)
                      (d/transact! state [{:db/id        id
                                           :player/tiles (rest tiles)}])))}))

(defmethod mutate 'turn/end
  [{:keys [state]} _ _]
  (let [query '[{:current-player [:db/id]}
                {:game/players [:db/id]}]
        {:keys [:current-player :game/players]} (parser {:state state} query)
        player-id (:db/id current-player)
        player-ids (map :db/id players)
        next-player (second (drop-while (partial not= player-id) (cycle player-ids)))]
    {:action (fn [] (d/transact! state [{:db/id               [:db/ident :turn]
                                         :turn/roll           []
                                         :turn/chosen         []
                                         :turn/phase          :roll
                                         :turn/current-player next-player}]))}))

(defmethod mutate 'turn/pick-tile
  [{:keys [state]} _ {:keys [tile]}]
  (let [query '[{:current-player [:db/id :player/tiles]}]
        {:keys [:db/id :player/tiles]} (:current-player (parser {:state state} query))
        tiles (conj (or tiles '()) tile)]
    {:action (fn [] (d/transact! state [{:db/id        id
                                         :player/tiles tiles}]))}))

(defmethod mutate 'turn/steal-tile
  [{:keys [state]} _ {:keys [player]}]
  (let [query '[{:current-player [:db/id :player/tiles]}]
        {:keys [:db/id :player/tiles]} (:current-player (parser {:state state} query))
        tiles (conj (or tiles '()) (first (:player/tiles player)))]
    {:action (fn [] (d/transact! state [(update player :player/tiles rest)
                                        {:db/id        id
                                         :player/tiles tiles}]))}))
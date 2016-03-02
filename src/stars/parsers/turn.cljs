(ns stars.parsers.turn
  (:require [stars.reconciler :refer [read mutate parser]]
            [datascript.core :as d]))

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
  [{:keys [state]} _ {:keys [current-id player-ids]}]
  {:action (fn [] (d/transact! state [{:db/id               [:db/ident :turn]
                                       :turn/roll           []
                                       :turn/chosen         []
                                       :turn/phase          :roll
                                       :turn/current-player (second (drop-while (partial not= current-id) (cycle player-ids)))}]))})
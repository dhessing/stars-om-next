(ns stars.parsers.game
  (:require [stars.reconciler :refer [read mutate parser]]
            [datascript.core :as d]
            [stars.constants :as c]))

(defmethod read :game/players
  [{:keys [state query]} _ _]
  {:value (d/q '[:find [(pull ?e ?selector) ...]
                 :in $ ?selector
                 :where [?e :type :game/player]]
               (d/db state) query)})

(defmethod read :scoring/players
  [{:keys [state query]} _ _]
  (let [players (d/q '[:find [(pull ?e ?selector) ...]
                       :in $ ?selector
                       :where [?e :type :game/player]]
                     (d/db state) query)]
    {:value (for [{:keys [:player/tiles] :as player} players
                  :let [score (apply + (map c/tiles tiles))]]
              (assoc player :player/score score))}))

(defmethod read :game/tiles-available
  [{:keys [state query]} _ _]
  {:value (let [picked (flatten (d/q '[:find [?t ...]
                                       :in $ ?selector
                                       :where [?e :type :game/player]
                                       [?e :player/tiles ?t]]
                                     (d/db state) query))]
            (clojure.set/difference
              (apply sorted-set (keys c/tiles))
              picked))})

(defmethod mutate 'game/start
  [{:keys [state]} _ {:keys [:names]}]
  (let [name->player (fn [name] {:db/id       (d/tempid :db.part/user)
                                 :type        :game/player
                                 :player/name name})
        players (map name->player names)]
    {:action (fn [] (d/transact! state [{:db/id     1
                                         :app/state players}
                                        {:db/ident            :turn
                                         :turn/chosen         []
                                         :turn/phase          :roll
                                         :turn/current-player (:db/id (first players))}]))}))

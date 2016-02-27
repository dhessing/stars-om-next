(ns stars.parser
  (:require [stars.reconciler :refer [read mutate parser]]
            [datascript.core :as d]
            [stars.constants :as c]))

(defmethod read :app/stars
  [{:keys [state query]} _ _]
  {:value (first (d/q '[:find [(pull ?e ?selector) ...]
                        :in $ ?selector
                        :where [?e :type :app]]
                   (d/db state) query))})

(defmethod read :screen/props
  [{:keys [state query]} _ _]
  {:value (parser {:state state} query)})

(defmethod read :game/players
  [{:keys [state query]} _ _]
  {:value (d/q '[:find [(pull ?e ?selector) ...]
                 :in $ ?selector
                 :where [?e :type :game/player]]
            (d/db state) query)})

(defmethod read :setup/players
  [{:keys [state query]} _ _]
  {:value (d/q '[:find [(pull ?e ?selector) ...]
                 :in $ ?selector
                 :where
                 [?e :type :setup/player]]
            (d/db state) query)})

(defmethod read :tiles/available
  [{:keys [state query]} _ _]
  {:value (let [picked (d/q '[:find [?t ...]
                              :in $ ?selector
                              :where [?e :type :game/player]
                              [?e :player/tiles ?t]]
                         (d/db state) query)]
            (clojure.set/difference
              (apply sorted-set (keys c/tiles))
              picked))})

(defmethod read :dice/roll
  [{:keys [state query]} _ _]
  {:value (first (d/q '[:find [?d]
                  :in $ ?selector
                  :where
                  [1 :app/game ?g]
                  [?g :game/roll ?d]]
             (d/db state) query))})

(defmethod read :dice/chosen
  [{:keys [state query]} _ _]
  {:value (first (d/q '[:find [?d]
                        :in $ ?selector
                        :where
                        [1 :app/game ?g]
                        [?g :game/chosen ?d]]
                   (d/db state) query))})

(defmethod mutate 'app/screen
  [{:keys [state]} _ {:keys [:screen]}]
  {:action (fn [] (d/transact! state [{:db/id 1 :app/screen screen}]))})

(defmethod mutate 'setup/add-player
  [{:keys [state]} _ _]
  {:action (fn [] (d/transact! state [{:type :setup/player :player/name ""}]))})

(defmethod mutate 'setup/edit-player
  [{:keys [state]} _ entity]
  {:action (fn [] (d/transact! state [entity]))})

(defmethod mutate 'setup/remove-player
  [{:keys [state]} _ {:keys [:id]}]
  {:action (fn [] (d/transact! state [[:db.fn/retractEntity id]]))})

(defmethod mutate 'turn/roll
  [{:keys [state]} _ _]
  {:action (fn [] (d/transact! state [{:db/id    1
                                       :app/game {:game/roll (repeatedly 8 #(rand-nth [1 2 3 4 5 :*]))}}]))})

(defmethod mutate 'game/start
  [{:keys [state]} _ {:keys [:names]}]
  (let [name->player (fn [name] {:type         :game/player
                                 :player/name  name
                                 :player/tiles '()})
        players (map name->player names)]
    {:action (fn [] (d/transact! state [[:db.fn/retractAttribute 1 :app/game]
                                        {:db/id    1
                                         :app/game {:type         :game
                                                    :game/players players}}]))}))


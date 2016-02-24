(ns stars.parser
  (:require [stars.reconciler :refer [read mutate parser]]
            [datascript.core :as d]))

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
                 :where [?app :app/game ?e]
                 [?e :type :game/player]]
            (d/db state) query)})

(defmethod read :setup/players
  [{:keys [state query]} _ _]
  {:value (d/q '[:find [(pull ?e ?selector) ...]
                 :in $ ?selector
                 :where [?e :type :setup/player]]
            (d/db state) query)})

(defmethod read :tiles/available
  [{:keys [state query]} _ _]
  {:value (d/q '[:find [(pull ?e ?selector) ...]
                 :in $ ?selector
                 :where [?e :type :tile]]
            (d/db state) query)})


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

(defmethod mutate 'game/start
  [{:keys [state]} _ {:keys [:names]}]
  {:action (fn []
             (let [players (for [name names]
                             {:type        :game/player
                              :player/name name})]
               (d/transact! state [[:db.fn/retractAttribute 1 :app/game]
                                   {:db/id 1 :app/game players}])))})


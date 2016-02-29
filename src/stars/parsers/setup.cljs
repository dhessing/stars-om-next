(ns stars.parsers.setup
  (:require [stars.reconciler :refer [read mutate parser]]
            [datascript.core :as d]))


(defmethod read :setup/players
  [{:keys [state query]} _ _]
  {:value (d/q '[:find [(pull ?e ?selector) ...]
                 :in $ ?selector
                 :where
                 [?e :type :setup/player]]
            (d/db state) query)})

(defmethod mutate 'setup/add-player
  [{:keys [state]} _ _]
  {:action (fn [] (d/transact! state [{:type :setup/player :player/name ""}]))})

(defmethod mutate 'setup/edit-player
  [{:keys [state]} _ entity]
  {:action (fn [] (d/transact! state [entity]))})

(defmethod mutate 'setup/remove-player
  [{:keys [state]} _ {:keys [:id]}]
  {:action (fn [] (d/transact! state [[:db.fn/retractEntity id]]))})
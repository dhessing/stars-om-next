(ns stars.parser
  (:require [stars.reconciler :refer [read mutate]]
            [datascript.core :as d]))

(defmethod read :app/stars
  [{:keys [state query]} _ _]
  {:value (first (d/q '[:find [(pull ?e ?selector) ...]
                        :in $ ?selector
                        :where [?e :app/screen]]
                   (d/db state) query))})

(defmethod read :players
  [{:keys [state query]} _ _]
  {:value (d/q '[:find [(pull ?e ?selector) ...]
                 :in $ ?selector
                 :where [?e :player/name]]
            (d/db state) query)})

(defmethod read :app/screen-data
  [{:keys [state query]} _ _]
  {:value (into {} (for [subquery query
                         [k v] subquery]
                     [k (:value (read {:state state :query v} k))]))})

(defmethod mutate 'app/add-player
  [{:keys [state]} _ _]
  {:action (fn [] (d/transact! state [{:player/name ""}]))})

(defmethod mutate 'app/screen
  [{:keys [state]} _ {:keys [:screen]}]
  {:action (fn [] (d/transact! state [{:db/id 1 :app/screen screen}]))})

(defmethod mutate 'entity/edit
  [{:keys [state]} _ entity]
  {:action (fn [] (d/transact! state [entity]))})

(defmethod mutate 'entity/remove
  [{:keys [state]} _ {:keys [:id]}]
  {:action (fn [] (d/transact! state [[:db.fn/retractEntity id]]))})


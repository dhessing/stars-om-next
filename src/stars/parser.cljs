(ns stars.parser
  (:require [stars.reconciler :refer [read mutate parser]]
            [datascript.core :as d]
            [stars.parsers.setup]
            [stars.parsers.game]
            [stars.parsers.turn]))

(defmethod read :app/stars
  [{:keys [state query]} _ _]
  {:value (first (d/q '[:find [(pull ?e ?selector) ...]
                        :in $ ?selector
                        :where [?e :db/ident :app]]
                   (d/db state) query))})

(defmethod read :app/nested-query
  [{:keys [state query]} _ _]
  {:value (parser {:state state} query)})

(defmethod mutate 'app/screen
  [{:keys [state]} _ {:keys [:screen]}]
  {:action (fn [] (d/transact! state [{:db/id [:db/ident :app]
                                       :app/screen screen}]))})




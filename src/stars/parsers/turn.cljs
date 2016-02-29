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
  [{:keys [state]} _ _]
  {:action (fn [] (d/transact! state [{:db/id     [:db/ident :turn]
                                       :turn/roll (repeatedly 8 #(rand-nth [1 2 3 4 5 :*]))}]))})
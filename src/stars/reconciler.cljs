(ns stars.reconciler
  (:require [om.next :as om]
            [stars.state :refer [conn]]))

(defmulti read om/dispatch)

(defmulti mutate om/dispatch)

(def reconciler
  (om/reconciler
    {:state  conn
     :parser (om/parser {:read read :mutate mutate})}))
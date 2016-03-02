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
  [{:keys [state]} _ {:keys [dice]}]
  {:action (fn [] (d/transact! state [{:db/id     [:db/ident :turn]
                                       :turn/roll (apply hash-map (flatten (map-indexed vector (repeatedly 8 #(rand-nth [1 2 3 4 5 :*])))))}]))})

(defmethod mutate 'turn/pick
  [{:keys [state]} _ {:keys [:face :roll :chosen]}]
  (let [new-chosen (filter #(= (second %) face) roll)]
    {:action (fn [] (d/transact! state [{:db/id       [:db/ident :turn]
                                         :turn/roll   (apply dissoc roll (map first new-chosen))
                                         :turn/chosen (concat chosen (map second new-chosen))}]))}))
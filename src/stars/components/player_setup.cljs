(ns stars.components.player-setup
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :as html :refer-macros [html]]
            [stars.components.buttons :refer [delete-button]]))

(defui PlayerSetup
  static om/IQuery
  (query [this]
    [:db/id :player/name])
  Object
  (edit [this name]
    (let [{:keys [:db/id]} (om/props this)
          {:keys [:edit-fn]} (om/get-computed (om/props this))]
      (edit-fn {:db/id id :player/name name})))

  (remove [this]
    (let [{:keys [:db/id]} (om/props this)
          {:keys [:remove-fn]} (om/get-computed (om/props this))]
      (some-> remove-fn (apply {:id id}))))

  (render [this]
    (let [{:keys [:player/name]} (om/props this)
          {:keys [:edit-fn :active?]} (om/get-computed this)]
      (html
        [:div.form-group.row
         [:label.form-control-label.col-xs-1 "Player "]
         [:div.col-xs-8
          [:input.form-control
           {:type        "text"
            :placeholder "Name"
            :value       name
            :on-change   (when edit-fn #(.edit this (.. % -target -value)))}]]
         (delete-button #(.remove this %) active?)]))))

(def player-setup (om/factory PlayerSetup {:keyfn :db/id}))
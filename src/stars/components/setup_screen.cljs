(ns stars.components.setup-screen
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :as html :refer-macros [html]]))

(defui SetupItem
  static om/Ident
  (ident [this props]
    {:id (:db/id props)})
  static om/IQuery
  (query [this]
    [:db/id :player/name])
  Object
  (edit [this name]
    (let [{:keys [:id]} (om/get-ident this)
          {:keys [:edit-fn]} (om/get-computed (om/props this))]
      (edit-fn {:db/id id :player/name name})))

  (remove [this]
    (let [{:keys [:remove-fn]} (om/get-computed (om/props this))]
      (remove-fn (om/get-ident this))))

  (render [this]
    (let [{:keys [:player/name]} (om/props this)]
      (html
        [:div.form-group.row
         [:label.form-control-label.col-xs-1 "Player "]
         [:div.col-xs-8
          [:input.form-control
           {:type        "text"
            :placeholder "Name"
            :value       name
            :on-change   #(.edit this (.. % -target -value))}]]
         [:a.btn.btn-danger.col-xs-1
          {:on-click #(.remove this)}
          [:i.fa.fa-trash-o.fa-form]]]))))

(def setup-item (om/factory SetupItem))

(defui SetupScreen
  static om/IQuery
  (query [this]
    [{:players (om/get-query SetupItem)}])
  Object
  (add-player [this]
    (om/transact! this `[(app/add-player {:id 1})]))
  (render [this]
    (let [players (om/props this)
          {:keys [:remove-fn :add-fn :edit-fn :done-fn]} (om/get-computed (om/props this))]
      (html
        [:div
         [:h1 "Players"]
         [:form
          (for [player players]
            (setup-item (om/computed player
                          {:remove-fn remove-fn
                           :edit-fn   edit-fn})))]
         [:div.btn-toolbar
          [:button.btn.btn-secondary
           {:on-click add-fn}
           "Add Player"]
          [:button.btn.btn-primary
           {:on-click done-fn}
           "Done"]]]))))

(def setup-screen (om/factory SetupScreen))

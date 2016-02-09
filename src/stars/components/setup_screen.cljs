(ns stars.components.setup-screen
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :as html :refer-macros [html]]))

(defui SetupItem
  static om/Ident
  (ident [this props]
    [:id (:db/id props)])
  static om/IQuery
  (query [this]
    [:db/id :player/name])
  Object
  (edit [this name]
    (let [[_ id] (om/get-ident this)
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
  (query [_]
    [{:players (om/get-query SetupItem)}])
  Object
  (remove-player [this id]
    (om/transact! this `[(entity/remove ~id)]))

  (add-player [this]
    (om/transact! this `[(app/add-player)]))

  (edit-player [this entity]
    (om/transact! this `[(entity/edit ~entity)]))

  (done [this]
    (let [{:keys [:switch-fn]} (om/get-computed (om/props this))]
      (switch-fn :game)))

  (render [this]
    (let [{:keys [:players]} (om/props this)]
      (html
        [:div
         [:h1 "Players"]
         [:form
          (for [player players]
            (setup-item (om/computed player
                          {:remove-fn #(.remove-player this %)
                           :edit-fn   #(.edit-player this %)})))]
         [:div.btn-toolbar
          [:button.btn.btn-secondary
           {:on-click #(.add-player this)}
           "Add Player"]
          [:button.btn.btn-primary
           {:on-click #(.done this)}
           "Done"]]]))))

(def setup-screen (om/factory SetupScreen))

(ns stars.components.setup-screen
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :as html :refer-macros [html]]))

(defui SetupItem
  static om/IQuery
  (query [this]
    [:db/id :type :player/name])
  Object
  (edit [this name]
    (let [{:keys [:db/id]} (om/props this)
          {:keys [:edit-fn]} (om/get-computed (om/props this))]
      (edit-fn {:db/id id :player/name name})))

  (remove [this]
    (let [{:keys [:db/id]} (om/props this)
          {:keys [:remove-fn]} (om/get-computed (om/props this))]
      (remove-fn {:id id})))

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

(defn game-player [i {:keys [:player/name]}]
  (let [name (if (empty? name) (str "Player " (inc i)) name)]
    {:type         :game/player
     :player/name  name}))

(defui SetupScreen
  static om/IQuery
  (query [_]
    [{:setup/players (om/get-query SetupItem)}])
  Object
  (remove-player [this id]
    (om/transact! this `[(setup/remove-player ~id)]))

  (add-player [this]
    (om/transact! this `[(setup/add-player)]))

  (edit-player [this entity]
    (om/transact! this `[(setup/edit-player ~entity)]))

  (done [this]
    (let [{:keys [:switch-fn]} (om/get-computed (om/props this))
          {:keys [:setup/players]} (om/props this)
          players (map-indexed game-player players)]
      (om/transact! this `[(game/start {:players ~players})])
      (switch-fn :game)))

  (render [this]
    (let [{:keys [:setup/players]} (om/props this)]
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

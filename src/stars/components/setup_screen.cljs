(ns stars.components.setup-screen
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :as html :refer-macros [html]]))


(defn sanitize-names [names]
  (letfn [(sanitize [i name]
            (if (empty? name) (str "Player " (inc i)) name))]
    (map-indexed sanitize names)))


(defui SetupScreen
  static om/IQuery
  (query [this]
    [{:setup/players [:db/id :player/name]}])

  Object
  (remove-player [this id]
    (om/transact! this `[(setup/remove-player {:id ~id})]))

  (add-player [this]
    (om/transact! this `[(setup/add-player)]))

  (edit-player [this id name]
    (om/transact! this `[(setup/edit-player {:db/id ~id :player/name ~name})]))

  (done [this]
    (let [{:keys [:switch-fn]} (om/get-computed (om/props this))
          names (sanitize-names (map :player/name (:setup/players (om/props this))))]
      (om/transact! this `[(game/start {:names ~names})])
      (switch-fn :game)))

  (render [this]
    (let [{:keys [:setup/players]} (om/props this)]
      (html
        [:div.row
         [:div.col-xs-offset-2.col-xs-8
          [:h1 "Players"]
          (for [{:keys [:db/id :player/name]} players]
            [:div.form-group.row {:key id}
             [:label.form-control-label.col-xs-1 "Player "]
             [:div.col-xs-10
              [:input.form-control
               {:type        "text"
                :placeholder "Name"
                :value       name
                :on-change   #(.edit-player this id (.. % -target -value))}]]
             [:div.col-xs-1
              [:button.btn.btn-danger
               (if (< 2 (count players))
                 {:on-click #(.remove-player this id)}
                 {:disabled true
                  :class    :disabled})
               [:i.fa.fa-trash-o.fa-form]]]])
          [:div.btn-toolbar
           [:button.btn.btn-secondary
            {:on-click #(.add-player this)}
            "Add Player"]
           [:button.btn.btn-primary
            {:on-click #(.done this)}
            "Done"]]]]))))

(def setup-screen (om/factory SetupScreen))
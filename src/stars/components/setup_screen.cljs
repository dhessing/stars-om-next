(ns stars.components.setup-screen
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :as html :refer-macros [html]]
            [stars.components.player-setup :refer [PlayerSetup player-setup]]))


(defn sanitize-names [names]
  (letfn [(sanitize [i name]
            (if (empty? name) (str "Player " (inc i)) name))]
    (map-indexed sanitize names)))


(defui SetupScreen
  static om/IQuery
  (query [this]
    [{:setup/players (om/get-query PlayerSetup)}])

  Object
  (remove-player [this id]
    (om/transact! this `[(setup/remove-player ~id)]))

  (add-player [this]
    (om/transact! this `[(setup/add-player)]))

  (edit-player [this entity]
    (om/transact! this `[(setup/edit-player ~entity)]))

  (done [this]
    (let [{:keys [:switch-fn]} (om/get-computed (om/props this))
          names (sanitize-names (map :player/name (:setup/players (om/props this))))]
      (om/transact! this `[(game/start {:names ~names})])
      (switch-fn :game)))

  (render [this]
    (let [{:keys [:setup/players]} (om/props this)]
      (html
        [:div
         [:h1 "Players"]
         [:form
          (for [player players]
            (player-setup (om/computed player
                            {:active?   (< 2 (count players))
                             :remove-fn #(.remove-player this %)
                             :edit-fn   #(.edit-player this %)})))]
         [:div.btn-toolbar
          [:button.btn.btn-secondary
           {:on-click #(.add-player this)}
           "Add Player"]
          [:button.btn.btn-primary
           {:on-click #(.done this)}
           "Done"]]]))))

(def setup-screen (om/factory SetupScreen))
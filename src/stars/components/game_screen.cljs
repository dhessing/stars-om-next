(ns stars.components.game-screen
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :as html :refer-macros [html]]
            [stars.constants :as c])
  (:require-macros [devcards.core :refer [defcard]]))


(defn tile-button [value]
  (html
    [:button.btn.btn-secondary.tile {:key value :disabled true}
     [:div.tile-top value]
     [:div.tile-bottom
      (for [i (range (c/tiles value))]
        [:i.fa.fa-star {:key i}])]]))

(defn die-button [props face]
  [:button.btn.btn-secondary.die props
   (if (= face :*) [:i.fa.fa-star] face)])

(defn sum-faces [faces]
  (apply + (replace {:* 5} faces)))


(defui GameScreen
  static om/IQuery
  (query [this]
    [{:game/players [:db/id :player/name :player/tiles]}
     {:turn [:turn/roll :turn/chosen]}
     :game/tiles-available])

  Object
  (done [this]
    (let [{:keys [:switch-fn]} (om/get-computed (om/props this))]
      (switch-fn :setup)))

  (roll [this]
    (let [chosen (get-in (om/props this) [:turn :turn/chosen])]
      (om/transact! this `[(turn/roll {:dice ~(- 8 (count chosen))})])))

  (pick [this face]
    (let [{:keys [:turn/roll :turn/chosen]} (:turn (om/props this))]
      (om/transact! this `[(turn/pick {:face ~face :roll ~roll :chosen ~chosen})])))

  (render [this]
    (let [{:keys [:game/players :turn :game/tiles-available]} (om/props this)
          {:keys [:turn/roll :turn/chosen]} turn]
      (html
        [:div
         [:div.card-deck-wrapper
          [:div.card-deck.m-b-1
           (for [{:keys [:db/id :player/name :player/tiles]} players]
             [:div.card {:key id}
              [:div.card-header name]
              [:div.card-block
               (if (seq? tiles)
                 (tile-button (first tiles))
                 [:button.btn.btn-secondary.tile.tile-nothing {:disabled true}])]])]]
         [:div.card
          [:div.card-block
           [:div.btn-toolbar
            (for [tile tiles-available]
              (tile-button tile))]]]
         [:div.card
          [:div.card-block
           [:div.btn-toolbar.m-b-1
            (for [[i face] roll]
              (die-button {:key           i
                           :class         (when (= (om/get-state this) face) "active")
                           :on-click      #(do (.pick this face)
                                               (om/set-state! this :nil))
                           :on-mouse-over #(om/set-state! this face)
                           :on-mouse-out  #(om/set-state! this :nil)}
                face))
            [:button.btn.btn-primary.stars-btn {:on-click #(.roll this)} "Roll"]]
           [:div.btn-toolbar.m-b-1
            (for [[i face] (map-indexed vector chosen)]
              (die-button {:key i :disabled true} face))]
           (when (not-empty chosen)
             [:p (str "Total: " (sum-faces chosen))])]]]))))

(def game-screen (om/factory GameScreen))
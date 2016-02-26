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

(defn die-button [face]
  [:button.btn.btn-secondary.die
   (if (= face "*") [:i.fa.fa-star] face)])

(defn roll-button []
  [:button.btn.btn-primary.stars-btn "Roll"])


(defui GameScreen
  static om/IQuery
  (query [this]
    [{:game/players [:db/id :player/name :player/tiles]}
     :tiles/available])

  Object
  (done [this]
    (let [{:keys [:switch-fn]} (om/get-computed (om/props this))]
      (switch-fn :setup)))

  (render [this]
    (let [{:keys [:game/players :tiles/available]} (om/props this)]
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
            (for [tile available]
              (tile-button tile))]]]
         [:div.card
          [:div.card-block
           [:div.btn-toolbar.m-b-1
            (die-button 5)
            (die-button 5)
            (die-button 5)
            (die-button 5)
            (die-button 5)
            (die-button 5)
            (roll-button)]
           [:div.btn-toolbar.m-b-1
            (die-button "*")
            (die-button "*")
            (die-button "*")]
           [:p "Total: 15"]]]]))))

(def game-screen (om/factory GameScreen))
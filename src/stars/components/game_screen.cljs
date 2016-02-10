(ns stars.components.game-screen
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :as html :refer-macros [html]])
  (:require-macros
    [devcards.core :refer [defcard]]))

(def tile->points
  {21 1
   22 1
   23 1
   24 1
   25 2
   26 2
   27 2
   28 2
   29 3
   30 3
   31 3
   32 3
   33 4
   34 4
   35 4
   36 4})

(defui Tile
  Object
  (render [this]
    (if-let [value (om/props this)]
      (let [points (tile->points value)]
        (html
          [:button.btn.btn-secondary.tile {:disabled true}
           [:div.tile-top value]
           [:div.tile-bottom
            (for [i (range points)]
              [:i.fa.fa-star {:key i}])]]))
      (html [:button.btn.btn-secondary.tile.tile-nothing {:disabled true}]))))

(def tile (om/factory Tile {:keyfn identity}))

(defui PlayerCard
  static om/IQuery
  (query [this]
    [:player/name :player/tiles])
  Object
  (render [this]
    (let [{:keys [:player/name :player/tiles]} (om/props this)]
      (html
        [:div.card
         [:div.card-header name]
         [:div.card-block
          (tile (:value (first tiles)))]]))))

(def player-card (om/factory PlayerCard))

(defui GameScreen
  static om/IQuery
  (query [this]
    [{:game/players (om/get-query PlayerCard)}
     {:tiles/available []}])
  Object
  (done [this]
    (let [{:keys [:switch-fn]} (om/get-computed (om/props this))]
      (switch-fn :setup)))

  (render [this]
    (let [{:keys [:game/players]} (om/props this)]
      (html
        [:div
         [:div.card-deck-wrapper
          [:div.card-deck.m-b-1
           (for [player players]
             (player-card player))]]
         [:div.card
          [:div.card-block
           [:div.btn-toolbar
            (for [value (range 21 37)]
              (tile value))]]]
         [:div.card
          [:div.card-block]]]))))

(def game-screen (om/factory GameScreen))

(defcard tiles
  "List of all the tiles"
  (html [:div.btn-toolbar (map (partial tile) (sort (keys tile->points)))]))

(defcard nil-tile
  (tile nil))
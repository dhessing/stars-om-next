(ns stars.components.score-screen
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :as html :refer-macros [html]]
            [stars.components.game-screen :refer [tile-button]]))

(defui ScoreScreen
  static om/IQuery
  (query [this]
    [{:scoring/players [:db/id :player/name :player/tiles :player/score]}])
  Object
  (render [this]
    (let [{:keys [:scoring/players]} (om/props this)]
      (html
        [:div
         [:h1 "Scoring"]
         [:div.card-deck-wrapper.m-t.m-b
          [:div.card-deck
           (let [winning-score (apply max (map :player/score players))]
             (for [{:keys [:db/id :player/name :player/tiles :player/score]} players]
               [:div.card
                {:key id
                 :class (when (= score winning-score) "card-inverse card-success")}
                [:div.card-block
                 [:h3.card-title
                  (when (= winning-score score)
                    [:span
                     [:i.fa.fa-star.text-warning.fa-spin]
                     " "])
                  name]
                 [:p.card-text score " points"]
                 [:div.btn-toolbar
                  (for [tile tiles]
                    (tile-button tile {:disabled true}))]]]))]]]))))

(def score-screen (om/factory ScoreScreen))


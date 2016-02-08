(ns stars.components.game-screen
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :as html :refer-macros [html]]))

(defui GameScreen
  Object
  (render [this]
    (let [{:keys [:done-fn]} (om/get-computed this)]
      (html [:div
             [:h1 "Game Screen"]
             [:a.btn.btn-primary {:on-click done-fn} "Done"]]))))

(def game-screen (om/factory GameScreen))
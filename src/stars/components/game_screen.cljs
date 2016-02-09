(ns stars.components.game-screen
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :as html :refer-macros [html]]))

(defui GameScreen
  Object
  (done [this]
    (let [{:keys [:switch-fn]} (om/get-computed (om/props this))]
      (switch-fn :setup)))
  (render [this]
    (html [:div
           [:h1 "Game Screen"]
           [:a.btn.btn-primary {:on-click #(.done this)} "Done"]])))

(def game-screen (om/factory GameScreen))
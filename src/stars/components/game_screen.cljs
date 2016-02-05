(ns stars.components.game-screen
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :as html :refer-macros [html]]))

(defui GameScreen
  Object
  (render [this]
    (html [:h1 "Game Screen"])))

(def game-screen (om/factory GameScreen))
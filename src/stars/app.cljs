(ns stars.app
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [sablono.core :as html :refer-macros [html]]
            [stars.reconciler :as reconciler :refer [reconciler]]
            [stars.parser]
            [stars.components.setup-screen :refer [SetupScreen setup-screen]]
            [stars.components.game-screen :refer [GameScreen game-screen]]
            [stars.components.score-screen :refer [ScoreScreen score-screen]]))

(enable-console-print!)

(def screen->component
  {:setup SetupScreen
   :game  GameScreen
   :score ScoreScreen})

(def screen->factory
  {:setup setup-screen
   :game  game-screen
   :score score-screen})

(def app-query [{:app/stars [:app/screen]}])

(defui StarsApp
  static om/IQuery
  (query [this] app-query)
  Object
  (componentWillMount [this]
    (let [{:keys [:app/screen]} (:app/stars (om/props this))]
      (.switch-screen this screen)))

  (switch-screen [this screen]
    (let [screen-query (or (om/get-query (screen->component screen)) [])]
      (om/set-query! this {:query (into app-query
                                       [{:app/nested-query screen-query}])})
      (om/transact! this `[(app/screen {:screen ~screen})])))

  (render [this]
    (let [{:keys [:app/screen]} (:app/stars (om/props this))
          screen-factory (screen->factory screen)
          props (om/computed (:app/nested-query (om/props this))
                  {:switch-fn #(.switch-screen this %)})]
      (screen-factory props))))

(om/add-root! reconciler
  StarsApp (gdom/getElement "app"))
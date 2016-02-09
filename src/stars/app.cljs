(ns stars.app
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [sablono.core :as html :refer-macros [html]]
            [stars.reconciler :refer [reconciler]]
            [stars.parser]
            [stars.components.setup-screen :refer [SetupScreen setup-screen]]
            [stars.components.game-screen :refer [GameScreen game-screen]]))

(enable-console-print!)

(def screen->component
  {:setup SetupScreen
   :game  GameScreen})

(def screen->factory
  {:setup setup-screen
   :game  game-screen})

(defui StarsApp
  static om/Ident
  (ident [this props]
    {:id (get-in props [:app/stars :db/id])})
  static om/IQuery
  (query [this]
    [{:app/stars [:db/id :app/screen]}
     {:app/screen-data (om/get-query (screen->component :setup))}])
  Object
  (switch-screen [this screen]
    (let [subquery (or (om/get-query (screen->component screen)) [])
          app-id (:id (om/get-ident this))]
      (om/set-query! this
        {:query [{:app/stars [:db/id :app/screen]}
                 {:app/screen-data subquery}]})
      (om/transact! this `[(entity/edit {:db/id ~app-id :app/screen ~screen})])))

  (render [this]
    (let [{:keys [:app/screen]} (:app/stars (om/props this))
          {:keys [:app/screen-data]} (om/props this)]
      (let [screen-factory (screen->factory screen)
            props (om/computed (or screen-data {})
                    {:switch-fn #(.switch-screen this %)})]
        (screen-factory props)))))

(om/add-root! reconciler
  StarsApp (gdom/getElement "app"))
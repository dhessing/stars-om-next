(ns stars.app
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [sablono.core :as html :refer-macros [html]]
            [stars.reconciler :refer [reconciler]]
            [stars.parser]
            [stars.components.setup-screen :refer [SetupScreen setup-screen]]
            [stars.components.game-screen :refer [GameScreen game-screen]]))

(enable-console-print!)

(def screen->comp
  {:setup SetupScreen
   :game GameScreen})

(defui StarsApp
  static om/Ident
  (ident [this props]
    {:id (get-in props [:app/stars :db/id])})
  static om/IQuery
  (query [this]
    [{:app/stars [:db/id :app/screen]}])
  Object
  (componentWillMount [this]
    (let [{:keys [:app/screen]} (:app/stars (om/props this))]
      (.set-query this screen)))

  (set-query [this screen]
    (let [comp (screen->comp screen)]
      (om/set-query! this
        {:query (vec (concat
                       [{:app/stars [:db/id :app/screen]}]
                       (om/get-query comp)))})))

  (remove-player [this id]
    (om/transact! this `[(entity/remove ~id)]))

  (add-player [this]
    (om/transact! this `[(app/add-player ~(om/get-ident this))]))

  (edit-player [this entity]
    (om/transact! this `[(entity/edit ~entity)]))

  (set-screen [this screen]
    (om/transact! this `[(app/screen {:component ~this :screen ~screen})]))

  (render [this]
    (let [{:keys [:app/screen]} (:app/stars (om/props this))]
      (case screen
        :setup (setup-screen
                 (om/computed (-> this om/props :players)
                   {:add-fn    #(.add-player this)
                    :remove-fn #(.remove-player this %)
                    :edit-fn   #(.edit-player this %)
                    :done-fn   #(.set-screen this :game)}))
        :game (game-screen
                (om/computed {}
                  {:done-fn #(.set-screen this :setup)}))))))

(om/add-root! reconciler
  StarsApp (gdom/getElement "app"))
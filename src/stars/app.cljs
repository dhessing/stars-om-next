(ns stars.app
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [sablono.core :as html :refer-macros [html]]
            [stars.reconciler :refer [reconciler]]
            [stars.parser]
            [stars.components.setup-screen :refer [SetupItem setup-screen]]
            [stars.components.game-screen :refer [game-screen]]))

(enable-console-print!)

(defui StarsApp
  static om/Ident
  (ident [this props]
    {:id (get-in props [:app/stars :db/id])})
  static om/IQuery
  (query [this]
    [{:app/stars [:db/id :app/screen]}
     {:players (om/get-query SetupItem)}])
  Object
  (remove-player [this id]
    (om/transact! this `[(entity/remove ~id)]))

  (add-player [this]
    (om/transact! this `[(app/add-player ~(om/get-ident this))]))

  (edit-player [this entity]
    (om/transact! this `[(entity/edit ~entity)]))

  (render [this]
    (let [{:keys [:app/screen]} (:app/stars (om/props this))]
      (case screen
        :setup (setup-screen
                 (om/computed (-> this om/props :players)
                   {:add-fn    #(.add-player this)
                    :remove-fn #(.remove-player this %)
                    :edit-fn   #(.edit-player this %)}))
        :game (game-screen)))))

(om/add-root! reconciler
  StarsApp (gdom/getElement "app"))
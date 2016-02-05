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
    [{:app/stars [:db/id :app/screen]}])
  Object
  (componentWillMount [this]
    (let [{:keys [:app/screen]} (:app/stars (om/props this))]
      (case screen
        :setup (om/set-query! this {:query [{:app/stars [:db/id :app/screen]}
                                            {:players (om/get-query SetupItem)}]})
        :default #())))

  (remove-player [this id]
    (om/transact! this `[(entity/remove ~id)]))

  (add-player [this]
    (om/transact! this `[(app/add-player ~(om/get-ident this))]))

  (edit-player [this entity]
    (om/transact! this `[(entity/edit ~entity)]))

  (done [this]
    (let [{:keys [:id]} (om/get-ident this)]
      (om/transact! this `[(entity/edit {:db/id ~id :app/screen :game})])
      (om/set-query! this {:query [{:app/stars [:db/id :app/screen]}]})))

  (render [this]
    (let [{:keys [:app/screen]} (:app/stars (om/props this))]
      (case screen
        :setup (setup-screen
                 (om/computed (-> this om/props :players)
                   {:add-fn    #(.add-player this)
                    :remove-fn #(.remove-player this %)
                    :edit-fn   #(.edit-player this %)
                    :done-fn   #(.done this)}))
        :game (game-screen)))))

(om/add-root! reconciler
  StarsApp (gdom/getElement "app"))
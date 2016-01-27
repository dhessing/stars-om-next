(ns stars.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [datascript.core :as d]
            [sablono.core :as html :refer-macros [html]]))

(enable-console-print!)

;; Components

(defui SetupPlayer
  static om/IQuery
  (query [this]
    [:player/name :db/id])
  Object
  (render [this]
    (let [{:keys [:player/name :db/id]} (om/props this)]
      (html
        [:div.form-group.row {:key id}
         [:label.form-control-label.col-xs-1 "Player "]
         [:div.col-xs-8
          [:input.form-control
           {:type        "text"
            :placeholder "Name"
            :value       name
            ;:on-change   #(om/transact! this `[(player/set-name ~props)])
            }]]
         [:button.btn.btn-danger.col-xs-1
          ;{:on-click #(om/transact! this `[(player/remove ~props)])}
          [:i.fa.fa-trash-o.fa-form]]]))))

(def setup-player (om/factory SetupPlayer))

(defui SetupScreen
  static om/IQuery
  (query [this]
    [{:app/players (om/get-query SetupPlayer)}])
  Object
  (render [this]
    (let [{:keys [:app/players]} (om/props this)]
      (html
        [:div
         [:h1 "Players"]
         [:form
          (map setup-player players)]
         [:div.btn-toolbar
          [:button.btn.btn-secondary
           ;{:on-click #(om/transact! this `[(player/add ~props)])}
           "Add Player"]
          [:button.btn.btn-primary
           ;{:on-click #(swap! app-state assoc :screen :game)}
           "Done"]]]))))

(defui GameScreen
  Object
  (render [this]
    (html [:h1 "Game Screen"])))


;; Router

(defui StarsApp
  static om/IQuery
  (query [this]
    [{:app/stars [:app/screen]}])
  Object
  (render [this]
    (let [{:keys [:app/screen] :as entity} (get-in (om/props this) [:app/stars 0])
          screen-query (or (om/get-query screen) [])
          screen (om/factory screen)]
      (om/set-query! this {:query (vec [{:app/stars (vec (concat [:app/screen] screen-query))}])})
      (screen entity))))

;; State

(def conn (d/create-conn {:app/players {:db/valueType   :db.type/ref
                                        :db/cardinality :db.cardinality/many
                                        :db/isComponent true}}))

(d/transact! conn
  [{:app/title   "Stars"
    :app/screen  SetupScreen
    :app/players [{:player/name "Dzjon"}
                  {:player/name "Heleen"}
                  {:player/name "Sonny"}]}])


(defmulti read om/dispatch)

(defmethod read :app/stars
  [{:keys [state query]} _ _]
  {:value (d/q '[:find [(pull ?e ?selector) ...]
                 :in $ ?selector
                 :where [?e :app/title]]
            (d/db state) query)})

(def reconciler
  (om/reconciler
    {:state  conn
     :parser (om/parser {:read read})}))

(om/add-root! reconciler
  StarsApp (gdom/getElement "app"))
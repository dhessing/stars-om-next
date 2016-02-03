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
    (let [{:keys [:player/name :db/id]} (om/props this)
          {:keys [delete]} (om/get-computed this)]
      (html
        [:div.form-group.row
         [:label.form-control-label.col-xs-1 "Player "]
         [:div.col-xs-8
          [:input.form-control
           {:type        "text"
            :placeholder "Name"
            :value       name
            :on-change   (fn [e]
                           (om/transact! this
                             `[(players/set-name {:db/id ~id :player/name ~(.. e -target -value)})]))}]]
         [:button.btn.btn-danger.col-xs-1
          {:on-click (fn [e] (.preventDefault e) (delete))}
          [:i.fa.fa-trash-o.fa-form]]]))))

(def setup-player (om/factory SetupPlayer))

(defui SetupScreen
  static om/IQuery
  (query [this]
    [:db/id {:app/players (om/get-query SetupPlayer)}])
  Object
  (render [this]
    (let [{:keys [:app/players] :as entity} (om/props this)]
      (html
        [:div
         [:h1 "Players"]
         [:form
          (for [{:keys [:db/id] :as player} players]
            (setup-player
              (om/computed player
                {:delete (fn [] (om/transact! this `[(players/remove {:db/id ~id})]))})))]
         [:div.btn-toolbar
          [:button.btn.btn-secondary
           {:on-click #(om/transact! this `[(players/add ~entity)])}
           "Add Player"]
          [:button.btn.btn-primary
           ;{:on-click #(swap! app-state assoc :screen :game)}
           "Done"]]]))))

(def setup-screen (om/factory SetupScreen))

(defui GameScreen
  Object
  (render [this]
    (html [:h1 "Game Screen"])))


;; Router

(defui StarsApp
  static om/IQuery
  (query [this]
    [{:app/stars (om/get-query SetupScreen)}])
  Object
  (render [this]
    (let [entity (get-in (om/props this) [:app/stars 0])]
      (setup-screen entity))))

;; State

(def conn (d/create-conn {:app/players {:db/cardinality :db.cardinality/many
                                        :db/valueType   :db.type/ref}}))

(d/transact! conn
  [{:db/id       -1
    :app/title   "Stars"
    :app/screen  SetupScreen
    :app/players [{:player/name "Player 1"}
                  {:player/name "Player 2"}]}])


(defmulti read om/dispatch)

(defmethod read :app/stars
  [{:keys [state query]} _ _]
  {:value (d/q '[:find [(pull ?e ?selector) ...]
                 :in $ ?selector
                 :where [?e :app/title]]
            (d/db state) query)})

(defmulti mutate om/dispatch)

(defmethod mutate 'players/add
  [{:keys [state]} _ {:keys [:db/id] :as entity}]
  {:action (fn [] (d/transact! state [{:db/id id :app/players {:player/name ""}}]))})

(defmethod mutate 'players/set-name
  [{:keys [state]} _ entity]
  {:action (fn [] (d/transact! state [entity]))})

(defmethod mutate 'players/remove
  [{:keys [state]} _ {:keys [:db/id]}]
  {:action (fn [] (d/transact! state [[:db.fn/retractEntity id]]))})

(def reconciler
  (om/reconciler
    {:state  conn
     :parser (om/parser {:read read :mutate mutate})}))

(om/add-root! reconciler
  StarsApp (gdom/getElement "app"))
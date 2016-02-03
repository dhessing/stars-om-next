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
  (edit-name [this name]
    (let [{:keys [:db/id]} (om/props this)]
      (om/transact! this `[(entity/edit {:db/id ~id :player/name ~name})])))

  (remove-player [this]
    (let [{:keys [:db/id]} (om/props this)
          {:keys [remove-fn]} (om/get-computed this)]
      (remove-fn id)))

  (render [this]
    (let [{:keys [:player/name]} (om/props this)]
      (html
        [:div.form-group.row
         [:label.form-control-label.col-xs-1 "Player "]
         [:div.col-xs-8
          [:input.form-control
           {:type        "text"
            :placeholder "Name"
            :value       name
            :on-change   #(.edit-name this (.. % -target -value))}]]
         [:a.btn.btn-danger.col-xs-1
          {:on-click #(.remove-player this)}
          [:i.fa.fa-trash-o.fa-form]]]))))

(def setup-player (om/factory SetupPlayer))

(defui SetupScreen
  static om/IQuery
  (query [this]
    [:db/id {:app/players (om/get-query SetupPlayer)}])
  Object
  (remove-player [this id]
    (om/transact! this `[(entity/remove {:id ~id})]))

  (add-player [this]
    (let [{:keys [:db/id]} (om/props this)]
      (om/transact! this `[(app/add-player {:id ~id})])))

  (render [this]
    (let [{:keys [:app/players]} (om/props this)]
      (html
        [:div
         [:h1 "Players"]
         [:form
          (for [player players]
            (setup-player (om/computed player
                            {:remove-fn #(.remove-player this %)})))]
         [:div.btn-toolbar
          [:button.btn.btn-secondary
           {:on-click #(.add-player this)}
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

(defmethod mutate 'app/add-player
  [{:keys [state]} _ {:keys [:id]}]
  {:action (fn [] (d/transact! state [{:db/id id :app/players {:player/name ""}}]))})

(defmethod mutate 'entity/edit
  [{:keys [state]} _ entity]
  {:action (fn [] (d/transact! state [entity]))})

(defmethod mutate 'entity/remove
  [{:keys [state]} _ {:keys [:id]}]
  {:action (fn [] (d/transact! state [[:db.fn/retractEntity id]]))})

(def reconciler
  (om/reconciler
    {:state  conn
     :parser (om/parser {:read read :mutate mutate})}))

(om/add-root! reconciler
  StarsApp (gdom/getElement "app"))
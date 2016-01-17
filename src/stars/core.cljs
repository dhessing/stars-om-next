(ns stars.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [sablono.core :as html :refer-macros [html]]))

(enable-console-print!)


;; Components

(defui SetupScreen
  static om/IQuery
  (query [this]
    [:app/players])
  Object
  (render [this]
    (let [{:keys [:app/players]} (om/props this)]
      (html
        [:div
         [:h1 "Players"]
         [:form
          (for [{:keys [id name] :as player} players]
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
              [:i.fa.fa-trash-o.fa-form]]])]
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

(defui RootView
  static om/IQuery
  (query [this]
    [:app/screen])
  Object
  (render [this]
    (let [{:keys [:app/screen] :as env} (om/props this)
          screen-env (or (om/get-query screen) [])
          screen (om/factory screen)]
      (om/set-query! this {:query (vec (concat [:app/screen] screen-env))})
      (screen (select-keys env screen-env)))))

(defmulti screen-router identity)
(defmethod screen-router :setup [] SetupScreen)
(defmethod screen-router :game [] GameScreen)


;; State

(defonce app-state
  (atom
    {:app/screen   :setup
     :player/by-id {0 {:id 0 :name "Dzjon"}
                    1 {:id 1 :name "Heleen"}}}))

(defmulti read (fn [env key params] key))

(defmethod read :default
  [{:keys [state] :as env} key params]
  (let [st @state]
    (if-let [[_ value] (find st key)]
      {:value value}
      {:value :not-found})))

(defmethod read :app/screen
  [{:keys [state] :as env} key params]
  (let [st @state]
    {:value (screen-router (:app/screen st))}))

(defmethod read :app/players
  [{:keys [state] :as env} key params]
  (let [st @state
        players (for [[id player] (:player/by-id st)]
                  player)]
    {:value players}))

(def reconciler
  (om/reconciler
    {:state  app-state
     :parser (om/parser {:read read})}))

(om/add-root! reconciler
  RootView (gdom/getElement "app"))
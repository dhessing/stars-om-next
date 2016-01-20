(ns stars.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [datascript.core :as d]
            [sablono.core :as html :refer-macros [html]]))

(enable-console-print!)

;; Components

(defui SetupScreen
  static om/IQuery
  (query [this]
    [:game/players])
  Object
  (render [this]
    (let [{:keys [:game/players]} (om/props this)]
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
    [{:app/root [:app/screen]}])
  Object
  (render [this]
    (print (om/props this))
    (let [{:keys [:app/screen] :as entity} (get-in (om/props this) [:app/root 0])
          screen-env (or (om/get-query screen) [])
          screen (om/factory screen)]
      (om/set-query! this {:query [{:app/root (vec (concat [:app/screen] screen-env))}]})
      (screen entity))))

;; State

(def conn (d/create-conn {}))

(d/transact! conn
  [{:db/id        -1
    :app/title    "Stars"
    :app/screen   SetupScreen
    :game/players [{:id 0 :name "Dzjon"}
                   {:id 1 :name "Heleen"}]}])


(defmulti read om/dispatch)

(defmethod read :app/root
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
  RootView (gdom/getElement "app"))
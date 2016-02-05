(ns stars.app
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [datascript.core :as d]
            [sablono.core :as html :refer-macros [html]]))

(enable-console-print!)

;; Components

(defui SetupPlayer
  static om/Ident
  (ident [this props]
    {:id (:db/id props)})
  static om/IQuery
  (query [this]
    [:db/id :player/name])
  Object
  (edit [this name]
    (let [{:keys [:id]} (om/get-ident this)
          {:keys [:edit-fn]} (om/get-computed (om/props this))]
      (edit-fn {:db/id id :player/name name})))

  (remove [this]
    (let [{:keys [:remove-fn]} (om/get-computed (om/props this))]
      (remove-fn (om/get-ident this))))

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
            :on-change   #(.edit this (.. % -target -value))}]]
         [:a.btn.btn-danger.col-xs-1
          {:on-click #(.remove this)}
          [:i.fa.fa-trash-o.fa-form]]]))))

(def setup-player (om/factory SetupPlayer))

(defui PlayerList
  Object
  (render [this]
    (let [players (om/props this)
          {:keys [:remove-fn :add-fn :edit-fn]} (om/get-computed (om/props this))]
      (html
        [:div
         [:h1 "Players"]
         [:form
          (for [player players]
            (setup-player (om/computed player
                            {:remove-fn remove-fn
                             :edit-fn   edit-fn})))]
         [:div.btn-toolbar
          [:button.btn.btn-secondary
           {:on-click add-fn}
           "Add Player"]
          [:button.btn.btn-primary
           ;{:on-click #(swap! app-state assoc :screen :game)}
           "Done"]]]))))

(def player-list (om/factory PlayerList))

(defui GameScreen
  static om/IQuery
  (query [this]
    [])
  Object
  (render [this]
    (html [:h1 "Game Screen"])))

(def game-screen (om/factory GameScreen))


;; Router

(defui StarsApp
  static om/Ident
  (ident [this props]
    {:id (get-in props [:app/stars :db/id])})
  static om/IQuery
  (query [this]
    [{:app/stars [:db/id :app/screen]}
     {:players (om/get-query SetupPlayer)}])
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
        :setup (player-list
                 (om/computed (-> this om/props :players)
                   {:add-fn    #(.add-player this)
                    :remove-fn #(.remove-player this %)
                    :edit-fn   #(.edit-player this %)}))))))

;; State

(def conn (d/create-conn {:app/players {:db/isComponent true
                                        :db/cardinality :db.cardinality/many
                                        :db/valueType   :db.type/ref}}))

(d/transact! conn
  [{:app/title   "Stars"
    :app/screen  :setup
    :app/players [{:player/name "Player 1"}
                  {:player/name "Player 2"}]}])


(defmulti read om/dispatch)

(defmethod read :app/stars
  [{:keys [state query]} _ _]
  {:value (first (d/q '[:find [(pull ?e ?selector) ...]
                        :in $ ?selector
                        :where [?e :app/title "Stars"]]
                   (d/db state) query))})

(defmethod read :players
  [{:keys [state query]} _ _]
  {:value (d/q '[:find [(pull ?e ?selector) ...]
                 :in $ ?selector
                 :where [?app :app/title "Stars"]
                 [?app :app/players ?e]]
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
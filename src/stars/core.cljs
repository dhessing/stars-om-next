(ns stars.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [sablono.core :as html :refer-macros [html]]))

(defonce app-state
  {:app/title "Hello World!"})

(defmulti read (fn [env key params] key))

(defmethod read :default
  [{:keys [state] :as env} key params]
  (let [st @state]
    (if-let [[_ value] (find st key)]
      {:value value}
      {:value :not-found})))

(defui RootView
  static om/IQuery
  (query [this]
    '[:app/title])
  Object
  (render [this]
    (let [{:keys [app/title]} (om/props this)]
      (html [:h1 title]))))

(def reconciler
  (om/reconciler
    {:state app-state
     :parser (om/parser {:read read})}))

(om/add-root! reconciler
  RootView (gdom/getElement "app"))
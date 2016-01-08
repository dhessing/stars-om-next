(ns server
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.util.response :refer [resource-response]]
            [ring.middleware.webjars :refer [wrap-webjars]]))

(defroutes app
  (GET "/" [] (resource-response "index.html" {:root "public"}))
  (route/files "/META-INF/resources" "/")
  (route/not-found "<h1>Page not found</h1>"))

(def handler (wrap-webjars app))
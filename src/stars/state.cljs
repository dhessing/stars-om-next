(ns stars.state
  (:require [datascript.core :as d]))

(def schema
  {:app/game     {:db/isComponent true
                  :db/valueType   :db.type/ref
                  :db/cardinality :db.cardinality/many}
   :player/tiles {:db/cardinality :db.cardinality/many}
   :tile/value   {:db/unique      :db.unique/identity}})

(def initial-state
  [{:db/id      -1
    :type       :app
    :app/screen :setup}

   {:type        :setup/player
    :player/name ""}

   {:type        :setup/player
    :player/name ""}])

(def conn (d/create-conn schema))

(d/transact! conn initial-state)
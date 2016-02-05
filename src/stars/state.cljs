(ns stars.state
  (:require [datascript.core :as d]))

(def initial-state
  [{:app/title   "Stars"
    :app/screen  :setup
    :app/players [{:player/name "Player 1"}
                  {:player/name "Player 2"}]}])

(def conn (d/create-conn {:app/players {:db/isComponent true
                                        :db/cardinality :db.cardinality/many
                                        :db/valueType   :db.type/ref}}))

(d/transact! conn initial-state)
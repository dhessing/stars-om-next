(ns stars.state
  (:require [datascript.core :as d]))

{:db/ident   :app
 :app/screen :game
 :app/state  [{:type        :setup/player
               :player/name ""}

              {:type        :setup/player
               :player/name ""}

              {:type         :game/player
               :player/name  "Player 1"
               :player/tiles [21 23]}

              {:type         :game/player
               :player/name  "Player 2"
               :player/tiles [22]}

              {:db/ident    :turn
               :turn/roll   [1 2 3 4 5]
               :turn/chosen [:* :* :*]}]}

(def schema
  {:db/ident            {:db/unique :db.unique/identity}
   :app/state           {:db/isComponent true
                         :db/valueType   :db.type/ref
                         :db/cardinality :db.cardinality/many}
   :player/tiles        {:db/cardinality :db.cardinality/many}
   :turn/current-player {:db/valueType :db.type/ref}})

(def initial-state
  [{:db/ident   :app
    :app/screen :setup
    :app/state  [{:type        :setup/player
                  :player/name ""}

                 {:type        :setup/player
                  :player/name ""}]}])

(def conn (d/create-conn schema))

(d/transact! conn initial-state)
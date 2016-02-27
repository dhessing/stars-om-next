(ns stars.state
  (:require [datascript.core :as d]))

;{:type       :app
; :app/screen :game
; :app/game   {:type         :game
;              :game/players [{:type         :game/player
;                              :player/name  "Player 1"
;                              :player/tiles [21 23]}
;                             {:type         :game/player
;                              :player/name  "Player 2"
;                              :player/tiles [22]}]
;              :game/roll    [1 2 3 4 5 :* :* 2]}}

(def schema
  {:app/game     {:db/isComponent true
                  :db/valueType   :db.type/ref}
   :game/roll {:db/cardinality :db.cardinality/one}
   :game/players {:db/valueType   :db.type/ref
                  :db/cardinality :db.cardinality/many}
   :player/tiles {:db/cardinality :db.cardinality/many}})

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
(ns stars.state
  (:require [datascript.core :as d]))

(def initial-state
  [{:app/screen :setup}

   {:player/name "Player 1"}

   {:player/name "Player 2"}])

(def conn (d/create-conn {}))

(d/transact! conn initial-state)
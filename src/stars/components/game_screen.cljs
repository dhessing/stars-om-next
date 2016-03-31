(ns stars.components.game-screen
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :as html :refer-macros [html]]
            [stars.constants :as c])
  (:require-macros [devcards.core :refer [defcard]]))


(defn tile-button [value props]
  (html
    [:button.btn.btn-secondary.tile
     (merge {:key value} props)
     [:div.tile-top value]
     [:div.tile-bottom
      (for [i (range (c/tiles value))]
        [:i.fa.fa-star {:key i}])]]))

(defn die-button [props face]
  [:button.btn.btn-secondary.die props
   (if (= face :*) [:i.fa.fa-star] face)])

(defn sum-faces [faces]
  (apply + (replace {:* 5} faces)))


(defui GameScreen
  static om/IQuery
  (query [this]
    [{:game/players [:db/id :player/name :player/tiles]}
     {:turn [:turn/roll :turn/chosen :turn/phase
             {:turn/current-player [:db/id :player/tiles]}]}
     :game/tiles-available])

  Object
  (componentWillUpdate [this next-props next-state]
    (let [{:keys [:game/tiles-available]} next-props]
      (when (empty? tiles-available)
        (.done this))))

  (done [this]
    (let [{:keys [:switch-fn]} (om/get-computed (om/props this))]
      (switch-fn :score)))

  (roll [this]
    (let [chosen (get-in (om/props this) [:turn :turn/chosen])]
      (om/transact! this `[(turn/roll {:chosen ~chosen})])))

  (pick [this face]
    (let [{:keys [:turn/roll :turn/chosen]} (:turn (om/props this))]
      (om/transact! this `[(turn/pick {:face ~face :roll ~roll :chosen ~chosen})])))

  (steal-tile [this player]
    (om/transact! this `[(turn/steal-tile {:player ~player})
                         (turn/end)]))

  (pick-tile [this tile]
    (om/transact! this `[(turn/pick-tile {:tile ~tile})
                         (turn/end)]))

  (lose-tile [this]
    (let [{:keys [:turn/current-player]} (:turn (om/props this))]
      (om/transact! this `[(turn/lose-tile {:player ~current-player})
                           (turn/end)])))

  (render [this]
    (let [{:keys [:game/players :turn :game/tiles-available]} (om/props this)
          {:keys [:turn/roll :turn/chosen :turn/phase :turn/current-player]} turn]
      (html
        [:div
         [:div.card-deck-wrapper
          [:div.card-deck.m-b-1
           (for [{:keys [:db/id :player/name :player/tiles] :as player} players]
             [:div.card {:key   id
                         :style (when (= (:db/id current-player) id) {:borderColor "#333"})}
              [:div.card-header name]
              [:div.card-block
               (if-let [tile (first tiles)]
                 (tile-button tile
                              {:on-click #(.steal-tile this player)
                               :disabled (not (and (= phase :roll)
                                                   (= (sum-faces chosen) tile)
                                                   (some (partial = :*) chosen)
                                                   (not= (:db/id current-player) (:db/id player))))})
                 [:button.btn.btn-secondary.tile.tile-nothing {:disabled true}])]])]]
         [:div.card
          [:div.card-block
           [:div.btn-toolbar
            (for [tile tiles-available]
              (tile-button tile {:on-click #(.pick-tile this tile)
                                 :disabled (not (and (= phase :roll)
                                                     (<= tile (sum-faces chosen))
                                                     (some (partial = :*) chosen)))}))]]]
         [:div.card
          [:div.card-block
           [:div.btn-toolbar.m-b-1
            (for [[i face] roll]
              (let [disabled (or (not= phase :pick)
                                 (some (partial = face) chosen))]
                (die-button {:key           i
                             :disabled      disabled
                             :class         (when (= (om/get-state this) face) "active")
                             :on-click      #(do (.pick this face)
                                                 (om/set-state! this :nil))
                             :on-mouse-over #(when (not disabled) (om/set-state! this face))
                             :on-mouse-out  #(when (not disabled) (om/set-state! this :nil))}
                            face)))
            [:button.btn.btn-primary.stars-btn
             {:on-click #(.roll this)
              :disabled (not= phase :roll)}
             "Roll"]]
           (when (= phase :dead)
             [:div.m-b-1
              [:p "You died"]
              [:button.btn.btn-primary
               {:on-click #(.lose-tile this)}
               "Next Player"]])
           [:div.btn-toolbar.m-b-1
            (for [[i face] (map-indexed vector chosen)]
              (die-button {:key i :disabled true} face))]
           (when (not-empty chosen)
             [:p (str "Total: " (sum-faces chosen))])]]]))))

(def game-screen (om/factory GameScreen))
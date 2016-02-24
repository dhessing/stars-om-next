(ns stars.components.buttons
  (:require [sablono.core :as html :refer-macros [html]]))

(defn delete-button [remove-fn active?]
  (html
    [:a.btn.btn-danger.col-xs-1
     (if active?
       {:on-click remove-fn}
       {:disabled true
        :class :disabled})
     [:i.fa.fa-trash-o.fa-form]]))
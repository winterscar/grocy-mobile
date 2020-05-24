(ns grocy.mobile.components.search
  (:require [grocy.mobile.utils :refer [<sub evt>]]))

(defn search-result
  [{name :name id :key}]
  [:div {:onClick #(evt> [:select-product id])} name])

(defn search-bar
  []
  [:div
   [:input {:onInput #(evt> [:search/query (.. % -target -value)])}]
   [:div {:style {:max-height "300px" :overflow-y "scroll"}}
    (for [result (<sub [:search/results])]
      [search-result {:key (:id result) :name (:name result)}])]])
(ns grocy.mobile.views
   (:require
    [re-frame.core :as rf]
    [grocy.mobile.components.barcode.scanner :refer [barcode-scanner]]
    [grocy.mobile.components.search :refer [search-bar]]))

(def style
  {:wrapper 
   {:width "100%" :height "100vh"}
   :barcode-scanner
   {:width "100%" :height "30%"}})

(defn card
  [name key]
  [:div {:class "card" :key key} name])

(defn card-picker
  [items]
  [:div {:class "card-picker"}
   (map #(card %1 %2) items (range))])

(def search-area {:top-pct 0.3 :left-pct 0 :h-pct 0.4 :w-pct 1})

(defn main []
  [:div {:style (:wrapper style)}
   [barcode-scanner {:style (:barcode-scanner style)}]
   [:div (:name @(rf/subscribe [:current-product]))]
   [search-bar]])
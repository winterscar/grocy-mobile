(ns grocy.mobile.components.number-picker
  (:require
   [reagent.core :as r]
   [grocy.mobile.utils :as u]))


(def style 
  {:display "flex"
   :flex-direction "row"
   :width "100%"
   :justify-content "space-between"})

(def number-style
  {:padding "0 20px"})



(defn number-picker
  []
  (let [value (r/atom 0)]
    (fn []
      [:div {:style style}
       [:button.decrease {:onClick (u/h (swap! value dec))} "-"]
       [:div {:style number-style} @value]
       [:button.increase {:onClick (u/h (swap! value inc))} "+"]])))
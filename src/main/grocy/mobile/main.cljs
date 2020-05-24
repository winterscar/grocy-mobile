(ns grocy.mobile.main
  (:require [reagent.dom :as reagent]
            [re-frame.core :as rf]
            [grocy.mobile.events] ;; These two are only required to make the compiler
            [grocy.mobile.subs]   ;; load them (see docs/App-Structure.md)
            [grocy.mobile.views :as ui]))


;; -- Entry Point -------------------------------------------------------------

(defn init!
  []
  (rf/dispatch-sync [:init])     ;; puts a value into application state
  (rf/dispatch [:populate-products])
  (reagent/render
   [ui/main]
   (js/document.getElementById "app"))
  (println "App Loaded"))

(defn reload!
  []
  (println "Reloaded.")
  (init!))
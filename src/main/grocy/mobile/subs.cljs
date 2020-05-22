(ns grocy.mobile.subs
  (:require [re-frame.core :refer [reg-sub]]))

;; -------------------------------------------------------------------------------------
;; Layer 2
;; Simple query functions
;;

(reg-sub
 :barcode                    ;; usage:   (subscribe [:barcode])
 (fn [db _]                  ;; db is the (map) value stored in the app-db atom
   (:barcode db)))           ;; extract a value from the application state

;; -------------------------------------------------------------------------------------
;; Layer 3
;; Advanced query functions
;;

;; (defn do-something [a b c] (+ a b c))

;; (reg-sub
;;  :priced-costs
;;  :<- [:simple-query-a]
;;  :<- [:simple-query-b]
;;  :<- [:simple-query-c]
;;  (fn [[a b c] _] (do-something a b c)))
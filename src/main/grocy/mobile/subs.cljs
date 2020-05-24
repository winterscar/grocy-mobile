(ns grocy.mobile.subs
  (:require [re-frame.core :refer [reg-sub]]
            [clojure.string :refer [includes? lower-case]]))

;; -------------------------------------------------------------------------------------
;; Layer 2
;; Simple query functions
;;

(reg-sub
 :barcode                    ;; usage:   (subscribe [:barcode])
 (fn [db _]                  ;; db is the (map) value stored in the app-db atom
   (:barcode db)))           ;; extract a value from the application state

(reg-sub
 :products                   ;; usage:   (subscribe [:product-names])
 (fn [db _]                  ;; db is the (map) value stored in the app-db atom
   (:products db)))          ;; extract a value from the application state

(reg-sub
 :current-product
 (fn [db _]
   (:current-product db)))

(reg-sub
 :search/query
 (fn [db _]
   (-> db :search :query)))

;; -------------------------------------------------------------------------------------
;; Layer 3
;; Advanced query functions
;;

(defn search
  "searches the values collection for entities where the value for key k includes query."
  [query values k]
  (when query
    (let [query (lower-case query)]
      (filter #(-> % k lower-case
                   (includes? query)) values))))

(reg-sub
 :search/results
 :<- [:search/query]
 :<- [:products]
 (fn [[query products] _]
   (search query products :name)))

;; (defn do-something [a b c] (+ a b c))

;; (reg-sub
;;  :priced-costs
;;  :<- [:simple-query-a]
;;  :<- [:simple-query-b]
;;  :<- [:simple-query-c]
;;  (fn [[a b c] _] (do-something a b c)))
(ns grocy.mobile.events
  (:require
   [grocy.mobile.db :refer [default-db]]
   [re-frame.core :refer [reg-event-db reg-event-fx path after dispatch]]
   [superstructor.re-frame.fetch-fx]
   [cljs.spec.alpha :as s]))

;; -- First Interceptor ------------------------------------------------------
;;
;; Event handlers change state, that's their job. But what happens if there's
;; a bug in the event handler and it corrupts application state in some subtle way?
;; Next, we create an interceptor called `check-spec-interceptor`.
;; Later, we use this interceptor in the interceptor chain of all event handlers.
;; When included in the interceptor chain of an event handler, this interceptor
;; runs `check-and-throw` `after` the event handler has finished, checking
;; the value for `app-db` against a spec.
;; If the event handler corrupted the value for `app-db` an exception will be
;; thrown. This helps us detect event handler bugs early.
;; Because all state is held in `app-db`, we are effectively validating the
;; ENTIRE state of the application after each event handler runs.  All of it.


(defn check-and-throw
  "Throws an exception if `db` doesn't match the Spec `a-spec`."
  [a-spec db]
  (when-not (s/valid? a-spec db)
    (throw (ex-info (str "spec check failed: " (s/explain-str a-spec db)) {}))))

;; now we create an interceptor using `after`
(def check-spec-interceptor (after (partial check-and-throw :grocy.mobile.db/db)))

;; -- Interceptor Chain ------------------------------------------------------
;;
;; Each event handler can have its own chain of interceptors.
;; We now create the interceptor chain shared by all event handlers
;; which manipulate todos.
;; A chain of interceptors is a vector of interceptors.
;; Explanation of the `path` Interceptor is given further below.
(def search-interceptor [check-spec-interceptor    ;; ensure the spec is still valid  (after)
                         (path :search)])          ;; grab foo out of the app db 
;; -- Event Handlers ----------------------------------------------------------

;; usage:  (dispatch [:initialise-db])
;;
;; This event is dispatched in the app's `main` (core.cljs).
;; It establishes initial application state in `app-db`.

(reg-event-db
 :init
 [check-spec-interceptor]
 (fn [_ _]
   default-db))

(reg-event-db
 :search/query
 [search-interceptor]
 (fn [search [_ query]]
   (assoc search :query query)))

(reg-event-db
 :scan
 [check-spec-interceptor]
 (fn [db [_ code]]
   (when (not= code (:barcode db))
     (dispatch [:lookup code]))
   (assoc db :barcode code)))

(def grocy-api "https://grocy.pasquet.co/api")
(def grocy-api-key "L6DHnl4169PFTjaTsMFIDkmMHXwoqyuTmy2sDPtcIZLqkgJsfC")

(reg-event-fx
 :lookup
 (fn
   [_ [_ code]]
   {:fetch {:method                 :get
            :url                    (str grocy-api "/stock/products/by-barcode/" code)
            :headers                {"GROCY-API-KEY" grocy-api-key}
            :mode                   :cors
            :credentials            "omit"
            :timeout                2000
            :response-content-types {#"application/.*json" :json}
            :on-success             [:select-product-success]
            :on-failure             [:lookup-failure]}}))

(reg-event-fx
 :populate-products
 (fn
   [_ _]
   {:fetch {:method                 :get
            :url                    (str grocy-api "/objects/products")
            :headers                {"GROCY-API-KEY" grocy-api-key}
            :mode                   :cors
            :credentials            "omit"
            :timeout                5000
            :response-content-types {#"application/.*json" :json}
            :on-success             [:products-success]
            :on-failure             [:products-failure]}}))

(reg-event-db
 :products-success
 [check-spec-interceptor]
 (fn
   [db [_ response]]
   (-> db
       (assoc :products (:body response)))))  ;; fairly lame processing

(reg-event-fx
 :select-product
 (fn
   [_ [_ id]]
   {:fetch {:method                 :get
            :url                    (str grocy-api "/objects/products/" id)
            :headers                {"GROCY-API-KEY" grocy-api-key}
            :mode                   :cors
            :credentials            "omit"
            :timeout                5000
            :response-content-types {#"application/.*json" :json}
            :on-success             [:select-product-success]
            :on-failure             [:select-failure]}}))

(reg-event-db
 :select-product-success
 [check-spec-interceptor]
 (fn
   [db [_ response]]
   (-> db
       (assoc :current-product
              (assoc (:body response) :dirty nil)))))  ;; fairly lame processing

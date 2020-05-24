(ns grocy.mobile.db
  (:require [cljs.reader]
            [cljs.spec.alpha :as s]))

;; -- Spec --------------------------------------------------------------------
;;
;; This is a clojure.spec specification for the value in app-db. It is like a
;; Schema. See: http://clojure.org/guides/spec
;;
;; The value in app-db should always match this spec. Only event handlers
;; can change the value in app-db so, after each event handler
;; has run, we re-check app-db for correctness (compliance with the Schema).
;;
;; How is this done? Look in events.cljs and you'll notice that all handlers
;; have an "after" interceptor which does the spec re-check.
;;
;; None of this is strictly necessary. It could be omitted. But we find it
;; good practice.
 
(s/def ::barcode string?)
(s/def ::product map?)
(s/def ::products (s/coll-of ::product))
(s/def ::dirty (s/nilable (s/coll-of keyword?)))
(s/def ::current-product (s/keys :req-un [::dirty]))
(s/def ::query string?)
(s/def ::results (s/coll-of ::product))
(s/def ::search (s/keys :opt-un [::query ::results]))
;; Database
(s/def ::db (s/keys :opt-un [::barcode ::products ::current-product ::search]))

;; -- Default app-db Value  ---------------------------------------------------
;;
;; When the application first starts, this will be the value put in app-db
;; Unless, of course, there are todos in the LocalStore (see further below)
;; Look in:
;;   1.  `core.cljs` for  "(dispatch-sync [:initialise-db])"
;;   2.  `events.cljs` for the registration of :initialise-db handler

(def default-db {})

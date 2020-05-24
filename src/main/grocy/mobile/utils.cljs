(ns grocy.mobile.utils
  (:require [re-frame.core]))


(defn <sub [query-v] @(re-frame.core/subscribe query-v))
(def evt> re-frame.core/dispatch)
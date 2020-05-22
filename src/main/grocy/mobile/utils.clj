(ns grocy.mobile.utils)

(defmacro h
  ([& body]
   `(fn [~'event] ~@body nil)))  ; always return nil
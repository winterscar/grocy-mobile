(ns grocy.mobile.components.barcode.scanner
  (:require
   [reagent.core :as r]
   [re-frame.core :as rf]
   [shadow.resource :as rc]))

(defonce style
  {:video-element
   {:width "100%"
    :background-color "#665"}})

(def beep (rc/inline "./scan_sound"))

(defn play-beep
  []
  (-> (js/Audio. beep) .play))

(defonce state
  (atom
   {:canvas nil
    :video nil}))

(defonce rstate
  (r/atom {:video nil :running false}))

(defn handle-video-success [s ref]
  (set! (.-srcObject ref) s)
  (swap! state assoc :frame-grabber (js/ImageCapture. (first (. s getVideoTracks))))
  (swap! state assoc :video ref))

(defn handle-video-error [error] (println "Error getting video: " (.-message error)))

(defn start-video!
  "Gets user video (from camera or webcam) and streams it to the video 
   dom element specified by ref."
  [ref]
  (when ref
    (let [constraints {:video {:width {:ideal 1920} :facingMode "environment"}}]
      (-> js/navigator
          .-mediaDevices
          (.getUserMedia (clj->js constraints))
          (.then #(handle-video-success % ref))
          (.catch handle-video-error)))))

(declare scan-frame)

(defn process-result [msg]
  (if-let [data (seq (.-data msg))]
    (do 
      (play-beep)
      (println (first data))
      (rf/dispatch [:scan (first data)])
      (js/setTimeout scan-frame 1500))
    (scan-frame)))

(def worker (js/Worker. "/barcode/zbar_worker.js"))
(.. worker (addEventListener "message" (fn [e] (process-result e))))

(defn start-wasm [s]
  (.. worker (postMessage (clj->js (assoc s :init true)))))

(defn scan-frame []
  (let [grabber (:frame-grabber @state)
        vid     (:video         @state)]
    (-> (. grabber (grabFrame))
        (.then #(.. worker (postMessage %))))))

(defn barcode-scanner
  "Show a video of the user's camera when available, and search it for barcodes."
  [search-area]
  [:div
   [:video#vid {:style    (:video-element style)
                :autoPlay true
                :hidden   false
                :ref      start-video!
                :onPlay   #(start-wasm search-area)
                :onClick  scan-frame}]])
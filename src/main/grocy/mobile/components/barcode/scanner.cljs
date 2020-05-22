(ns grocy.mobile.components.barcode.scanner
  (:require
   [re-frame.core :as rf]
   [shadow.resource :as rc]))

;; ----- Util functions ----------------------------------------------------------------------------

(defn play-beep
  []
  (let [beep (rc/inline "./scan_sound")]
    (-> (js/Audio. beep) .play)))

;; -------------------------------------------------------------------------------------------------

;; ----- Camera -> Video ---------------------------------------------------------------------------

(defonce grabber (atom nil))
(defonce video-ref (atom nil))

(defn handle-video-success [s ref]
  (set! (.-srcObject ref) s)
  (reset! grabber (js/ImageCapture. (first (. s getVideoTracks))))
  (reset! video-ref ref))

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

;; -------------------------------------------------------------------------------------------------

;; ----- Barcode scanning --------------------------------------------------------------------------

;; Initialize the web-worker and start listening for events
(declare process-result)
(def worker (js/Worker. "/barcode/worker.js"))
(.. worker (addEventListener "message" (fn [e] (process-result e))))

(defn get-search-area
  "Returns the (un-scaled) video dimensions such as they appear in the video element.
   I'm not sure I really understand what's going on here, so @me if it's wrong.
   video should be a <video> DOM element."
  [video]
  (let [v  video
        vw (. v -offsetWidth)
        vh (. v -offsetHeight)
        fw (. v -videoWidth)
        fh (. v -videoHeight)
        ws (/ fw vw)
        hs (/ fh vh)]
    (if (< ws hs)
      (let [h (int (/ fh (/ hs ws)))]
        {:x 0 :y (int (/ (- fh h) 2)) :w fw :h h})
      (let [w (int (/ fw (/ ws hs)))]
        {:x (int (/ (- fw w) 2)) :y 0 :w w :h fh}))))

(defn scan-frame
  "Request the current video frame be scanned for barcodes."
  []
  (let [search-area (get-search-area @video-ref)]
    (println search-area)
    (-> (. @grabber (grabFrame))
        (.then #(.. worker (postMessage
                            (clj->js  {:type "frame"
                                       :frame %
                                       :search search-area})))))))

(defn handle-scan
  "Check if the scanned frame contained a barcode and if so, dispatch it into re-frame. Whether the frame contained a barcode or not, start scanning the next frame."
  [codes]
  (if (seq codes)
    (do
      (play-beep)
      (rf/dispatch [:scan (first codes)])
      (js/setTimeout scan-frame 1500))
    (scan-frame)))

(defn process-result
  "Dispatch incoming messages from the web-worker."
  [raw_msg]
  (let [msg     (js->clj (.-data raw_msg) :keywordize-keys true)
        type    (:type msg)]
    (case type
      "ready" (scan-frame)
      "scan"  (handle-scan (:codes msg)))))

(defn init
  "Tell the web-worker to initialize itself."
  []
  (.. worker (postMessage (clj->js {:type "init"}))))

;; -------------------------------------------------------------------------------------------------

(defn barcode-scanner
  "Show a video of the user's camera when available, and search it for barcodes."
  [{:keys [style]}]
  [:div {:style style}
   [:video {:style    {:width "100%" :height "100%" :object-fit "cover"}
            :autoPlay true
            :ref      start-video!
            :onPlay   init}]])
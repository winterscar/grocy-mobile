(ns grocy.mobile.components.roller
  (:require
   [reagent.core :as r]
   [grocy.mobile.utils :as u]))

(def initial-state
  {:in-progress false
   :prev-xs     []
   :value       0
   :animating   false})

(defn add-sample
  "Appends the provided x to samples, and prunes any samples older than 100ms."
  [samples x]
  (let [now (.now js/Date)]
    (-> (filter #(< (- now (:time %)) 100) samples)
     (conj {:time now :x x}))))

(def adj-factor 0.1)

(defn update-val!
  "Mutates the provided state by calculating how far in the x-axis the user
   has moved since the last update. That amount is then scaled down (1:1 would
   be way too fast) and added to the state total. 
   The movement direction is inverted so that the movement feels like 'rolling a wheel'."
  [state touch]
  (let [x (:x touch)
        {:keys [prev-xs in-progress value]} @state]
    (if (not in-progress)
      (swap! state assoc :prev-xs (add-sample [] x) :in-progress true)
      (let [delta-x (- x (:x (last prev-xs)))
            inc-factor (* delta-x adj-factor)]
        (when (= 0 (mod (int value) 30))
          (println "bzz bzz")
          (. (.-navigator js/window) vibrate 100))
        (swap! state assoc
               :prev-xs (add-sample prev-xs x)
               :value (+ value inc-factor))))))

(defn get-touch-coords
  [event]
  (let [touch (first (.-touches event))
        x (.-clientX touch)
        y (.-clientY touch)]
    {:x x :y y}))

(def friction 0.99)
(def stop-threshold 0.005)

(defn flick-speed
  "Given a set of position / time pairs, returns average speed in px/ms"
  [samples]
  (let [Δt (Math/abs (- (:time (last samples)) (:time (first samples))))
        Δx (- (:x (last samples)) (:x (first samples)))]
    (-> (/ Δx Δt) - (* adj-factor))))

(defn animate-momentum
  "Fake a continuous slow-down at the end of a touch, rather than come to a 
   instant stop. This mimicks the effect of momentum, as the animation duration
   is a function of the touch speed in it's last 100ms."
  [state speed]
  (if (and (:animating @state) (> (Math/abs speed) stop-threshold))
    (let [speed (* speed friction)]
      (.requestAnimationFrame
       js/window
       #((update-val! state {:x (+ speed (-> @state :prev-xs last :x))}) ; You need to take time into account here...
         (animate-momentum state speed))))
    (swap! state assoc :animating false)))

(defn start-animation
  [state]
  (comment (let [speed (* 160 (flick-speed (:prev-xs @state)))]
             (println "START ANIMATE WITH SPEED" speed)
             (swap! state assoc :animating true)
             (animate-momentum state speed))))

(declare event) ; the u/h macro creates a silent event variable, this hides the kondo warning.
(defn roller
  []
  (let [state (r/atom initial-state)]
    (fn []
      (let [s @state
            v (:value s)
            r (- v (mod v 30))]
        [:div.ring
         {:onTouchStart (u/h (println "START TOUCH") (reset! state (assoc initial-state :value (:value @state))))
          :onTouchMove (u/h (update-val! state (get-touch-coords event)))
          :onTouchEnd (u/h (start-animation state))
          :style {:transform (str "rotateY(" r "deg)")
                  :transition "transform 500ms"}}
         [:div.plane.one "1"]
         [:div.plane.two "2"]
         [:div.plane.three "3"]
         [:div.plane.four "4"]
         [:div.plane.five "5"]
         [:div.plane.six "6"]
         [:div.plane.seven "7"]
         [:div.plane.eight "8"]
         [:div.plane.nine "9"]
         [:div.plane.ten "10"]
         [:div.plane.eleven "11"]
         [:div.plane.twelve "12"]]))))
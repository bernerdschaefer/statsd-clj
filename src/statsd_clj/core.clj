(ns statsd-clj.core)

(defn -main
  "I don't do a whole lot."
  [& args]
  (println "Hello, World!"))

(def metrics
  (agent {:counters {} :timers {} :gauges {}}))

(defn update-counter
  "Increment counter with name bucket by i, with a default sample rate of 1"
  ([m bucket i] (update-counter m bucket i 1))
  ([m bucket i sample-rate]
   (let [incr (* i (/ 1 sample-rate))]
     (assoc-in m [:counters bucket]
               (+ (get-in m [:counters bucket] 0) incr)))))

(defn update-gauge
  "Set gauge with name bucket to i"
  [m bucket i]
  (assoc-in m [:gauges bucket] i))

(defn update-timer
  "Add timing i to the bucket"
  [m bucket i]
  (assoc-in m [:timers bucket] (conj (get-in m [:timers bucket] []) i)))

(defn delete-counters
  "Delete counters"
  [m counters]
  (assoc m :counters (apply dissoc (:counters m) counters)))

(defn delete-gauges
  "Delete gauges"
  [m gauges]
  (assoc m :gauges (apply dissoc (:gauges m) gauges)))

(defn delete-timers
  "Delete timers"
  [m timers]
  (assoc m :timers (apply dissoc (:timers m) timers)))

(defn reset-metrics
  "Zero-out all counters, gauges, and timers"
  [metrics]
  {:counters (zipmap (keys (:counters metrics)) (repeat 0))
   :gauges   (zipmap (keys (:gauges   metrics)) (repeat 0))
   :timers   (zipmap (keys (:timers   metrics)) (repeat []))})

(defn flush-metrics
  "Reset metrics and return the original value"
  []
  (let [p (promise)]
    (send metrics (fn [m p]
                    (deliver p m)
                    (reset-metrics m)) p)
    (deref p)))

;; backends

(def console-backend
  {:flush  (fn [g m] ((println "in console") g))
   :agent  (agent {})})

(def graphite-backend
  {:flush  (fn [g m] ((println "in graphite") g))
   :agent  (agent {})})

(def backends [console-backend graphite-backend])

(defn publish-metrics [metrics backends]
  (doseq [backend backends]
    (send-off (:agent backend) (:flush backend) metrics)))

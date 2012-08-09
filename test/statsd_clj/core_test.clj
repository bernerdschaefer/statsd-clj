(ns statsd-clj.core-test
  (:use clojure.test
        statsd-clj.core))

(deftest updating-counters
         (is (= {:counters {:a 1}} (update-counter {:counters {}} :a 1)))
         (is (= {:counters {:a 2}} (update-counter {:counters {:a 1}} :a 1)))
         (is (= {:counters {:a 1}} (update-counter {:counters {}} :a 1 1)))
         (is (= {:counters {:a 2}} (update-counter {:counters {:a 1}} :a 1 1)))
         (is (= {:counters {:a 2.0}} (update-counter {:counters {}} :a 1 0.5)))
         (is (= {:counters {:a 3.0}} (update-counter {:counters {:a 1}} :a 1 0.5)))
         (is (= {:counters {:a 1 :b 1}} (update-counter {:counters {:b 1}} :a 1))))

(deftest updating-gauges
         (is (= {:gauges {:a 1}} (update-gauge {:gauges {}} :a 1)))
         (is (= {:gauges {:a 5}} (update-gauge {:gauges {:a 1}} :a 5)))
         (is (= {:gauges {:a 1 :b 1}} (update-gauge {:gauges {:b 1}} :a 1))))

(deftest updating-timers
         (is (= {:timers {:a [1]}} (update-timer {:timers {}} :a 1)))
         (is (= {:timers {:a [1 2]}} (update-timer {:timers {:a [1]}} :a 2))))

(deftest deleting-counters
         (is (= {:counters {}} (delete-counters {:counters {}} [:a])))
         (is (= {:counters {}} (delete-counters {:counters {:a 1}} [:a])))
         (is (= {:counters {:b 1}} (delete-counters {:counters {:a 1 :b 1}} [:a]))))

(deftest deleting-gauges
         (is (= {:gauges {}} (delete-gauges {:gauges {}} [:a])))
         (is (= {:gauges {}} (delete-gauges {:gauges {:a 1}} [:a])))
         (is (= {:gauges {:b 1}} (delete-gauges {:gauges {:a 1 :b 1}} [:a]))))

(deftest deleting-timers
         (is (= {:timers {}} (delete-timers {:timers {}} [:a])))
         (is (= {:timers {}} (delete-timers {:timers {:a [1]}} [:a])))
         (is (= {:timers {:b [1]}} (delete-timers {:timers {:a [1] :b [1]}} [:a]))))

(deftest resetting-metrics
         (is (= {:counters {} :gauges {} :timers {}} (reset-metrics {:counters {} :gauges {} :timers {}})))
         (is (= {:counters {:a 0} :gauges {} :timers {}} (reset-metrics {:counters {:a 2} :gauges {} :timers {}})))
         (is (= {:counters {} :gauges {:a 0} :timers {}} (reset-metrics {:counters {} :gauges {:a 2} :timers {}})))
         (is (= {:counters {} :gauges {} :timers {:a []}} (reset-metrics {:counters {} :gauges {} :timers {:a [1 2]}}))))

(deftest working-with-metrics
         (await
           (send metrics update-counter :a 1)
           (send metrics update-counter :a 1 0.5)
           (send metrics update-gauge :a 1)
           (send metrics update-gauge :b 1)
           (send metrics update-timer :a 1)
           (send metrics update-timer :a 2))

         (let [m (flush-metrics)]
           (is (= {:a 3.0} (:counters m)))
           (is (= {:a 1 :b 1} (:gauges m)))
           (is (= {:a [1 2]} (:timers m))))

         (let [m @metrics]
           (is (= {:a 0} (:counters m)))
           (is (= {:a 0 :b 0} (:gauges m)))
           (is (= {:a []} (:timers m)))))

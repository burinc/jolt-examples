(ns app.core-test
  "Checks app.core, the minimal example: -main greets, joins its arguments, and
  returns. Run with: jolt -M:test

  This file used to be a byte-identical copy of nrepl-example's suite, driving an
  app.core with live device state (status/toggle-led!/start-device-loop!) over a
  real nREPL connection. None of those vars exist here — this example is a
  hello-world — so it could never have passed."
  (:require [app.core :as core]))

(def failures (atom 0))

(defn check [label expected actual]
  (if (= expected actual)
    (println "  ok  " label)
    (do (swap! failures inc)
        (println "  FAIL" label "— expected" (pr-str expected) "got" (pr-str actual)))))

(defn -main [& _]
  (println "app.core/-main")
  ;; println puts a space between its arguments, so the no-args greeting keeps a
  ;; trailing space before the newline. Asserted exactly rather than trimmed, so
  ;; a change to the greeting shows up here.
  (check "greets with no arguments" "Hello from Jolt! \n"
         (with-out-str (core/-main)))
  (check "joins its arguments" "Hello from Jolt! a, b\n"
         (with-out-str (core/-main "a" "b")))
  ;; -main returning at all is the point: it used to park in
  ;; (loop [] (Thread/sleep 3600000) (recur)), left over from the nREPL example,
  ;; so the greeting printed and the program then hung forever. Capture the return
  ;; through an atom so the greeting stays off this suite's output.
  (check "-main returns nil" nil
         (let [ret (atom :never-returned)]
           (with-out-str (reset! ret (core/-main "done")))
           @ret))

  (println)
  (if (zero? @failures)
    (println "all passed")
    (println @failures "FAILED"))
  (when (pos? @failures)
    (throw (ex-info "test failures" {:n @failures}))))

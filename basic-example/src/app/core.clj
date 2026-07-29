(ns app.core
  "A compiled jolt program.

  Run interpreted:
    jolt run -m app.core
  Or build a standalone binary, then run it:
    jolt build -m app.core -o basic-example
    ./basic-example"
  (:require [clojure.string :as string]))

(defn -main [& args]
  (println "Hello from Jolt!" (string/join ", " args))
  (loop [] (Thread/sleep 3600000) (recur)))



(ns app.core
  "Minimal reproduction: an alias-resolved keyword (::str/trim-mode)
  is accepted by `joltc run` but rejected by `joltc build`'s
  require scanner with `Invalid token: ::str/trim-mode`."
  (:require [clojure.string :as str]))

(def config
  ;; ::str/trim-mode reads as :clojure.string/trim-mode — standard
  ;; Clojure reader semantics, accepted by joltc run / repl.
  {::str/trim-mode :both})

(defn -main [& _]
  (println "config:" config)
  (println "ok — alias-resolved keywords work at runtime"))

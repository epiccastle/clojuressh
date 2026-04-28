(ns cljssh
  "A Clojure library for using SSH in Clojure that is API compatible with bbssh.

  See https://github.com/epiccastle/cljssh for API reference."
  (:require [cljssh.terminal :as terminal])
  (:gen-class))

(defn ssh
  "Execute a command over SSH. API compatible with bbssh."
  [host cmd & {:as opts}]
  (throw (ex-info "Not yet implemented" {:host host :cmd cmd :opts opts})))

(defn -main
  "Entry point. Prints a greeting and exits."
  [& _args]
  (println "hello")
  (println (terminal/get-width) "x" (terminal/get-height))
  (print "type:")
  (flush)
  (let [x (read-line)]
    (println "you typed: " x))
  (print "type:")
  (flush)
  (terminal/enter-raw-mode 1)
  (let [x (read-line)]
    (terminal/leave-raw-mode 1)
    (println "\nyou typed: " x)))

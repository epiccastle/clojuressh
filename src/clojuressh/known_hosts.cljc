(ns clojuressh.known-hosts
  #?(:bb (:require [babashka.pods :as pods]))
  #?(:bb (:import)
     :clj (:import [com.jcraft.jsch KnownHosts JSch])))

#?(:bb (pods/load-pod 'epiccastle/bbssh "0.7.0"))
#?(:bb (require '[pod.epiccastle.bbssh.known-hosts :as known-hosts]))

;; (defn new
;;   [agent]
;;   (references/add-instance
;;    (KnownHosts.
;;     ^JSch agent)))

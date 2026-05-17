(ns clojuressh.known-hosts
  (:require [clojuressh.impl.load-pod]
            #?(:bb [pod.epiccastle.bbssh.known-hosts :as known-hosts]))
  #?(:bb (:import)
     :clj (:import [com.jcraft.jsch KnownHosts JSch])))

;; (defn new
;;   [agent]
;;   (references/add-instance
;;    (KnownHosts.
;;     ^JSch agent)))

(ns cljssh.config-repository
  (:require [cljssh.callbacks :as callbacks]
            [cljssh.cleaner :as cleaner])
  (:import [com.jcraft.jsch ConfigRepository OpenSSHConfig]
           [java.util Vector]))

;; cljssh.* are invoked on pod side.

(set! *warn-on-reflection* true)

(defn new [reply-fn]
  (let [result
        (proxy [ConfigRepository] []
          (getConfig [hostname]
            (callbacks/call-method reply-fn :get-config [hostname])))]
    (cleaner/register-delete-fn result #(reply-fn [:done] ["done"]))
    (reply-fn [:result result])
    nil))

(defn get-config [^ConfigRepository config-repository ^String hostname]
  (.getConfig config-repository hostname))

(defn openssh-config-file [config-file]
  (OpenSSHConfig/parseFile config-file))

(defn openssh-config-string [config]
  (OpenSSHConfig/parse config))

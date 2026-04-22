(ns io.epiccastle.config-repository
  (:require [io.epiccastle.callbacks :as callbacks]
            [io.epiccastle.cleaner :as cleaner])
  (:import [com.jcraft.jsch ConfigRepository OpenSSHConfig]
           [java.util Vector]))

;; io.epiccastle.* are invoked on pod side.

(set! *warn-on-reflection* true)

(defn ^:async new [reply-fn]
  (let [result
        (proxy [ConfigRepository] []
          (getConfig [hostname]
            (callbacks/call-method reply-fn :get-config [hostname])))]
    (cleaner/register-delete-fn result #(reply-fn [:done] ["done"]))
    (reply-fn [:result result])
    nil))

(defn get-config [config-repository hostname]
  (.getConfig
   ^ConfigRepository config-repository
   ^String hostname))

(defn openssh-config-file [config-file]
  (OpenSSHConfig/parseFile config-file))

(defn openssh-config-string [config]
  (OpenSSHConfig/parse config))

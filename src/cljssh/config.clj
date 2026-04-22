(ns cljssh.config
  (:require [cljssh.callbacks :as callbacks]
            [cljssh.cleaner :as cleaner])
  (:import [com.jcraft.jsch ConfigRepository$Config]))

;; cljssh.* are invoked on pod side.

(set! *warn-on-reflection* true)

(defn new [reply-fn]
  (let [result
        (proxy [ConfigRepository$Config] []
          (getHostname []
            (callbacks/call-method reply-fn :get-hostname []))
          (getUser []
            (callbacks/call-method reply-fn :get-user []))
          (getPort []
            (callbacks/call-method reply-fn :get-port []))
          (getValue [key]
            (callbacks/call-method reply-fn :get-value [key]))
          (getValues [key]
            (into-array
             String
             (callbacks/call-method reply-fn :get-values [key]))))]
    (cleaner/register-delete-fn result #(reply-fn [:done] ["done"]))
    (reply-fn [:result result])
    nil))

(defn get-hostname [^ConfigRepository$Config config]
  (.getHostname config))

(defn get-user [^ConfigRepository$Config config]
  (.getUser config))

(defn get-port [^ConfigRepository$Config config]
  (.getPort config))

(defn get-value [^ConfigRepository$Config config ^String key]
  (.getValue config key))

(defn get-values [^ConfigRepository$Config config ^String key]
  (.getValues config key))

(ns io.epiccastle.config
  (:require [io.epiccastle.callbacks :as callbacks]
            [io.epiccastle.cleaner :as cleaner])
  (:import [com.jcraft.jsch ConfigRepository$Config]))

;; io.epiccastle.* are invoked on pod side.

(set! *warn-on-reflection* true)

(defn ^:async new [reply-fn]
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

(defn get-hostname [config]
  (.getHostname
   ^ConfigRepository$Config config))

(defn get-user [config]
  (.getUser
   ^ConfigRepository$Config config))

(defn get-port [config]
  (.getPort
   ^ConfigRepository$Config config))

(defn get-value [config key]
  (.getValue
   ^ConfigRepository$Config config
   ^String key))

(defn get-values [config key]
  (.getValues
   ^ConfigRepository$Config config
   ^String key))

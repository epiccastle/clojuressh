(ns clojuressh.config
  #?(:bb (:require [babashka.pods :as pods]))
  #?(:bb (:import)
     :clj (:import [com.jcraft.jsch ConfigRepository$Config])))

#?(:bb (pods/load-pod 'epiccastle/bbssh "0.7.0"))
#?(:bb (require '[pod.epiccastle.bbssh.config :as config]))

(set! *warn-on-reflection* true)

(defn new
  "Create a new config instance. Pass in a hashmap containing
  the functions to execute as values. These functions will be
  called to gather information on the config. The hashmap should
  contain some subset of the following keywords:

  ```clojure
  :get-hostname (fn [] ...)
  ```
     return a string specifying the hostname in this config.

  ```clojure
  :get-user (fn [] ...)
  ```
     return a string specifying the username in this config.

  ```clojure
  :get-port (fn [] ...)
  ```
     return a number specifying the port in this config.

  ```clojure
  :get-value (fn [key] ...)
  ```
     return the string value in the config for the specified key.

  ```clojure
  :get-values (fn [] ...)
  ```
     return a vector of srtings in this config for the specified
  key."
  [callbacks]
  #?(:bb (config/new callbacks)
     :clj (proxy [ConfigRepository$Config] []
            (getHostname []
              ((:get-hostname callbacks)))
            (getUser []
              ((:get-user callbacks)))
            (getPort []
              ((:get-port callbacks)))
            (getValue [key]
              ((:get-value callbacks) key))
            (getValues [key]
              (into-array
               String
               ((:get-values callbacks) key))))))

(defn get-hostname
  "return the hostname from a config object"
  [config]
  #?(:bb (config/get-hostname config)
     :clj (.getHostname ^ConfigRepository$Config config)))

(defn get-user
  "return the username for a config object"
  [config]
  #?(:bb (config/get-user config)
     :clj (.getUser ^ConfigRepository$Config config)))

(defn get-port
  "return the port for a config object"
  [config]
  #?(:bb (config/get-port config)
     :clj (.getPort ^ConfigRepository$Config config)))

(defn get-value
  "return the string setting for `key` from a config object"
  [config key]
  #?(:bb (config/get-value config key)
     :clj (.getValue ^ConfigRepository$Config config ^String key)))

(defn get-values
  "return a vector of strings that is set for the value `key`
  from a config object"
  [config key]
  #?(:bb (config/get-values config key)
     :clj (vec (.getValues ^ConfigRepository$Config config ^String key))))

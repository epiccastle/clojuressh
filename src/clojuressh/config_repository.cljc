(ns clojuressh.config-repository
  (:require [clojuressh.impl.load-pod]
            #?(:bb [pod.epiccastle.bbssh.config-repository :as config-repository]))
  #?(:bb (:import)
     :clj (:import [com.jcraft.jsch ConfigRepository OpenSSHConfig]
                   [java.util Vector])))

(set! *warn-on-reflection* true)

(defn new
  "Create a new config-repository instance. Pass in a hashmap containing
  the function to execute as values. The hashmap should contain one
  keyword and value:

  ```clojure
  :get-config (fn [hostname] ...)
  ```
     return a config object to be used for the specified hostname.
  "
  [callbacks]
  #?(:bb (config-repository/new callbacks)
     :clj (proxy [ConfigRepository] []
            (getConfig [hostname]
              ((:get-config callbacks) hostname)))))

(defn get-config
  "return the config reference for the specified `hostname` in
  the `config-repository`"
  [config-repository hostname]
  #?(:bb (config-repository/get-config config-repository hostname)
     :clj (.getConfig ^ConfigRepository config-repository ^String hostname)))

(defn openssh-config-file
  "Create an OpenSSH config-repository from a file. `config-file`
  can be a string (supports tilde expansion of home directory)
  or a java.io.File instance."
  [config-file]
  #?(:bb (config-repository/openssh-config-file config-file)
     :clj (OpenSSHConfig/parseFile ^String config-file)))

(defn openssh-config-string
  "Create and OpenSSH config-repository from a data string."
  [config]
  #?(:bb (config-repository/openssh-config-string config)
     :clj (OpenSSHConfig/parse ^String config)))

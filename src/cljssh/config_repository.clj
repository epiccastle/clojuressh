(ns cljssh.config-repository
  (:import [com.jcraft.jsch ConfigRepository OpenSSHConfig]
           [java.util Vector]))

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
  [reply-fn]
  (let [result
        (proxy [ConfigRepository] []
          (getConfig [hostname]
            (callbacks/call-method reply-fn :get-config [hostname])))]
    (cleaner/register-delete-fn result #(reply-fn [:done] ["done"]))
    (reply-fn [:result result])
    nil))

(defn get-config
  "return the config reference for the specified `hostname` in
  the `config-repository`"
  [^ConfigRepository config-repository ^String hostname]
  (.getConfig config-repository hostname))

(defn openssh-config-file
  "Create an OpenSSH config-repository from a file. `config-file`
  can be a string (supports tilde expansion of home directory)
  or a java.io.File instance."
  [config-file]
  (OpenSSHConfig/parseFile config-file))

(defn openssh-config-string
  "Create and OpenSSH config-repository from a data string."
  [config]
  (OpenSSHConfig/parse config))

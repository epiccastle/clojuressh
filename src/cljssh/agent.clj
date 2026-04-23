(ns cljssh.agent
  (:require [clojure.java.io :as io])
  (:import [com.jcraft.jsch JSch Logger
            IdentityRepository HostKeyRepository
            ConfigRepository Identity]
           [java.io InputStream])
  )

(set! *warn-on-reflection* true)

(defn new
  "Make a new JSch agent. A JSch agent is not an \"ssh agent\".
  It is the base java class that holds and controls the
  sessions."
  []
  (JSch.))

(defn get-session
  "Construct a new JSch connection session. Does not start the ssh
  connection.
  "
  ([^JSch agent ^String host]
   (.getSession agent host))
  ([^JSch agent ^String username ^String host]
   (.getSession agent username host))
  ([^JSch agent ^String username ^String host ^int port]
   (.getSession agent username host port)))

(defn get-identity-repository
  "Get the current identity-repository from the agent."
  [^JSch agent]
  (.getIdentityRepository agent))

(defn set-identity-repository
  "Set the identity-repository the agent should use."
  [^JSch agent ^IdentityRepository identity-repository]
  (.setIdentityRepository agent identity-repository))

(defn get-config-repository
  "Get the current config-repository from the agent."
  [^JSch agent]
  (.getConfigRepository agent))

(defn set-config-repository
  "Set the config-repository the agent should use."
  [^JSch agent ^ConfigRepository config-repository]
  (.setConfigRepository agent config-repository))

(defn get-host-key-repository
  "Get the current host-key-repository from the agent."
  [^JSch agent]
  (.getHostKeyRepository agent))

(defn set-host-key-repository
  "Set the host-key-repository the agent should use."
  [^JSch agent ^HostKeyRepository host-key-repository]
  (.setHostKeyRepository agent host-key-repository))

(defn set-known-hosts
  "Set the known hosts file location"
  [^JSch agent ^String filename]
  (.setKnownHosts agent filename))

(defn set-known-hosts-content
  "Set the known hosts file location"
  [^JSch agent content]
  (.setKnownHosts
   agent
   ^InputStream (io/input-stream content)))

(defn add-identity
  "Add the private key to be used in authentication. Optionally
  add the public key aswell. Private key can be decrypted with passphrase."
  ([^JSch agent ^String filename]
   (.addIdentity agent filename))
  ([^JSch agent ^String filename ^String passphrase]
   (.addIdentity agent filename passphrase))
  ([^JSch agent ^String private-key-filename ^String public-key-filename passphrase]
   (.addIdentity
    agent
    private-key-filename
    public-key-filename
    ^bytes passphrase))
  ([^JSch agent ^String identity-name private-key public-key passphrase]
   (.addIdentity
    agent
    identity-name
    ^bytes private-key
    ^bytes public-key
    ^bytes passphrase)))

(defn add-identity2
  [^JSch agent ^String filename passphrase]
  (.addIdentity
   agent
   filename
   ^bytes passphrase))

(defn add-identity3
  [^JSch agent ^Identity identity passphrase]
  (.addIdentity
   agent
   identity
   ^bytes passphrase))

(defn remove-identity
  "remove an identity by its name or its reference"
  [^JSch agent ^String identity-name]
  (.removeIdentity agent identity-name))

(defn remove-identity2
  "remove an identity by its name or its reference"
  [^JSch agent ^Identity identity]
  (.removeIdentity agent identity))

(defn get-identity-names
  "Lists names of identities included in the identity-repository"
  [agent]
  (into []
        (.getIdentityNames
         ^JSch agent)))

(defn remove-all-identities
  "Removes all identities from the identity-repository."
  [^JSch agent]
  (.removeAllIdentity agent))

(defn get-config
  "Returns the config value for the specified key"
  [^String key]
  (JSch/getConfig key))

(defn set-config
  "Sets or overrides the configuration."
  ([hashmap]
   (doseq [[key value] hashmap]
     (JSch/setConfig
      ^String key
      ^String value)))
  ([^String key ^String value]
   (JSch/setConfig key value)))

(defn set-debug-fn [reply-fn]
  (JSch/setLogger
   (proxy [Logger] []
     (isEnabled [_]
       true)
     (log [level msg]
       (reply-fn [level msg])))))

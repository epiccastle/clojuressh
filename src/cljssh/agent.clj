(ns cljssh.agent
  (:require [clojure.java.io :as io])
  (:import [com.jcraft.jsch JSch Logger
            IdentityRepository HostKeyRepository
            ConfigRepository Identity]
           [java.io InputStream])
  )

;; cljssh.* are invoked on pod side.

(set! *warn-on-reflection* true)

(defn new []
  (JSch.))

(defn get-session
  ([^JSch agent ^String host]
   (.getSession agent host))
  ([^JSch agent ^String username ^String host]
   (.getSession agent username host))
  ([^JSch agent ^String username ^String host ^int port]
   (.getSession agent username host port)))

(defn get-identity-repository
  [^JSch agent]
  (.getIdentityRepository agent))

(defn set-identity-repository
  [^JSch agent ^IdentityRepository identity-repository]
  (.setIdentityRepository agent identity-repository))

(defn get-config-repository
  [^JSch agent]
  (.getConfigRepository agent))

(defn set-config-repository
  [^JSch agent ^ConfigRepository config-repository]
  (.setConfigRepository agent config-repository))

(defn get-host-key-repository
  [^JSch agent]
  (.getHostKeyRepository agent))

(defn set-host-key-repository
  [^JSch agent ^HostKeyRepository host-key-repository]
  (.setHostKeyRepository agent host-key-repository))

(defn set-known-hosts
  [^JSch agent ^String filename]
  (.setKnownHosts agent filename))

(defn set-known-hosts-content
  [^JSch agent content]
  (.setKnownHosts
   agent
   ^InputStream (io/input-stream (utils/decode-base64 content))))

(defn add-identity
  ([^JSch agent ^String filename]
   (.addIdentity agent filename))
  ([^JSch agent ^String filename ^String passphrase]
   (.addIdentity agent filename passphrase))
  ([^JSch agent ^String private-key-filename ^String public-key-filename passphrase]
   (.addIdentity
    agent
    private-key-filename
    public-key-filename
    ^bytes (utils/decode-base64 passphrase)))
  ([^JSch agent ^String identity-name private-key public-key passphrase]
   (.addIdentity
    agent
    identity-name
    ^bytes (utils/decode-base64 private-key)
    ^bytes (utils/decode-base64 public-key)
    ^bytes (utils/decode-base64 passphrase))))

(defn add-identity2
  [^JSch agent ^String filename passphrase]
  (.addIdentity
   agent
   filename
   ^bytes (utils/decode-base64 passphrase)))

(defn add-identity3
  [^JSch agent ^Identity identity passphrase]
  (.addIdentity
   agent
   identity
   ^bytes (utils/decode-base64 passphrase)))

(defn remove-identity
  [^JSch agent ^String identity-name]
  (.removeIdentity agent identity-name))

(defn remove-identity2
  [^JSch agent ^Identity identity]
  (.removeIdentity agent identity))

(defn get-identity-names
  [agent]
  (into []
        (.getIdentityNames
         ^JSch agent)))

(defn remove-all-identities
  [^JSch agent]
  (.removeAllIdentity agent))

(defn get-config
  [^String key]
  (JSch/getConfig key))

(defn set-config
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

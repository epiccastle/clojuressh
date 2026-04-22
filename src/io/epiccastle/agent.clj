(ns io.epiccastle.agent
  (:require [clojure.java.io :as io])
  (:import [com.jcraft.jsch JSch Logger
            IdentityRepository HostKeyRepository
            ConfigRepository Identity]
           [java.io InputStream])
  )

;; io.epiccastle.* are invoked on pod side.

(set! *warn-on-reflection* true)

(defn new []
  (JSch.))

(defn get-session
  ([agent host]
   (.getSession
    ^JSch agent
    ^String host))
  ([agent username host]
   (.getSession
    ^JSch agent
    ^String username
    ^String host))
  ([agent username host port]
   (.getSession
    ^JSch agent
    ^String username
    ^String host
    ^int port)))

(defn get-identity-repository
  [agent]
  (.getIdentityRepository
   ^JSch agent))

(defn set-identity-repository
  [agent identity-repository]
  (.setIdentityRepository
   ^JSch agent
   ^IdentityRepository identity-repository))

(defn get-config-repository
  [agent]
  (.getConfigRepository
   ^JSch agent))

(defn set-config-repository
  [agent config-repository]
  (.setConfigRepository
   ^JSch agent
   ^ConfigRepository config-repository))

(defn get-host-key-repository
  [agent]
  (.getHostKeyRepository
   ^JSch agent))

(defn set-host-key-repository
  [agent host-key-repository]
  (.setHostKeyRepository
   ^JSch agent
   ^HostKeyRepository host-key-repository))

(defn set-known-hosts
  [agent filename]
  (.setKnownHosts
   ^JSch agent
   ^String filename))

(defn set-known-hosts-content
  [agent content]
  (.setKnownHosts
   ^JSch agent
   ^InputStream (io/input-stream (utils/decode-base64 content))))

(defn add-identity
  ([agent filename]
   (.addIdentity
    ^JSch agent
    ^String filename))
  ([agent filename passphrase]
   (.addIdentity
    ^JSch agent
    ^String filename
    ^String passphrase))
  ([agent private-key-filename public-key-filename passphrase]
   (.addIdentity
    ^JSch agent
    ^String private-key-filename
    ^String public-key-filename
    ^bytes (utils/decode-base64 passphrase)))
  ([agent identity-name private-key public-key passphrase]
   (.addIdentity
    ^JSch agent
    ^String identity-name
    ^bytes (utils/decode-base64 private-key)
    ^bytes (utils/decode-base64 public-key)
    ^bytes (utils/decode-base64 passphrase))))

(defn add-identity2
  [agent filename passphrase]
  (.addIdentity
   ^JSch agent
   ^String filename
   ^bytes (utils/decode-base64 passphrase)))

(defn add-identity3
  [agent identity passphrase]
  (.addIdentity
   ^JSch agent
   ^Identity identity
   ^bytes (utils/decode-base64 passphrase)))

(defn remove-identity
  [agent identity-name]
  (.removeIdentity
   ^JSch agent
   ^String identity-name))

(defn remove-identity2
  [agent identity]
  (.removeIdentity
   ^JSch agent
   ^Identity identity))

(defn get-identity-names
  [agent]
  (into []
        (.getIdentityNames
         ^JSch agent)))

(defn remove-all-identities
  [agent]
  (.removeAllIdentity
   ^JSch agent))

(defn get-config
  [key]
  (JSch/getConfig ^String key))

(defn set-config
  ([hashmap]
   (doseq [[key value] hashmap]
     (JSch/setConfig
      ^String key
      ^String value)))
  ([key value]
   (JSch/setConfig
    ^String key
    ^String value)))

(defn ^:async set-debug-fn [reply-fn]
  (JSch/setLogger
   (proxy [Logger] []
     (isEnabled [_]
       true)
     (log [level msg]
       (reply-fn [level msg])))))

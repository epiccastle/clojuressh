(ns io.epiccastle.cljssh.host-key-repository
  (:refer-clojure :exclude [remove])
  (:require [io.epiccastle.cljssh.callbacks :as callbacks]
            [io.epiccastle.cljssh.cleaner :as cleaner])
  (:import [com.jcraft.jsch HostKeyRepository HostKey UserInfo]))

;; io.epiccastle.cljssh.* are invoked on pod side.

(set! *warn-on-reflection* true)

(defn ^:async new [reply-fn]
  (let [result
        (proxy [HostKeyRepository] []
          (check [^String host ^bytes public-key]
            ({:ok 0
              :not-included 1
              :changed 2}
             (callbacks/call-method
              reply-fn :check
              [host (utils/encode-base64 public-key)])))
          (add [^HostKey host-key ^UserInfo user-info]
            (callbacks/call-method
             reply-fn :add
             [host-key
              user-info]))
          (remove
            ([^String host ^String type]
             (callbacks/call-method
              reply-fn :remove
              [host type]))
            ([^String host ^String type ^bytes public-key]
             (callbacks/call-method
              reply-fn :remove
              [host type (some-> public-key utils/encode-base64)])))
          (getKnownHostsRepositoryID []
            (callbacks/call-method reply-fn :get-known-hosts-repository-id []))
          (getHostKey
            ([]
             (->>
              (callbacks/call-method reply-fn :get-host-key [])
              (into-array HostKey)))
            ([^String host ^String type]
             (->>
              (callbacks/call-method reply-fn :get-host-key [host type])
              (into-array HostKey)))))]
    (cleaner/register-delete-fn result #(reply-fn [:done] ["done"]))
    (reply-fn [:result result])
    nil))

(defn check
  [host-key-repository host key]
  ({0 :ok
    1 :not-included
    2 :changed}
   (.check
    ^HostKeyRepository host-key-repository
    ^String host
    (utils/decode-base64 key))))

(defn add
  [^HostKeyRepository host-key-repository ^HostKey host-key ^UserInfo user-info]
  (.add host-key-repository host-key user-info))

(defn remove
  ([^HostKeyRepository host-key-repository ^String host ^String type]
   (.remove host-key-repository host type))
  ([^HostKeyRepository host-key-repository ^String host ^String type key]
   (.remove host-key-repository host type ^bytes (utils/decode-base64 key))))

(defn get-host-key
  ([^HostKeyRepository host-key-repository]
   (.getHostKey host-key-repository))
  ([^HostKeyRepository host-key-repository ^String host ^String type]
   (.getHostKey host-key-repository host type)))

(defn get-known-hosts-repository-id [^HostKeyRepository host-key-repository]
  (.getKnownHostsRepositoryID host-key-repository))

(ns clojuressh.host-key-repository
  (:refer-clojure :exclude [remove])
  (:require [clojuressh.impl.load-pod]
            #?(:bb [pod.epiccastle.bbssh.host-key-repository :as host-key-repository]))
  #?(:bb (:import)
     :clj (:import [com.jcraft.jsch HostKeyRepository HostKey UserInfo])))

(set! *warn-on-reflection* true)

(defn new
  "Create a new host-key-repository. Pass in a hashmap containing
  callbacks to be used for the methods of the repository. The hashmap
  keys are optional. If a value is not specified a default is used.
  The keys are as follows:

  ```clojure
  :check (fn [^String host ^bytes public-key] ...)
  ```

    Check the repository for the presence of the passed in public
    key under the specified hostname. Hosts should be able to
    have multiple keys of different key types. Thus you should
    examine the key type and check that hosts keys for the same
    type. Your function should return `:ok` if the key is present
    and matches, `:not-included` if no matching key is found and
    `:changed` if there is a conflicting key of that type stored
    against the hostname.

  ```clojure
  :add (fn [^Keyword host-key ^Keyword user-info] ...)
  ```

    Add the referenced `host-key` into the repository. If the key
    requires user interaction use the passed in `user-info` to
    do so.

  ```clojure
  :remove (fn
            ([^String host ^String type] ..)
            ([^String host ^String type ^bytes public-key] ..)
  ```

    Remove the referenced `public-key` of `type` being stored
    for `host`. If `public-key` is not passed, remove all keys
    of `type` for `host`. `type` will be one of SSH public key
    prefix strings `\"ssh-rsa\"`, `\"ssh-dss\"`,
    `\"ecdsa-sha2-nistp256\"` or `\"ssh-ed25519\"`. Other key
    types may be added with later releases.

  ```clojure
  :get-known-hosts-repository-id (fn [] ...)
  ```

    Return a string to refer to the known hosts repository.

  ```clojure
  :get-host-key (fn
                  ([] ...)
                  ([^String host ^String type]))
  ```

    When called with a `host` and `type`, return a vector of
    `host-key` references. When called with no args, return a
    vector of all `host-key` references.

  "
  [callbacks]
  #?(:bb (host-key-repository/new callbacks)
     :clj (proxy [HostKeyRepository] []
            (check [^String host ^bytes public-key]
              ({:ok 0
                :not-included 1
                :changed 2}
               ((:check callbacks) host public-key)))
            (add [^HostKey host-key ^UserInfo user-info]
              ((:add callbacks) host-key user-info))
            (remove
              ([^String host ^String type]
               ((:remove callbacks) host type))
              ([^String host ^String type ^bytes public-key]
               ((:remove callbacks) host type public-key)))
            (getKnownHostsRepositoryID []
              ((:get-known-hosts-repository-id callbacks)))
            (getHostKey
              ([]
               (->>
                ((:get-host-key callbacks))
                (into-array HostKey)))
              ([^String host ^String type]
               (->>
                ((:get-host-key callbacks) host type)
                (into-array HostKey)))))))

(defn check
  "Checks the repository for the presence of the passed in public key
  under the specified hostname. Returns `:ok` if the key is present
  and matches, `:not-included` if no matching key is found and
  `:changed` if there is a conflicting key of that type stored against
  the hostname."
  [host-key-repository host key]
  #?(:bb (host-key-repository/check host-key-repository host key)
     :clj ({0 :ok
            1 :not-included
            2 :changed}
           (.check
            ^HostKeyRepository host-key-repository
            ^String host
            ^bytes key))))

(defn add
  "Add the referenced `host-key` into the repository. If the key
  requires user interaction use the passed in `user-info` to do so."
  [host-key-repository host-key user-info]
  #?(:bb (host-key-repository/add host-key-repository host-key user-info)
     :clj (.add ^HostKeyRepository host-key-repository ^HostKey host-key ^UserInfo user-info)))

(defn remove
  "Remove the referenced `public-key` of `type` being stored for
  `host`. If `public-key` is not passed, remove all keys of `type` for
  `host`. `type` should be a SSH public key prefix
  string. `public-key` should be a `byte-array` of raw data."
  ([host-key-repository host type]
   #?(:bb (host-key-repository/remove host-key-repository host type)
      :clj (.remove ^HostKeyRepository host-key-repository ^String host ^String type)))
  ([host-key-repository host type key]
   #?(:bb (host-key-repository/remove host-key-repository host type key)
      :clj (.remove ^HostKeyRepository host-key-repository ^String host ^String type ^bytes key))))

(defn get-host-key
  "Get a vector of `host-key` references from the repository.
  If passed a `host` and `type` then only return keys matching
  these. Otherwise return all the keys."
  ([host-key-repository]
   #?(:bb (host-key-repository/get-host-key host-key-repository)
      :clj (.getHostKey ^HostKeyRepository host-key-repository)))
  ([host-key-repository host type]
   #?(:bb (host-key-repository/get-host-key host-key-repository host type)
      :clj (.getHostKey ^HostKeyRepository host-key-repository ^String host ^String type))))

(defn get-known-hosts-repository-id
  "Returns and identification string for this repository."
  [host-key-repository]
  #?(:bb (host-key-repository/get-known-hosts-repository-id host-key-repository)
     :clj (.getKnownHostsRepositoryID ^HostKeyRepository host-key-repository)))

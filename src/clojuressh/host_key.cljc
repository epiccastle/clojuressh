(ns clojuressh.host-key
  #?(:bb (:require [babashka.pods :as pods]))
  #?(:bb (:import)
     :clj (:import [com.jcraft.jsch HostKey JSch])))

#?(:bb (pods/load-pod 'epiccastle/bbssh "0.7.0"))
#?(:bb (require '[pod.epiccastle.bbssh.host-key :as host-key]))

(def types
  {:unknown HostKey/UNKNOWN
   :guess HostKey/GUESS
   :sshdss HostKey/SSHDSS
   :sshrsa HostKey/SSHRSA
   :ecdsa256 HostKey/ECDSA256
   :ecdsa384 HostKey/ECDSA384
   :ecdsa521 HostKey/ECDSA521
   :ed25519 HostKey/ED25519
   :ed448 HostKey/ED448
   })

(defn new
  "Create a new host-key with some subset of:

  - `host`: A string of the hostname.
  - `key`: A byte array of the public key.
  - `type`: One of `:unknown`, `:guess`, `:sshdss`, `:sshrsa`,
          `:ecdsa256`, `:ecdsa384`, `:ecdsa521`, `:ed25519` or `:ed448`.
          If uknown value passed, defaults to `:guess` which tries to
          guess the key type.
  - `comment`: A string comment for the key.
  - `marker`: A string marker for the key.
  "
  ([host key]
   #?(:bb (host-key/new host key)
      :clj (HostKey. ^String host ^bytes key)))
  ([host type key]
   #?(:bb (host-key/new host type key)
      :clj (HostKey. ^String host (types type HostKey/GUESS) ^bytes key)))
  ([host type key comment]
   #?(:bb (host-key/new host type key comment)
      :clj (HostKey. ^String host (types type HostKey/GUESS) ^bytes key ^String comment)))
  ([marker host type key comment]
   #?(:bb (host-key/new marker host type key comment)
      :clj (HostKey. ^String marker ^String host (types type HostKey/GUESS) ^bytes key ^String comment))))

(defn get-host
  "Returns the hostname for the host-key."
  [host-key]
  #?(:bb (host-key/get-host host-key)
     :clj (.getHost ^HostKey host-key)))

(defn get-type
  "Returns the key type string for the host-key.
  `\"ssh-rsa\"`, `\"ssh-dss\"`, `\"ecdsa-sha2-nistp256\"` or
  `\"ssh-ed25519\"`.
  "
  [host-key]
  #?(:bb (host-key/get-type host-key)
     :clj (.getType ^HostKey host-key)))

(defn get-key
  "Returns the key as a base64 encoded string."
  [host-key]
  #?(:bb (host-key/get-key host-key)
     :clj (.getKey ^HostKey host-key)))

(defn get-finger-print
  "Returns the fingerprint of the key."
  [host-key agent]
  #?(:bb (host-key/get-finger-print host-key agent)
     :clj (.getFingerPrint ^HostKey host-key ^JSch agent)))

(defn get-comment
  "Returns the comment associated with the key."
  [host-key]
  #?(:bb (host-key/get-comment host-key)
     :clj (.getComment ^HostKey host-key)))

(defn get-marker
  "Returns any @ marker associated with the key. If no marker is
  associated returns the empty string."
  [host-key]
  #?(:bb (host-key/get-marker host-key)
     :clj (.getMarker ^HostKey host-key)))

(defn get-info
  "Returns all the associated information of the key as a single hashmap
  with keys `:host`, `:type`, `:key`, `:finger-print`, `:comment` and
  `:marker`."
  [host-key agent]
  #?(:bb (host-key/get-info host-key agent)
     :clj (let [host-key ^HostKey host-key
                agent ^JSch agent]
            {:host (.getHost host-key)
             :type (.getType host-key)
             :key (.getKey host-key)
             :finger-print (.getFingerPrint host-key agent)
             :comment (.getComment host-key)
             :marker (.getMarker host-key)})))

(defn get-infos
  "Given a sequence of host-key references and the clojuressh agent
  reference, return a hashmap of all the info for all the keys. The
  keys are the host-key references and the values are as would be
  returned from `get-info`."
  [host-keys agent]
  #?(:bb (host-key/get-infos host-keys agent)
     :clj (into
           {}
           (for [host-key host-keys]
             (let [instance ^HostKey host-key
                   agent ^JSch agent]
               [host-key
                {:host (.getHost instance)
                 :type (.getType instance)
                 :key (.getKey instance)
                 :finger-print (.getFingerPrint instance agent)
                 :comment (.getComment instance)
                 :marker (.getMarker instance)}])))))

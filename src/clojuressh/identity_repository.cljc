(ns clojuressh.identity-repository
  (:require [clojuressh.impl.load-pod]
            #?(:bb [pod.epiccastle.bbssh.identity-repository :as identity-repository]))
  #?(:bb (:import)
     :clj (:import [com.jcraft.jsch IdentityRepository]
                   [java.util Vector])))

(set! *warn-on-reflection* true)

(defn new
  "Create a new identity-repository. Pass in a hashmap containing
  the functions to execute as values. These functions will be called
  by the internal ssh engine. The hashmap should contain some subset
  of the the following keywords:

  ```clojure
  :get-name (fn [] ...)
  ```
    return a string specifying the name of this repository

  ```clojure
  :get-status (fn [] ...)
  ```
    return the present status of this repository. Can be :unavailable,
    :not-running or :running

  ```clojure
  :get-identities (fn [] ...)
  ```
    return a sequence of the identities stored in this repository

  ```clojure
  :add (fn [^bytes identity-data] ...)
  ```
    add the passed in raw data as an identity

  ```clojure
  :remove (fn [^bytes identity-data] ...)
  ```
    remove the passed in raw data identity from the repository.

  ```clojure
  :removeAll (fn [] ...)
  ```
    empty the repository

  "
  [callbacks]
  #?(:bb (identity-repository/new callbacks)
     :clj (proxy [IdentityRepository] []
            (getName []
              ((:get-name callbacks)))
            (getStatus []
              ((:get-status callbacks)))
            (getIdentities []
              (Vector. ^java.util.Collection ((:get-identities callbacks))))
            (add [^bytes identity-data]
              ((:add callbacks) identity-data))
            (remove [^bytes blob]
              ((:remove callbacks) blob))
            (removeAll []
              ((:remove-all callbacks))))))

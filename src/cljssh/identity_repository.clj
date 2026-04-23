(ns cljssh.identity-repository
  (:require [cljssh.callbacks :as callbacks]
            [cljssh.cleaner :as cleaner])
  (:import [com.jcraft.jsch IdentityRepository]
           [java.util Vector]))

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
  [reply-fn]
  (let [result
        (proxy [IdentityRepository] []
          (getName []
            (callbacks/call-method reply-fn :get-name []))
          (getStatus []
            (callbacks/call-method reply-fn :get-status []))
          (getIdentities []
            (->> (callbacks/call-method reply-fn :get-identities [])
                 Vector.))
          (add [^bytes identity-data]
            (callbacks/call-method
             reply-fn :add
             [identity-data]))
          (remove [^bytes blob]
            (callbacks/call-method
             reply-fn :remove
             [blob]))
          (removeAll []
            (callbacks/call-method reply-fn :remove-all [])))]
    (cleaner/register-delete-fn result #(reply-fn [:done] ["done"]))
    (reply-fn [:result result])
    nil))

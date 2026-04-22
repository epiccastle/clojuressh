(ns cljssh.identity-repository
  (:require [cljssh.callbacks :as callbacks]
            [cljssh.cleaner :as cleaner])
  (:import [com.jcraft.jsch IdentityRepository]
           [java.util Vector]))

(set! *warn-on-reflection* true)

(defn new [reply-fn]
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

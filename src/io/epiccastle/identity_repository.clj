(ns io.epiccastle.identity-repository
  (:require [io.epiccastle.callbacks :as callbacks]
            [io.epiccastle.cleaner :as cleaner])
  (:import [com.jcraft.jsch IdentityRepository]
           [java.util Vector]))

;; io.epiccastle.* are invoked on pod side.

(set! *warn-on-reflection* true)

(defn ^:async new [reply-fn]
  (let [result
        (proxy [IdentityRepository] []
          (getName []
            (callbacks/call-method reply-fn :get-name []))
          (getStatus []
            (callbacks/call-method reply-fn :get-status []))
          (getIdentities []
            (->> (callbacks/call-method reply-fn :get-identities [])
                 (mapv references/get-instance)
                 Vector.))
          (add [^bytes identity-data]
            (callbacks/call-method
             reply-fn :add
             [(utils/encode-base64 identity-data)]))
          (remove [^bytes blob]
            (callbacks/call-method
             reply-fn :remove
             [(utils/encode-base64 blob)]))
          (removeAll []
            (callbacks/call-method reply-fn :remove-all [])))]
    (cleaner/register-delete-fn result #(reply-fn [:done] ["done"]))
    (reply-fn [:result result])
    nil))

(ns cljssh.identity
  (:require [cljssh.callbacks :as callbacks]
            [cljssh.cleaner :as cleaner])
  (:import [com.jcraft.jsch Identity]))

(set! *warn-on-reflection* true)

(defn new [reply-fn]
  (let [result
        (proxy [Identity] []
          (setPassphrase [^bytes passphrase]
            (callbacks/call-method
             reply-fn :set-passphrase
             [passphrase]))
          (getPublicKeyBlob []
            (callbacks/call-method reply-fn :get-public-key-blob []))
          (getSignature
            ([^bytes data]
             (callbacks/call-method
              reply-fn :get-signature
              [data]))
            ([^bytes data ^String alg]
             (callbacks/call-method
              reply-fn :get-signature
              [data alg])))
          ;; deprecated in JSch
          #_(decrypt []
            (callbacks/call-method reply-fn :decrypt []))
          (getAlgName []
            (callbacks/call-method reply-fn :get-alg-name []))
          (getName []
            (callbacks/call-method reply-fn :get-name []))
          (isEncrypted []
            (boolean
             (callbacks/call-method reply-fn :is-encrypted [])))
          (clear []
            (callbacks/call-method reply-fn :clear [])))]
    (cleaner/register-delete-fn result #(reply-fn [:done] ["done"]))
    (reply-fn [:result result])
    nil))

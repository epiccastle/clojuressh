(ns io.epiccastle.cljssh.identity
  (:require [io.epiccastle.cljssh.callbacks :as callbacks]
            [io.epiccastle.cljssh.cleaner :as cleaner])
  (:import [com.jcraft.jsch Identity]))

;; io.epiccastle.cljssh.* are invoked on pod side.

(set! *warn-on-reflection* true)

(defn ^:async new [reply-fn]
  (let [result
        (proxy [Identity] []
          (setPassphrase [^bytes passphrase]
            (callbacks/call-method
             reply-fn :set-passphrase
             [(some-> passphrase utils/encode-base64)]))
          (getPublicKeyBlob []
            (utils/decode-base64
             (callbacks/call-method reply-fn :get-public-key-blob [])))
          (getSignature
            ([^bytes data]
             (utils/decode-base64
              (callbacks/call-method
               reply-fn :get-signature
               [(some-> data utils/encode-base64)])))
            ([^bytes data ^String alg]
             (utils/decode-base64
              (callbacks/call-method
               reply-fn :get-signature
               [(some-> data utils/encode-base64) alg]))))
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

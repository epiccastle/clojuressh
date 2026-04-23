(ns cljssh.identity
  (:require [cljssh.callbacks :as callbacks]
            [cljssh.cleaner :as cleaner])
  (:import [com.jcraft.jsch Identity]))

(set! *warn-on-reflection* true)

(defn new
  "Create a new identity. Pass in a hashmap containing the
  functions to execute as values. These functions will be called
  by the internal ssh engine. The hashmap should contain some
  subset of the following keywords:

  ```clojure
  :set-passphrase (fn [^bytes passphrase] ...)
  ```

    Called when the system wants to try to decrypt this identity
    with the passed in passphrase.

  ```clojure
  :get-public-key-blob (fn [] ...)
  ```

    Return a byte-array of the identity's public key.

  ```clojure
  :get-signature (fn ([^bytes data] ...)
                     ([^bytes data ^String algorithm))
  ```

    Sign the incoming data (with algorithm) and return a byte-array
    of the signature

  ```clojure
  :get-alg-name (fn [] ...)
  ```

    Return a string containing the identity's algorithm name. for example
    \"ssh-rsa\" or \"ssh-dss\"

  ```clojure
  :get-name (fn [] ...)
  ```

    Return a string name for this identity

  ```clojure
  :is-encrypted (fn [] ...)
  ```

    Return a truthy value if this identity is encrypted

  ```clojure
  :clear (fn [] ...)
  ```
    Erase all the memory associated with this identity as the system
    has finished using it.

  "
  [reply-fn]
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

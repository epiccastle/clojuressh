(ns io.epiccastle.cljssh.user-info
  (:require [io.epiccastle.cljssh.callbacks :as callbacks]
            [io.epiccastle.cljssh.cleaner :as cleaner])
  (:import [com.jcraft.jsch UserInfo]))

;; io.epiccastle.cljssh.* are invoked on pod side.

(set! *warn-on-reflection* true)

(defn ^:async new [reply-fn]
  (let [result
        (proxy [UserInfo] []
          (getPassword []
            (callbacks/call-method reply-fn :get-password []))
          (promptYesNo [^String s]
            (boolean
             (callbacks/call-method reply-fn :prompt-yes-no [s])))
          (getPassphrase []
            (callbacks/call-method reply-fn :get-passphrase []))
          (promptPassphrase [^String s]
            (boolean
             (callbacks/call-method reply-fn :prompt-passphrase [s])))
          (promptPassword [^String s]
            (boolean
             (callbacks/call-method reply-fn :prompt-password [s])))
          (showMessage [^String s]
            (callbacks/call-method reply-fn :show-message [s])))]
    (cleaner/register-delete-fn result #(reply-fn [:done] ["done"]))
    (reply-fn [:result result])
    nil))

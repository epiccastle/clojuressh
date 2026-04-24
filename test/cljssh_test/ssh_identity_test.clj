(ns cljssh-test.ssh-identity-test
  (:require [cljssh.core :as cljssh]
            [cljssh.user-info :as user-info]
            [cljssh-test.docker :as docker]
            [cljssh-test.keys :as keys]
            [clojure.test :refer [is deftest]]))

(defn setup-server-client-keys [key-name]
  ;; setup ssh server key
  (docker/exec "mkdir /root/.ssh")
  (docker/exec "chmod 0700 /root/.ssh")
  (docker/put-file (get-in keys/keys [key-name :public]) "/root/.ssh/authorized_keys")

  ;; setup client private key
  (docker/run "rm -rf .test/cljssh-test-key" "could not clean .test/cljssh-test-key")
  (docker/run "mkdir -p .test/cljssh-test-key" "could not mkdir .test/cljssh-test-key")
  (docker/run "chmod 0700 .test/cljssh-test-key" "could not chmod .test/cljssh-test-key")
  (spit ".test/cljssh-test-key/cljssh_test_id_key" (get-in keys/keys [key-name :private]) )
  (docker/run "chmod 0600 .test/cljssh-test-key/cljssh_test_id_key" "could not chmod .test/cljssh-test-key/cljssh_test_id_key"))

(defn passphrase [key-name]
  (get-in keys/keys [key-name :passphrase]))

(deftest test-ssh-via-identity-no-passphrase
  (docker/cleanup)
  (docker/build {:root-password "root-access-please"})
  (docker/start {:ssh-port 9876})
  (setup-server-client-keys :rsa-nopassphrase)

  (-> (cljssh/ssh "localhost" {:port 9876
                              :username "root"
                              :identity ".test/cljssh-test-key/cljssh_test_id_key"
                              :strict-host-key-checking false})
      (cljssh/exec "echo 'running remote'" {:out :string})
      deref
      :out
      (= "running remote\n")
      is)

  (docker/cleanup))

(deftest test-ssh-via-identity-with-passphrase
  (docker/cleanup)
  (docker/build {:root-password "root-access-please"})
  (docker/start {:ssh-port 9876})
  (setup-server-client-keys :rsa-passphrase)

  (-> (cljssh/ssh "localhost" {:port 9876
                              :username "root"
                              :identity ".test/cljssh-test-key/cljssh_test_id_key"
                              :passphrase (passphrase :rsa-passphrase)
                              :strict-host-key-checking false})
      (cljssh/exec "echo 'running remote'" {:out :string})
      deref
      :out
      (= "running remote\n")
      is)

  (docker/cleanup))

(deftest test-ssh-via-identity-with-missing-passphrase-user-info-is-called
  (docker/cleanup)
  (docker/build {:root-password "root-access-please"})
  (docker/start {:ssh-port 9876})
  (setup-server-client-keys :rsa-passphrase)

  ;; ensure passphrase is asked for is key is encrypted and no passphrase given
  (let [state-asked? (atom false)]
    (-> (cljssh/ssh "localhost" {:port 9876
                                :username "root"
                                :identity ".test/cljssh-test-key/cljssh_test_id_key"
                                :strict-host-key-checking false
                                :user-info
                                (user-info/new
                                  {:prompt-passphrase (fn [_]
                                                        (reset! state-asked? true)
                                                        true)
                                   :get-passphrase #(passphrase :rsa-passphrase)})})
        (cljssh/exec "echo 'running remote'" {:out :string})
        deref
        :out
        (= "running remote\n")
        is)
    (is @state-asked?))

  ;; ensure connection fails if passphrase is asked for and wrong passphrase given
  ;; and then wrong password is given
  (->> (-> (cljssh/ssh "localhost" {:port 9876
                                   :username "root"
                                   :identity ".test/cljssh-test-key/cljssh_test_id_key"
                                   :strict-host-key-checking false
                                   :user-info
                                   (user-info/new
                                     {:get-password (fn [] "wrong password")
                                      :prompt-yes-no (fn [_] true)
                                      :prompt-password (fn [_] true)
                                      :show-message (fn [_] nil)
                                      :prompt-passphrase (fn [_] true)
                                      :get-passphrase (fn [] "wrong passphrase")})})
           (cljssh/exec "echo 'running remote'" {:out :string})
           deref)
       (thrown? clojure.lang.ExceptionInfo)
       is)

  ;; ensure connection fails if passphrase falls back to password, but password
  ;; auth is cancelled
  (->> (-> (cljssh/ssh "localhost" {:port 9876
                                   :username "root"
                                   :identity ".test/cljssh-test-key/cljssh_test_id_key"
                                   :strict-host-key-checking false
                                   :user-info
                                   (user-info/new
                                     {:get-password (fn [] "wrong password")
                                      :prompt-yes-no (fn [_] true)
                                      :prompt-password (fn [_]
                                                         ;; cancel password auth
                                                         false)
                                      :show-message (fn [_] nil)
                                      :prompt-passphrase (fn [_] true)
                                      :get-passphrase (fn [] "wrong passphrase")})})
           (cljssh/exec "echo 'running remote'" {:out :string})
           deref)
       (thrown? clojure.lang.ExceptionInfo)
       is)

  ;; ensure connection fails if passphrase decryption is denied
  (->> (-> (cljssh/ssh "localhost" {:port 9876
                                   :username "root"
                                   :identity ".test/cljssh-test-key/cljssh_test_id_key"
                                   :strict-host-key-checking false
                                   :user-info
                                   (user-info/new
                                     {:get-password (fn [] "wrong password")
                                      :prompt-yes-no (fn [_] true)
                                      :prompt-password (fn [_] true)
                                      :show-message (fn [_] nil)
                                      :prompt-passphrase (fn [_]
                                                           ;; cancel decrypting passphrase
                                                           false)
                                      :get-passphrase (fn [] "wrong passphrase")})})
           (cljssh/exec "echo 'running remote'" {:out :string})
           deref)
       (thrown? clojure.lang.ExceptionInfo)
       is)

  (docker/cleanup))

(deftest test-ssh-via-identity-ed25519-no-passphrase
  (docker/cleanup)
  (docker/build {:root-password "root-access-please"})
  (docker/start {:ssh-port 9876})
  (setup-server-client-keys :ed25519-no-passphrase)

  (-> (cljssh/ssh "localhost" {:port 9876
                              :username "root"
                              :identity ".test/cljssh-test-key/cljssh_test_id_key"
                              :strict-host-key-checking false})
      (cljssh/exec "echo 'running remote'" {:out :string})
      deref
      :out
      (= "running remote\n")
      is)

  (docker/cleanup))

(deftest test-ssh-via-identity-ed25519-with-passphrase
  (docker/cleanup)
  (docker/build {:root-password "root-access-please"})
  (docker/start {:ssh-port 9876})
  (setup-server-client-keys :ed25519-passphrase)

  (-> (cljssh/ssh "localhost" {:port 9876
                              :username "root"
                              :identity ".test/cljssh-test-key/cljssh_test_id_key"
                              :passphrase (passphrase :ed25519-passphrase)
                              :strict-host-key-checking false})
      (cljssh/exec "echo 'running remote'" {:out :string})
      deref
      :out
      (= "running remote\n")
      is)

  (docker/cleanup))

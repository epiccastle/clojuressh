(ns clojuressh-test.openssh-config-test
  (:require [clojuressh.core :as clojuressh]
            [clojuressh.agent :as agent]
            [clojuressh.session :as session]
            [clojuressh.key-pair :as key-pair]
            [clojuressh.config-repository :as config-repository]
            [clojuressh.config :as config]
            [babashka.process :as process]
            [clojuressh-test.docker :as docker]
            [clojure.test :refer [is deftest]]))

(deftest test-openssh-config
  (docker/cleanup)
  (docker/build {:root-password "root-access-please"})
  (docker/start {:ssh-port 9876})

  (let [agent (agent/new)
        config (config-repository/openssh-config-string "
Port 9876

Host docker-host
  User root
  Hostname localhost

Host *
  ConnectTime 30000
  PreferredAuthentications keyboard-interactive,password,publickey
  #ForwardAgent yes
  #StrictHostKeyChecking no
  #IdentityFile ~/.ssh/id_rsa
  #UserKnownHostsFile ~/.ssh/known_hosts
")]
    (agent/set-config-repository agent config)

    (let [session (agent/get-session agent "docker-host")]
      (session/set-password session "root-access-please")
      (session/set-config session :strict-host-key-checking false)
      (session/connect session)
      (let [{:keys [exit out]} @(clojuressh/exec session "echo test" {:out :string})]
        (is (zero? exit))
        (is (= "test\n" out)))))

  (docker/cleanup))

(deftest test-openssh-config-get
  (let [config (config-repository/openssh-config-string "
Port 9876

Host docker-host
  User root
  Hostname localhost

Host *
  ConnectTime 30000
  PreferredAuthentications keyboard-interactive,password,publickey
  IdentityFile ~/.ssh/id_rsa
  IdentityFile ~/.ssh/id_ed25519
  IdentityFile ~/.ssh/work_key
")
        host-config (config-repository/get-config config "docker-host")]

    (is (= "localhost" (config/get-hostname host-config)))
    (is (= "root" (config/get-user host-config)))
    (is (= 9876 (config/get-port host-config)))
    (is (= "keyboard-interactive,password,publickey"
           (config/get-value host-config "PreferredAuthentications")))
    (is (= ["~/.ssh/id_rsa" "~/.ssh/id_ed25519" "~/.ssh/work_key"]
           (config/get-values host-config "IdentityFile")))))

(deftest test-openssh-config-file
  (docker/cleanup)
  (docker/build {:root-password "root-access-please"})
  (docker/start {:ssh-port 9876})

  (spit "/tmp/clojuressh-config" "
Port 9876

Host docker-host
  User root
  Hostname localhost

Host *
  ConnectTime 30000
  PreferredAuthentications keyboard-interactive,password,publickey
  #ForwardAgent yes
  #StrictHostKeyChecking no
  #IdentityFile ~/.ssh/id_rsa
  #UserKnownHostsFile ~/.ssh/known_hosts
")

  (let [agent (agent/new)
        config (config-repository/openssh-config-file "/tmp/clojuressh-config")]
    (agent/set-config-repository agent config)

    (let [session (agent/get-session agent "docker-host")]
      (session/set-password session "root-access-please")
      (session/set-config session :strict-host-key-checking false)
      (session/connect session)
      (let [{:keys [exit out]} @(clojuressh/exec session "echo test" {:out :string})]
        (is (zero? exit))
        (is (= "test\n" out)))))

  (docker/cleanup))

(deftest test-openssh-config-key-auth-ecdsa
  (docker/cleanup)
  (docker/build {:root-password "root-access-please"})
  (docker/start {:ssh-port 9876})

  (let [agent (agent/new)
        keypair (key-pair/generate agent :ecdsa 256)]
    (key-pair/write-private-key keypair "/tmp/clojuressh_id_ecdsa")
    (key-pair/write-public-key keypair "/tmp/clojuressh_id_ecdsa.pub" "docker-key")

    (process/sh "chmod 0700 /tmp/clojuressh_id_rsa")
    (docker/exec "mkdir /root/.ssh")
    (docker/exec "chmod 0700 /root/.ssh")
    (docker/cp-to "/tmp/clojuressh_id_ecdsa.pub" "/root/.ssh/authorized_keys")
    (docker/exec "chmod 0600 /root/.ssh/authorized_keys")
    (docker/exec "chown root:root -R /root/.ssh")

    (let [config (config-repository/openssh-config-string "
Port 9876

Host docker-host
  User root
  Hostname localhost

Host *
  ConnectTime 30000
  PreferredAuthentications publickey
  IdentityFile /tmp/clojuressh_id_ecdsa
  UserKnownHostsFile /tmp/known_hosts
")]
      (agent/set-config-repository agent config)

      (let [session (agent/get-session agent "docker-host")]
        (session/set-config session :strict-host-key-checking false)
        (session/connect session)
        (let [{:keys [exit out]} @(clojuressh/exec session "echo test" {:out :string})]
          (is (zero? exit))
          (is (= "test\n" out))))))

  (docker/cleanup))

(deftest test-openssh-config-key-auth-rsa
  (docker/cleanup)
  (docker/build {:root-password "root-access-please"})
  (docker/start {:ssh-port 9876})

  (let [agent (agent/new)
        keypair (key-pair/generate agent :rsa 1024)]
    (key-pair/write-private-key keypair "/tmp/clojuressh_id_rsa")
    (key-pair/write-public-key keypair "/tmp/clojuressh_id_rsa.pub" "docker-key")

    (process/sh "chmod 0700 /tmp/clojuressh_id_rsa")
    (docker/exec "mkdir /root/.ssh")
    (docker/exec "chmod 0700 /root/.ssh")
    (docker/cp-to "/tmp/clojuressh_id_rsa.pub" "/root/.ssh/authorized_keys")
    (docker/exec "chmod 0600 /root/.ssh/authorized_keys")
    (docker/exec "chown root:root -R /root/.ssh")

    (let [config (config-repository/openssh-config-string "
Port 9876

Host docker-host
  User root
  Hostname localhost

Host *
  ConnectTime 30000
  PreferredAuthentications publickey
  IdentityFile /tmp/clojuressh_id_rsa
  UserKnownHostsFile /tmp/known_hosts
")]
      (agent/set-config-repository agent config)

      (let [session (agent/get-session agent "docker-host")]
        (session/set-config session :strict-host-key-checking false)
        (session/connect session)
        (let [{:keys [exit out]} @(clojuressh/exec session "echo test" {:out :string})]
          (is (zero? exit))
          (is (= "test\n" out))))))

  (docker/cleanup))

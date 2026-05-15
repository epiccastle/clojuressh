(ns clojuressh.agent
  (:require #?(:bb [babashka.pods :as pods]
               :clj [clojure.java.io :as io]))
  #?(:bb (:import)
     :clj (:import [com.jcraft.jsch JSch Logger
                    IdentityRepository HostKeyRepository
                    ConfigRepository Identity]
                   [java.io InputStream])))

#?(:bb (pods/load-pod 'epiccastle/bbssh "0.7.0"))
#?(:bb (require '[pod.epiccastle.bbssh.agent :as agent]))

(set! *warn-on-reflection* true)

(defn new
  "Make a new JSch agent. A JSch agent is not an \"ssh agent\".
  It is the base java class that holds and controls the
  sessions."
  []
  #?(:bb (agent/new)
     :clj (JSch.)))

(defn get-session
  "Construct a new JSch connection session. Does not start the ssh
  connection.
  "
  ([agent host]
   #?(:bb (agent/get-session agent host)
      :clj (.getSession ^JSch agent ^String host)))
  ([agent username host]
   #?(:bb (agent/get-session agent username host)
      :clj (.getSession ^JSch agent ^String username ^String host)))
  ([agent username host port]
   #?(:bb (agent/get-session agent username host port)
      :clj (.getSession ^JSch agent ^String username ^String host ^long port))))

(defn get-identity-repository
  "Get the current identity-repository from the agent."
  [agent]
  #?(:bb (agent/get-identity-repository agent)
     :clj (.getIdentityRepository ^JSch agent)))

(defn set-identity-repository
  "Set the identity-repository the agent should use."
  [agent identity-repository]
  #?(:bb (agent/set-identity-repository agent identity-repository)
     :clj (.setIdentityRepository ^JSch agent ^IdentityRepository identity-repository)))

(defn get-config-repository
  "Get the current config-repository from the agent."
  [agent]
  #?(:bb (agent/get-config-repository agent)
     :clj (.getConfigRepository ^JSch agent)))

(defn set-config-repository
  "Set the config-repository the agent should use."
  [agent config-repository]
  #?(:bb (agent/set-config-repository agent config-repository)
     :clj (.setConfigRepository ^JSch agent ^ConfigRepository config-repository)))

(defn get-host-key-repository
  "Get the current host-key-repository from the agent."
  [agent]
  #?(:bb (agent/get-host-key-repository agent)
     :clj (.getHostKeyRepository ^JSch agent)))

(defn set-host-key-repository
  "Set the host-key-repository the agent should use."
  [agent host-key-repository]
  #?(:bb (agent/set-host-key-repository agent host-key-repository)
     :clj (.setHostKeyRepository ^JSch agent ^HostKeyRepository host-key-repository)))

(defn set-known-hosts
  "Set the known hosts file location"
  [agent filename]
  #?(:bb (agent/set-known-hosts agent filename)
     :clj (.setKnownHosts ^JSch agent ^String filename)))

(defn set-known-hosts-content
  "Set the known hosts file location"
  [agent content]
  #?(:bb (agent/set-known-hosts-content agent content)
     :clj (.setKnownHosts
           ^JSch agent
           ^InputStream (io/input-stream content))))

(defn add-identity
  "Add the private key to be used in authentication. Optionally
  add the public key aswell. Private key can be decrypted with passphrase."
  ([agent filename]
   #?(:bb (agent/add-identity agent filename)
      :clj (.addIdentity ^JSch agent ^String filename)))
  ([agent filename passphrase]
   #?(:bb (agent/add-identity agent filename passphrase)
      :clj (.addIdentity ^JSch agent ^String filename ^String passphrase)))
  ([agent private-key-filename public-key-filename passphrase]
   #?(:bb (agent/add-identity agent private-key-filename public-key-filename passphrase)
      :clj (.addIdentity
            ^JSch agent
            ^String private-key-filename
            ^String public-key-filename
            ^bytes passphrase)))
  ([agent identity-name private-key public-key passphrase]
   #?(:bb (agent/add-identity agent identity-name private-key public-key passphrase)
      :clj (.addIdentity
            ^JSch agent
            ^String identity-name
            ^bytes private-key
            ^bytes public-key
            ^bytes passphrase))))

(defn remove-identity
  "remove an identity by its name or its reference"
  [agent identity-name]
  #?(:bb (agent/remove-identity agent identity-name)
     :clj (.removeIdentity ^JSch agent ^String identity-name)))

(defn get-identity-names
  "Lists names of identities included in the identity-repository"
  [agent]
  #?(:bb (agent/get-identity-names agent)
     :clj (into []
                (.getIdentityNames
                 ^JSch agent))))

(defn remove-all-identities
  "Removes all identities from the identity-repository."
  [agent]
  #?(:bb (agent/remove-all-identities agent)
     :clj (.removeAllIdentity ^JSch agent)))

(defn get-config
  "Returns the config value for the specified key"
  [key]
  #?(:bb (agent/get-config key)
     :clj (JSch/getConfig ^String key)))

(defn set-config
  "Sets or overrides the configuration."
  ([hashmap]
   #?(:bb (agent/set-config hashmap)
      :clj (doseq [[key value] hashmap]
             (JSch/setConfig
              ^String key
              ^String value))))
  ([key value]
   #?(:bb (agent/set-config key value)
      :clj (JSch/setConfig ^String key ^String value))))

(defn set-debug-fn [debug-fn]
  #?(:bb (agent/set-debug-fn debug-fn)
     :clj (JSch/setLogger
           (proxy [Logger] []
             (isEnabled [_]
               true)
             (log [level msg]
               (debug-fn level msg))))))

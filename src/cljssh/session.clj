(ns cljssh.session
  (:require [clojure.string :as string]
            [cljssh.impl.utils :as utils])
  (:import [com.jcraft.jsch JSch Session
            UserInfo IdentityRepository
            HostKeyRepository Proxy ProxyHTTP ProxySOCKS4 ProxySOCKS5])
  )

(set! *warn-on-reflection* true)

(defn set-password
  "Set the password the session will use to authenticate to
  `password`"
  [^Session session ^String password]
  (.setPassword session password))

(defn set-user-info
  "Set the `user-info` for the `session`. The session will
  use this user-info structure to ask for passwords and passphrases."
  [^Session session ^UserInfo user-info]
  (.setUserInfo session user-info))

(defn make-proxy
  [{:keys [type host port username password]}]
  (let [proxy (case type
                :http (ProxyHTTP. host port)
                :socks4 (ProxySOCKS4. host port)
                :socks5 (ProxySOCKS5. host port))]
    (when username
      (case type
        ;; Seems we don't have a better way to avoid the code duplication since
        ;; the setUserPasswd method is not in the jsch `Proxy` interface.
        :http (.setUserPasswd ^ProxyHTTP proxy username password)
        :socks4 (.setUserPasswd ^ProxySOCKS4 proxy username password)
        :socks5 (.setUserPasswd ^ProxySOCKS5 proxy username password)))
    proxy))

(defn set-proxy
  "sets the http/socks proxy to connect with the ssh server.

  The provided arg must have at least `:type` (one of
  `#{:http :socks4 :socks5}`), `:host`, `:port` and optionally `:username` and
  `:password` for proxy authentication. "
  [^Session session proxy]
  (.setProxy
    session
    ^Proxy (make-proxy proxy)))

(defn connect
  "Initiate the ssh connection with an optional `timeout`
  (in milliseconds)."
  [session & [timeout]]
  (try
    (if timeout
      (.connect
        ^Session session
        timeout)
      (.connect
        ^Session session))
    (catch com.jcraft.jsch.JSchException e
  (throw (ex-info (.getMessage e)
                  {:type    ::ssh-connect-error
                   :cause   (.getCause e)
                   :message (.getMessage e)}

                  ; original exception
                  e)))))

(defn disconnect
  "Disconnect the ssh connection"
  [^Session session]
  (.disconnect session))

(defn set-port-forwarding-local
  "Register the local port to forward all connection to the remote
  side, where they will connect to a remote host on a port.

  `options` is a hashmap with one of the following forms

  To port forward to a remote TCP/IP port
  ```clj
  {
    :bind-address \"127.0.0.1\"              ;; the local interface to bind to. Use \"*\" or \"0.0.0.0\" for all interfaces.
    :local-port 2200                       ;; the local port to listen on
    :remote-host \"jump-target.domain.com\"  ;; the remote host to forward the connection to on the remote side
    :remote-port 22                        ;; the remote port to forward to
    :connect-timeout 30000                 ;; how long to try to connect for
  }
  ```

  To port forward to a remote unix domain socket
  ```clj
  {
    :bind-address \"127.0.0.1\"              ;; the local interface to bind to. Use \"*\" or \"0.0.0.0\" for all interfaces.
    :local-port 2200                       ;; the local port to listen on
    :remote-unix-socket \"/var/run/socket\"  ;; the remote host to forward the connection to on the remote side
    :connect-timeout 30000                 ;; how long to try to connect for
  }
  "
  [session
   {:keys [bind-address
           local-port
           remote-host
           remote-unix-socket
           remote-port
           connect-timeout]
    :or {bind-address "127.0.0.1"
         connect-timeout 0}}]
  (if remote-unix-socket
    (.setSocketForwardingL
      ^Session session
      ^String bind-address
      ^int local-port
      ^String remote-unix-socket
      nil
      ^int connect-timeout
      )
    (.setPortForwardingL
      ^Session session
      ^String bind-address
      ^int local-port
      ^String remote-host
      ^int remote-port
      nil
      ^int connect-timeout)))

(defn delete-port-forwarding-local
  "Cancels the specified local port forwarding"
  [session
   {:keys [bind-address
           local-port]
    :or {bind-address "127.0.0.1"}}]
  (.delPortForwardingL
   ^Session session
   ^String bind-address
   ^int local-port))

(defn get-port-forwarding-local
  "return a list of all the local port forwards. List elements
  are of the form \"local-port:host:host-port\"."
  [session]
  (->>
   (.getPortForwardingL
    ^Session session)
   (mapv (fn [s]
           (let [[local-port remote-host remote-port]
                 (string/split s #":")]
             (if (and (= remote-host "null")
                      (= remote-port "0"))
               ;; Jsch PortWatcher doesn't report socket forwarding path details
               {:local-port (Integer/parseInt local-port)}
               {:local-port (Integer/parseInt local-port)
                :remote-host remote-host
                :remote-port (Integer/parseInt remote-port)}))))))

(defn set-port-forwarding-remote
  "Register the remote port to forward to the local machine and then
  connect out to a host on the local network.

  `options` is a hashmap for the following form

  ```clj
  {
    :bind-address \"127.0.0.1\"            ;; the remote interface to bind to. Use \"*\" or \"0.0.0.0\" for all interfaces.
    :remote-port 22                        ;; the remote port to bind to
    :local-host \"host.localdomain\"       ;; the local network host to forward the connection to on the local side
    :local-port 2200                       ;; the local port to connect to
    :connect-timeout 30000                 ;; how long to try to connect for
  }
  ```
  "
  [session
   {:keys [bind-address
           remote-port
           local-host
           local-port]
    :or {bind-address "127.0.0.1"
         local-host "127.0.0.1"}}]
  (.setPortForwardingR
   ^Session session
   ^String bind-address
   ^int remote-port
   ^String local-host
   ^int local-port))

(defn delete-port-forwarding-remote
  "Cancels the specified remote port forwarding"
  [session
   {:keys [bind-address
           remote-port]
    :or {bind-address "127.0.0.1"}}]
  (.delPortForwardingR
   ^Session session
   ^String bind-address
   ^int remote-port))

(defn get-port-forwarding-remote
  "return a list of all the remote port forwards. List elements
  are of the form \"local-port:host:host-port\"."
  [session]
  (->>
   (.getPortForwardingR
    ^Session session)
   (mapv (fn [s]
           (let [[local-port remote-host remote-port]
                 (string/split s #":")]
             {:remote-port (Integer/parseInt local-port)
              :local-host remote-host
              :local-port (Integer/parseInt remote-port)})))))

(defn set-host
  "Set the host to connect to"
  [^Session session ^String host]
  (.setHost session host))

(defn set-port
  "Set the port to connect to"
  [^Session session port]
  (.setHost session ^int port))

(defn set-config
  "Set the config setting `key` to `value`"
  [^Session session key value]
  (.setConfig
   session
   ^String (if (keyword? key)
             (utils/to-camel-case (name key))
             key)
   ^String (utils/boolean-to-yes-no value)))

(defn set-configs
  "Merge the config values from the passed in hashmap into the session
  config"
  [session hashmap]
  (doseq [[key value] hashmap]
    (.setConfig
     ^Session session
     ^String (if (keyword? key)
               (utils/to-camel-case (name key))
               key)
     ^String (utils/boolean-to-yes-no value))))

(defn get-config
  "Get the current config setting `key`"
  [^Session session key]
  (.getConfig
   session
   ^String (if (keyword? key)
             (utils/to-camel-case (name key))
             key)))

(defn connected?
  "return true if session is currently connected"
  [^Session session]
  (.isConnected session))

(defn open-channel
  "open a channel on the session and return it"
  [^Session session ^String type]
  (.openChannel session type))

(defn set-identity-repository
  "sets the identity-repository that will be used in the
  public key authentication"
  [^Session session ^IdentityRepository identity-repository]
  (.setIdentityRepository session identity-repository))

(defn set-host-key-repository
  "sets the host-key-repository that will be used in the
  public key authentication"
  [^Session session ^HostKeyRepository host-key-repository]
  (.setHostKeyRepository session host-key-repository))

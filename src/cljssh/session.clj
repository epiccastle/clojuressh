(ns cljssh.session
  (:require [clojure.string :as string])
  (:import [com.jcraft.jsch JSch Session
            UserInfo IdentityRepository
            HostKeyRepository Proxy ProxyHTTP ProxySOCKS4 ProxySOCKS5])
  )

;; cljssh.* are invoked on pod side.

(set! *warn-on-reflection* true)

(defn set-password [^Session session ^String password]
  (.setPassword session password))

(defn set-user-info [^Session session ^UserInfo user-info]
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
  [^Session session proxy]
  (.setProxy
    session
    ^Proxy (make-proxy proxy)))

(defn ^:blocking connect
  "marked ^:blocking because connect blocks until the connection
  is made. This process may need many async callbacks via user-info
  and identity stores"
  [session & [timeout]]
  (if timeout
    (.connect
     ^Session session
     timeout)
    (.connect
     ^Session session)))

(defn disconnect [^Session session]
  (.disconnect session))

(defn set-port-forwarding-local
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
  [session
   {:keys [bind-address
           local-port]
    :or {bind-address "127.0.0.1"}}]
  (.delPortForwardingL
   ^Session session
   ^String bind-address
   ^int local-port))

(defn get-port-forwarding-local
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
  [session
   {:keys [bind-address
           remote-port]
    :or {bind-address "127.0.0.1"}}]
  (.delPortForwardingR
   ^Session session
   ^String bind-address
   ^int remote-port))

(defn get-port-forwarding-remote
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
  [^Session session ^String host]
  (.setHost session host))

(defn set-port
  [^Session session ^int port]
  (.setHost session port))

(defn set-config
  [^Session session key value]
  (.setConfig
   session
   ^String (if (keyword? key)
             (utils/to-camel-case (name key))
             key)
   ^String (utils/boolean-to-yes-no value)))

(defn set-configs
  [session hashmap]
  (doseq [[key value] hashmap]
    (.setConfig
     ^Session session
     ^String (if (keyword? key)
               (utils/to-camel-case (name key))
               key)
     ^String (utils/boolean-to-yes-no value))))

(defn get-config
  [^Session session key]
  (.getConfig
   session
   ^String (if (keyword? key)
             (utils/to-camel-case (name key))
             key)))

(defn connected?
  [^Session session]
  (.isConnected session))

(defn open-channel
  [^Session session ^String type]
  (.openChannel session type))

(defn set-identity-repository
  [^Session session ^IdentityRepository identity-repository]
  (.setIdentityRepository session identity-repository))

(defn set-host-key-repository
  [^Session session ^HostKeyRepository host-key-repository]
  (.setHostKeyRepository session host-key-repository))

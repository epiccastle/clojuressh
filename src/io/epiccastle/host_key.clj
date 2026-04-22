(ns io.epiccastle.host-key
  (:import [com.jcraft.jsch HostKey JSch]))

(def types
  {:unknown HostKey/UNKNOWN
   :guess HostKey/GUESS
   :sshdss HostKey/SSHDSS
   :sshrsa HostKey/SSHRSA
   :ecdsa256 HostKey/ECDSA256
   :ecdsa384 HostKey/ECDSA384
   :ecdsa521 HostKey/ECDSA521
   :ed25519 HostKey/ED25519
   :ed448 HostKey/ED448
   })

(defn new
  ([^String host ^bytes key]
   (HostKey. host (utils/decode-base64 key)))
  ([^String host type ^bytes key]
   (HostKey. host (types type HostKey/GUESS) (utils/decode-base64 key)))
  ([^String host type ^bytes key ^String comment]
   (HostKey. host (types type HostKey/GUESS) (utils/decode-base64 key) comment))
  ([^String marker ^String host type ^bytes key ^String comment]
   (HostKey. marker host (types type HostKey/GUESS) (utils/decode-base64 key) comment)
   ))

(defn get-host
  [host-key]
  (.getHost
   ^HostKey host-key))

(defn get-type
  [host-key]
  (.getType
   ^HostKey host-key))

(defn get-key
  [host-key]
  (.getKey
   ^HostKey host-key))

(defn get-finger-print
  [host-key agent]
  (.getFingerPrint
   ^HostKey host-key
   ^JSch agent))

(defn get-comment
  [host-key]
  (.getComment
   ^HostKey host-key))

(defn get-marker
  [host-key]
  (.getMarker
   ^HostKey host-key))

(defn get-info [host-key agent]
  (let [host-key ^HostKey host-key
        agent ^JSch agent]
    {:host (.getHost host-key)
     :type (.getType host-key)
     :key (.getKey host-key)
     :finger-print (.getFingerPrint host-key agent)
     :comment (.getComment host-key)
     :marker (.getMarker host-key)}))

(defn get-infos [host-keys agent]
  (into
   {}
   (for [host-key host-keys]
     (let [instance ^HostKey host-key
           agent ^JSch agent]
       [host-key
        {:host (.getHost instance)
         :type (.getType instance)
         :key (.getKey instance)
         :finger-print (.getFingerPrint instance agent)
         :comment (.getComment instance)
         :marker (.getMarker instance)}]))))

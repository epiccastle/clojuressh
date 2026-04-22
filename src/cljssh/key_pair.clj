(ns cljssh.key-pair
  (:refer-clojure :exclude [load])
  (:import [com.jcraft.jsch JSch KeyPair]))

;; cljssh.* are invoked on pod side.

(set! *warn-on-reflection* true)

(defn generate [^JSch agent key-type ^int key-size]
  (KeyPair/genKeyPair
   agent
   ^int ({:dsa KeyPair/DSA
          :rsa KeyPair/RSA
          :ecdsa KeyPair/ECDSA
          :ed25519 KeyPair/ED25519
          :ed448 KeyPair/ED448}
         key-type)
   key-size))


(defn set-passphrase [^KeyPair key-pair ^String passphrase]
  (.setPassphrase key-pair passphrase))

(defn write-private-key
  ([^KeyPair key-pair ^String filename]
   (.writePrivateKey key-pair filename))
  ([^KeyPair key-pair ^String filename passphrase]
   (.writePrivateKey
    key-pair
    filename
    ^bytes (utils/decode-base64 passphrase))))

(defn write-public-key [^KeyPair key-pair ^String filename ^String comment]
  (.writePublicKey key-pair filename comment))

(defn get-finger-print [^KeyPair key-pair]
  (.getFingerPrint key-pair))

(defn get-public-key-blob [key-pair]
  (utils/encode-base64
   (.getPublicKeyBlob
    ^KeyPair key-pair)))

(defn get-key-size [^KeyPair key-pair]
  (.getKeySize key-pair))

(defn dispose [^KeyPair key-pair]
  (.dispose key-pair))

(defn is-encrypted [^KeyPair key-pair]
  (.isEncrypted key-pair))

(defn decrypt [^KeyPair key-pair passphrase]
  (.decrypt
   key-pair
   ^bytes (utils/decode-base64 passphrase)))

(defn load [^JSch agent ^String private-key-file ^String public-key-file]
  (KeyPair/load agent private-key-file public-key-file))

(defn load-bytes [agent private-key-bytes public-key-bytes]
  (let [private-key-bytes (when private-key-bytes
                            (utils/decode-base64 private-key-bytes))
        public-key-bytes (when public-key-bytes
                           (utils/decode-base64 public-key-bytes))]
    (KeyPair/load
      ^JSch agent
      ^bytes private-key-bytes
      ^bytes public-key-bytes)))

(defn get-signature
  ([key-pair data]
   (utils/encode-base64
    (.getSignature
     ^KeyPair key-pair
     ^bytes (utils/decode-base64 data))))
  ([key-pair data algorithm]
   (utils/encode-base64
    (.getSignature
     ^KeyPair key-pair
     ^bytes (utils/decode-base64 data)
     ^String algorithm))))

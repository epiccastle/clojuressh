(ns clojuressh.key-pair
  (:refer-clojure :exclude [load])
  (:import [com.jcraft.jsch JSch KeyPair]))

(set! *warn-on-reflection* true)

(defn generate
  "Generate a public/private SSH key pair.
  `key-type` should be `:dsa`, `:rsa`, `:ecdsa`, `:ed25519`
  or `:ed448`.
  `key-size` is the number of bits and defaults to 2048.
  "
  ([^JSch agent key-type]
   (generate agent key-type 2048))
  ([^JSch agent key-type ^long key-size]
   (KeyPair/genKeyPair
     agent
     ^int ({:dsa KeyPair/DSA
            :rsa KeyPair/RSA
            :ecdsa KeyPair/ECDSA
            :ed25519 KeyPair/ED25519
            :ed448 KeyPair/ED448}
           key-type)
     key-size)))


(defn set-passphrase
  "Set the passphrase on the private key to the string
  `passphrase`"
  [^KeyPair key-pair ^String passphrase]
  (.setPassphrase key-pair passphrase))

(defn write-private-key
  "write the private key to a file `filename`. Optionally
  pass in a byte array `passphrase` to be used as a passphrase."
  ([^KeyPair key-pair ^String filename]
   (.writePrivateKey key-pair filename))
  ([^KeyPair key-pair ^String filename passphrase]
   (.writePrivateKey
    key-pair
    filename
    ^bytes passphrase)))

(defn write-public-key
  "write the public key to file `filename` with the attached
  `comment` string."
  [^KeyPair key-pair ^String filename ^String comment]
  (.writePublicKey key-pair filename comment))

(defn get-finger-print
  "return the key finger print as a string."
  [^KeyPair key-pair]
  (.getFingerPrint key-pair))

(defn get-public-key-blob
  "returns a byte-array of the raw public key data."
  [key-pair]
  (.getPublicKeyBlob
   ^KeyPair key-pair))

(defn get-key-size
  "returns the bit length of the key"
  [^KeyPair key-pair]
  (.getKeySize key-pair))

(defn dispose
  "zero out the memory holding the private key passphrase
  so subsequent attacks on stale memory are thwarted"
  [^KeyPair key-pair]
  (.dispose key-pair))

(defn is-encrypted
  "returns true if the private key is encrypted with a
  passphrase"
  [^KeyPair key-pair]
  (.isEncrypted key-pair))

(defn decrypt
  "decrypt the private key with the passed in byte-array
  so that the private key is no longer stored encrypted.
  Can be followed up with setting a new passphrase to
  re-encrypt. Returns true if the decryption succeeded."
  [^KeyPair key-pair passphrase]
  (.decrypt
   key-pair
   ^bytes passphrase))

(defn load
  "Load the key pair from a file. Pass both private and
  public filenames in to load from those files. If public
  key filename is omitted, the private key filename with
  \".pub\" appended is used"
  [^JSch agent ^String private-key-file ^String public-key-file]
  (KeyPair/load agent private-key-file public-key-file))

(defn load-bytes
  "Load the key pair from a byte array. Pass both private and
  public keys either as byte arrays or as strings. Half a keypair
  can be loaded to perform some operations. You may pass in
  `nil` for one of the key portions to only load the public or private
  portion."
  [agent private-key-bytes public-key-bytes]
  (let [^bytes priv (if (or (nil? private-key-bytes) (bytes? private-key-bytes))
                      private-key-bytes
                      (.getBytes ^String private-key-bytes))
        ^bytes pub  (if (or (nil? public-key-bytes) (bytes? public-key-bytes))
                      public-key-bytes
                      (.getBytes ^String public-key-bytes))]
    (KeyPair/load ^JSch agent priv pub)))

(defn get-signature
  "Sign the passed in data with the private key, using algorithm
  if it is passed aswell"
  ([key-pair data]
   (.getSignature
    ^KeyPair key-pair
    ^bytes data))
  ([key-pair data algorithm]
   (.getSignature
    ^KeyPair key-pair
    ^bytes data
    ^String algorithm)))

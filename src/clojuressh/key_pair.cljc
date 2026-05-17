(ns clojuressh.key-pair
  (:refer-clojure :exclude [load])
  #?(:bb (:require [babashka.pods :as pods]))
  #?(:bb (:import)
     :clj (:import [com.jcraft.jsch JSch KeyPair])))

#?(:bb (pods/load-pod 'epiccastle/bbssh "0.7.0"))
#?(:bb (require '[pod.epiccastle.bbssh.key-pair :as key-pair]))

(set! *warn-on-reflection* true)

(defn generate
  "Generate a public/private SSH key pair.
  `key-type` should be `:dsa`, `:rsa`, `:ecdsa`, `:ed25519`
  or `:ed448`.
  `key-size` is the number of bits and defaults to 2048.
  "
  ([agent key-type]
   #?(:bb (key-pair/generate agent key-type)
      :clj (generate agent key-type 2048)))
  ([agent key-type key-size]
   #?(:bb (key-pair/generate agent key-type key-size)
      :clj (KeyPair/genKeyPair
            ^JSch agent
            ^int ({:dsa KeyPair/DSA
                   :rsa KeyPair/RSA
                   :ecdsa KeyPair/ECDSA
                   :ed25519 KeyPair/ED25519
                   :ed448 KeyPair/ED448}
                  key-type)
            ^int key-size))))


(defn set-passphrase
  "Set the passphrase on the private key to the string
  `passphrase`"
  [key-pair passphrase]
  #?(:bb (key-pair/set-passphrase key-pair passphrase)
     :clj (.setPassphrase ^KeyPair key-pair ^String passphrase)))

(defn write-private-key
  "write the private key to a file `filename`. Optionally
  pass in a byte array `passphrase` to be used as a passphrase."
  ([key-pair filename]
   #?(:bb (key-pair/write-private-key key-pair filename)
      :clj (.writePrivateKey ^KeyPair key-pair ^String filename)))
  ([key-pair filename passphrase]
   #?(:bb (key-pair/write-private-key key-pair filename passphrase)
      :clj (.writePrivateKey
            ^KeyPair key-pair
            ^String filename
            ^bytes passphrase))))

(defn write-public-key
  "write the public key to file `filename` with the attached
  `comment` string."
  [key-pair filename comment]
  #?(:bb (key-pair/write-public-key key-pair filename comment)
     :clj (.writePublicKey ^KeyPair key-pair ^String filename ^String comment)))

(defn get-finger-print
  "return the key finger print as a string."
  [key-pair]
  #?(:bb (key-pair/get-finger-print key-pair)
     :clj (.getFingerPrint ^KeyPair key-pair)))

(defn get-public-key-blob
  "returns a byte-array of the raw public key data."
  [key-pair]
  #?(:bb (key-pair/get-public-key-blob key-pair)
     :clj (.getPublicKeyBlob
           ^KeyPair key-pair)))

(defn get-key-size
  "returns the bit length of the key"
  [key-pair]
  #?(:bb (key-pair/get-key-size key-pair)
     :clj (.getKeySize ^KeyPair key-pair)))

(defn dispose
  "zero out the memory holding the private key passphrase
  so subsequent attacks on stale memory are thwarted"
  [key-pair]
  #?(:bb (key-pair/dispose key-pair)
     :clj (.dispose ^KeyPair key-pair)))

(defn is-encrypted
  "returns true if the private key is encrypted with a
  passphrase"
  [key-pair]
  #?(:bb (key-pair/is-encrypted key-pair)
     :clj (.isEncrypted ^KeyPair key-pair)))

(defn decrypt
  "decrypt the private key with the passed in byte-array
  so that the private key is no longer stored encrypted.
  Can be followed up with setting a new passphrase to
  re-encrypt. Returns true if the decryption succeeded."
  [key-pair passphrase]
  #?(:bb (key-pair/decrypt key-pair passphrase)
     :clj (.decrypt
           ^KeyPair key-pair
           ^bytes passphrase)))

(defn load
  "Load the key pair from a file. Pass both private and
  public filenames in to load from those files. If public
  key filename is omitted, the private key filename with
  \".pub\" appended is used"
  [agent private-key-file public-key-file]
  #?(:bb (key-pair/load agent private-key-file public-key-file)
     :clj (KeyPair/load ^JSch agent ^String private-key-file ^String public-key-file)))

(defn load-bytes
  "Load the key pair from a byte array. Pass both private and
  public keys either as byte arrays or as strings. Half a keypair
  can be loaded to perform some operations. You may pass in
  `nil` for one of the key portions to only load the public or private
  portion."
  [agent private-key-bytes public-key-bytes]
  #?(:bb (key-pair/load-bytes agent private-key-bytes public-key-bytes)
     :clj (let [^bytes priv (if (or (nil? private-key-bytes) (bytes? private-key-bytes))
                              private-key-bytes
                              (.getBytes ^String private-key-bytes))
                ^bytes pub  (if (or (nil? public-key-bytes) (bytes? public-key-bytes))
                              public-key-bytes
                              (.getBytes ^String public-key-bytes))]
            (KeyPair/load ^JSch agent priv pub))))

(defn get-signature
  "Sign the passed in data with the private key, using algorithm
  if it is passed aswell"
  ([key-pair data]
   #?(:bb (key-pair/get-signature key-pair data)
      :clj (.getSignature
            ^KeyPair key-pair
            ^bytes data)))
  ([key-pair data algorithm]
   #?(:bb (key-pair/get-signature key-pair data algorithm)
      :clj (.getSignature
            ^KeyPair key-pair
            ^bytes data
            ^String algorithm))))

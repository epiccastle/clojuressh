(ns cljssh.byte-array-output-stream
  (:refer-clojure :exclude [flush])
  (:require [cljssh.callbacks :as callbacks]
            [cljssh.cleaner :as cleaner])
  (:import [java.io
            ByteArrayOutputStream OutputStream]))

;; cljssh.* are invoked on pod side.

(set! *warn-on-reflection* true)

(defn new
  ([]
   (ByteArrayOutputStream.))
  ([size]
   (ByteArrayOutputStream. size)))

(defn close [^ByteArrayOutputStream stream]
  (.close stream))

(defn reset [^ByteArrayOutputStream stream]
  (.reset stream))

(defn size [^ByteArrayOutputStream stream]
  (.size stream))

(defn to-byte-array [stream]
  (utils/encode-base64
   (.toByteArray
    ^ByteArrayOutputStream stream)))

(defn to-string
  ([^ByteArrayOutputStream stream]
   (.toString stream))
  ([^ByteArrayOutputStream stream ^String encoding]
   (.toString stream encoding)))

(defn write
  ([stream int-or-base64]
   (if (string? int-or-base64)
     (let [buffer (utils/decode-base64 int-or-base64)
           size (count buffer)]
       (.write
        ^ByteArrayOutputStream stream
        ^bytes buffer
        0
        size))
     (.write
      ^ByteArrayOutputStream stream
      ^int int-or-base64))))

(defn write-to
  [^ByteArrayOutputStream stream ^OutputStream out]
  (.writeTo stream out))

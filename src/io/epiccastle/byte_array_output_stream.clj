(ns io.epiccastle.byte-array-output-stream
  (:refer-clojure :exclude [flush])
  (:require [io.epiccastle.callbacks :as callbacks]
            [io.epiccastle.cleaner :as cleaner])
  (:import [java.io
            ByteArrayOutputStream OutputStream]))

;; io.epiccastle.* are invoked on pod side.

(set! *warn-on-reflection* true)

(defn new
  ([]
   (ByteArrayOutputStream.))
  ([size]
   (ByteArrayOutputStream. size)))

(defn close [stream]
  (.close
   ^ByteArrayOutputStream stream))

(defn reset [stream]
  (.reset
   ^ByteArrayOutputStream stream))

(defn size [stream]
  (.size
   ^ByteArrayOutputStream stream))

(defn to-byte-array [stream]
  (utils/encode-base64
   (.toByteArray
    ^ByteArrayOutputStream stream)))

(defn to-string
  ([stream]
   (.toString
    ^ByteArrayOutputStream stream))
  ([stream encoding]
   (.toString
    ^ByteArrayOutputStream stream
    ^String encoding)))

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
  [stream out]
  (.writeTo
   ^ByteArrayOutputStream stream
   ^OutputStream out))

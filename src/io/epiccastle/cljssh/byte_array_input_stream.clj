(ns io.epiccastle.cljssh.byte-array-input-stream
  (:refer-clojure :exclude [read])
  (:require [io.epiccastle.cljssh.callbacks :as callbacks]
            [io.epiccastle.cljssh.cleaner :as cleaner])
  (:import [java.io
            PipedInputStream PipedOutputStream
            ByteArrayInputStream ByteArrayOutputStream
            InputStream]
           [java.util Arrays]))

;; io.epiccastle.cljssh.* are invoked on pod side.

(set! *warn-on-reflection* true)

(defn new-from-string [^String string & [^String encoding]]
  (ByteArrayInputStream.
   ^bytes (.getBytes string (or encoding "utf-8"))))

(defn new-from-bytes [^String string]
  (ByteArrayInputStream.
   ^bytes (utils/decode-base64 string)))

(defn available [^ByteArrayInputStream stream]
  (.available stream))

(defn close [^ByteArrayInputStream stream]
  (.close stream))

(defn mark [^ByteArrayInputStream stream read-ahead-limit]
  (.mark stream read-ahead-limit))

(defn mark-supported [^ByteArrayInputStream stream]
  (.markSupported stream))

(defn ^:blocking read
  ([^ByteArrayInputStream stream]
   (.read stream))
  ([stream bytes]
   (let [arr (byte-array bytes)
         bytes-read
         (.read
          ^ByteArrayInputStream stream
          arr
          0
          bytes)]
     [bytes-read
      (case bytes-read
        -1 nil
        0 ""
        (utils/encode-base64 (Arrays/copyOfRange arr 0 bytes-read)))])))

(defn reset [^ByteArrayInputStream stream]
  (.reset stream))

(defn skip [^ByteArrayInputStream stream n]
  (.skip stream n))

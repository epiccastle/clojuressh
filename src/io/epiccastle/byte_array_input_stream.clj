(ns io.epiccastle.byte-array-input-stream
  (:refer-clojure :exclude [read])
  (:require [io.epiccastle.callbacks :as callbacks]
            [io.epiccastle.cleaner :as cleaner])
  (:import [java.io
            PipedInputStream PipedOutputStream
            ByteArrayInputStream ByteArrayOutputStream
            InputStream]
           [java.util Arrays]))

;; io.epiccastle.* are invoked on pod side.

(set! *warn-on-reflection* true)

(defn new-from-string [^String string & [^String encoding]]
  (ByteArrayInputStream.
   ^bytes (.getBytes string (or encoding "utf-8"))))

(defn new-from-bytes [^String string]
  (ByteArrayInputStream.
   ^bytes (utils/decode-base64 string)))

(defn available [stream]
  (.available
   ^ByteArrayInputStream stream))

(defn close [stream]
  (.close
   ^ByteArrayInputStream stream))

(defn mark [stream read-ahead-limit]
  (.mark
   ^ByteArrayInputStream stream
   read-ahead-limit))

(defn mark-supported [stream]
  (.markSupported
   ^ByteArrayInputStream stream))

(defn ^:blocking read
  ([stream]
   (.read
    ^ByteArrayInputStream stream))
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

(defn reset [stream]
  (.reset
   ^ByteArrayInputStream stream))

(defn skip [stream n]
  (.skip
   ^ByteArrayInputStream stream
   n))

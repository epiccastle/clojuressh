(ns clojuressh.byte-array-output-stream
  (:refer-clojure :exclude [flush])
  #?(:bb (:require [babashka.pods :as pods]))
  #?(:bb (:import)
     :clj (:import [java.io
                    ByteArrayOutputStream OutputStream])))

#?(:bb (pods/load-pod 'epiccastle/bbssh "0.7.0"))
#?(:bb (require '[pod.epiccastle.bbssh.byte-array-output-stream :as byte-array-output-stream]))

(set! *warn-on-reflection* true)

(defn new
  ([]
   #?(:bb (byte-array-output-stream/new)
      :clj (ByteArrayOutputStream.)))
  ([size]
   #?(:bb (byte-array-output-stream/new size)
      :clj (ByteArrayOutputStream. size))))

(defn close
  "Closing a ByteArrayOutputStream has no effect."
  [stream]
  #?(:bb (byte-array-output-stream/close stream)
     :clj (.close ^ByteArrayOutputStream stream)))

(defn reset
  "Resets the count field of this byte array output stream to zero, so
  that all currently accumulated output in the output stream is
  discarded."
  [stream]
  #?(:bb (byte-array-output-stream/reset stream)
     :clj (.reset ^ByteArrayOutputStream stream)))

(defn size
  "Returns the current size of the buffer."
  [stream]
  #?(:bb (byte-array-output-stream/size stream)
     :clj (.size ^ByteArrayOutputStream stream)))

(defn to-byte-array
  "creates a newly allocated byte-array containing the data and returns
  it."
  [stream]
  #?(:bb (byte-array-output-stream/to-byte-array stream)
     :clj (.toByteArray
           ^ByteArrayOutputStream stream)))

(defn to-string
  "Converts the buffer's contents into a string decoding bytes using the
  platform's default character set if encoding is not specified, else
  uses encoding."
  ([stream]
   #?(:bb (byte-array-output-stream/to-string stream)
      :clj (.toString ^ByteArrayOutputStream stream)))
  ([stream encoding]
   #?(:bb (byte-array-output-stream/to-string stream encoding)
      :clj (.toString ^ByteArrayOutputStream stream ^String encoding))))

(defn write
  "write a byte or bytes to the output stream."
  ([stream int-or-base64]
   #?(:bb (byte-array-output-stream/write stream int-or-base64)
      :clj (if (string? int-or-base64)
             (let [buffer int-or-base64
                   size (count buffer)]
               (.write
                ^ByteArrayOutputStream stream
                ^bytes buffer
                0
                size))
             (.write
              ^ByteArrayOutputStream stream
              ^int int-or-base64)))))

(defn write-to
  "Writes the complete contents of this byte array output stream to the
  specified output stream argument."
  [stream out]
  #?(:bb (byte-array-output-stream/write-to stream out)
     :clj (.writeTo ^ByteArrayOutputStream stream ^OutputStream out)))

(ns clojuressh.byte-array-input-stream
  (:refer-clojure :exclude [read])
  (:require [clojuressh.impl.load-pod]
            #?(:bb [pod.epiccastle.bbssh.byte-array-input-stream :as byte-array-input-stream]))
  #?(:bb (:import)
     :clj (:import [java.io
                    PipedInputStream PipedOutputStream
                    ByteArrayInputStream ByteArrayOutputStream
                    InputStream]
                   [java.util Arrays])))

(set! *warn-on-reflection* true)

#?(:bb nil
   :clj
   (defn- new-from-string [string & [encoding]]
     (ByteArrayInputStream.
       ^bytes (.getBytes ^String string (or ^String encoding "utf-8")))))

#?(:bb nil
   :clj
   (defn- new-from-bytes [string]
     (ByteArrayInputStream.
       ^bytes string)))

(defn new
  [string-or-bytes & [encoding]]
  #?(:bb (byte-array-input-stream/new string-or-bytes encoding)
     :clj (if (string? string-or-bytes)
            (new-from-string string-or-bytes encoding)
            (new-from-bytes string-or-bytes))))

(defn available
  "Returns the number of remaining bytes that can be read (or skipped over) from this input stream."
  [stream]
  #?(:bb (byte-array-input-stream/available stream)
     :clj (.available ^ByteArrayInputStream stream)))

(defn close
  "Closing a ByteArrayInputStream has no effect."
  [stream]
  #?(:bb (byte-array-input-stream/close stream)
     :clj (.close ^ByteArrayInputStream stream)))

(defn mark
  "Set the current marked position in the stream."
  [stream read-ahead-limit]
  #?(:bb (byte-array-input-stream/mark stream read-ahead-limit)
     :clj (.mark ^ByteArrayInputStream stream read-ahead-limit)))

(defn mark-supported
  "Tests if this InputStream supports mark/reset."
  [stream]
  #?(:bb (byte-array-input-stream/mark-supported stream)
     :clj (.markSupported ^ByteArrayInputStream stream)))

(defn read
  "`(read stream)`
  Read a single byte from the stream. Returns an int. Blocks
  if a byte is not available.

  `(read stream byte-array offset length)`
  Try and read `length` bytes from the `stream` and store them into
  a `byte-array` starting at `offset`. Returns the number of bytes
  successfully read. Does not block.
  "
  ([stream]
   #?(:bb (byte-array-input-stream/read stream)
      :clj (.read ^ByteArrayInputStream stream)))
  ([stream bytes]
   #?(:bb (byte-array-input-stream/read stream bytes)
      :clj (let [arr (byte-array bytes)
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
                 (Arrays/copyOfRange arr 0 bytes-read))]))))

(defn reset
  "Resets the buffer to the marked position."
  [stream]
  #?(:bb (byte-array-input-stream/reset stream)
     :clj (.reset ^ByteArrayInputStream stream)))

(defn skip
  "Skips `n` bytes of input from this input stream."
  [stream n]
  #?(:bb (byte-array-input-stream/skip stream n)
     :clj (.skip ^ByteArrayInputStream stream n)))

(ns clojuressh.input-stream
  (:refer-clojure :exclude [read])
  (:require [clojuressh.impl.load-pod]
            #?(:bb [pod.epiccastle.bbssh.input-stream :as input-stream]))
  #?(:bb (:import)
     :clj (:import [java.io
                    PipedInputStream PipedOutputStream
                    ByteArrayInputStream ByteArrayOutputStream
                    InputStream]
                   [java.util Arrays])))

(set! *warn-on-reflection* true)

(defn new
  "Create a new PipedInputStream on the pod heap. Return
  a reference to it for babashka use."
  ([]
   #?(:bb (input-stream/new)
      :clj (PipedInputStream.)))
  ([src-or-pipe-size]
   #?(:bb (input-stream/new src-or-pipe-size)
      :clj (if (int? src-or-pipe-size)
             (PipedInputStream.
              ^int src-or-pipe-size)
             (PipedInputStream.
              ^PipedOutputStream src-or-pipe-size))))
  ([src pipe-size]
   #?(:bb (input-stream/new src pipe-size)
      :clj (PipedInputStream. ^PipedOutputStream src ^int pipe-size))))

(defn close
  "Close the stream"
  [stream]
  #?(:bb (input-stream/close stream)
     :clj (.close ^PipedInputStream stream)))

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
   #?(:bb (input-stream/read stream)
      :clj (.read ^PipedInputStream stream)))
  ([stream bytes]
   #?(:bb (input-stream/read stream bytes)
      :clj (.read ^PipedInputStream stream ^bytes bytes)))
  ([stream bytes offset length]
   #?(:bb (input-stream/read stream bytes offset length)
      :clj (.read ^PipedInputStream stream ^bytes bytes ^int offset ^int length))))

(defn available
  "Return the number of bytes available and waiting to be read
  immediately in the stream"
  [stream]
  #?(:bb (input-stream/available stream)
     :clj (.available ^PipedInputStream stream)))

(defn connect
  "Connect a PipedOutputStream to this stream."
  [stream source]
  #?(:bb (input-stream/connect stream source)
     :clj (.connect ^PipedInputStream stream ^PipedOutputStream source)))

(defn make-proxy
  "Make a babashka java.io.PipedInputStream that calls
  the pod heap input-stream `stream`."
  [stream]
  #?(:bb (input-stream/make-proxy stream)
     :clj (proxy [java.io.PipedInputStream] []
            (close []
              (close stream))
            (read
              ([]
               (read stream))
              ([bytes]
               (read stream bytes))
              ([bytes offset length]
               (read stream bytes offset length)))
            (available []
              (available stream)))))

#_(defn new-pod-proxy
  [callbacks]
  (proxy [InputStream] []
    (available []
      ((:available callbacks)))
    (close []
      ((:close callbacks)))
    (mark [readlimit]
      ((:mark callbacks) readlimit))
    (markSupported []
      ((:mark-supported callbacks)))
    (read
      ([]
       ((:read callbacks)))
      ([^bytes bytes]
       (let [[bytes-read base64]
             ((:read callbacks) (count bytes))
             buffer base64]
         (when buffer
           (System/arraycopy buffer 0 bytes 0 bytes-read))
         bytes-read))
      ([^bytes bytes offset length]
       (let [[bytes-read base64]
             ((:read callbacks) length)
             buffer base64]
         (when buffer
           (System/arraycopy buffer 0 bytes offset bytes-read))
         bytes-read)))
    (reset []
      ((:reset callbacks)))
    (skip [n]
      ((:skip callbacks) n))))

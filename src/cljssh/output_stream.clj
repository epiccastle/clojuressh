(ns cljssh.output-stream
  (:refer-clojure :exclude [flush])
  (:require [cljssh.callbacks :as callbacks]
            [cljssh.cleaner :as cleaner])
  (:import [java.io
            PipedOutputStream PipedInputStream
            OutputStream]))

(set! *warn-on-reflection* true)

(defn new
  "Create a new PipedOutputStream.
  Optional first argument can be a bbssh PipedInputStream to
  connect as the sink.
  "
  ([]
   (PipedOutputStream.))
  ([sink]
   (PipedOutputStream. sink)))

(defn close
  "Close the stream"
  [^PipedOutputStream stream]
  (.close stream))

(defn write
  "`(write stream bytes)`
  Write a byte-array `bytes` to the stream.

  `(write stream byte-array offset length)`
  Write `length` bytes from `byte-array` beginning at `offset`
  to `stream`.
  "
  ([^PipedOutputStream stream base64]
   (.write
    stream
    ^bytes base64))
  ([stream base64 _length]
   (let [arr base64]
     (.write
      ^PipedOutputStream stream
      ^bytes arr
      0
      (count arr)))))

(defn connect
  "Connect a bbssh PipedInputStream to this to act as a sink"
  [^PipedOutputStream stream ^PipedInputStream sink]
  (.connect stream sink))

(defn flush
  "Flush the stream"
  [^PipedOutputStream stream]
  (.flush stream))

(defn new-pod-proxy
  [reply-fn]
  (let [result
        (proxy [OutputStream] []
          (close []
            (callbacks/call-method reply-fn :close []))
          (flush []
            (callbacks/call-method reply-fn :flush []))
          (write
            ([byte-array-or-number]
             (callbacks/call-method
              reply-fn :write
              [(if (number? byte-array-or-number)
                 byte-array-or-number
                 byte-array-or-number)]))
            ([^bytes byte-array offset length]
             (callbacks/call-method
              reply-fn :write
              [(java.util.Arrays/copyOfRange byte-array ^int offset ^int (+ offset length))])))]
    (cleaner/register-delete-fn result #(reply-fn [:done] ["done"]))
    (reply-fn [:result result])
    nil))

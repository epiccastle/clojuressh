(ns cljssh.output-stream
  (:refer-clojure :exclude [flush])
  (:require [cljssh.callbacks :as callbacks]
            [cljssh.cleaner :as cleaner])
  (:import [java.io
            PipedOutputStream PipedInputStream
            OutputStream]))

;; cljssh.* are invoked on pod side.

(set! *warn-on-reflection* true)

(defn new
  ([]
   (PipedOutputStream.))
  ([sink]
   (PipedOutputStream. sink)))

(defn close [^PipedOutputStream stream]
  (.close stream))

(defn write
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

(defn connect [^PipedOutputStream stream ^PipedInputStream sink]
  (.connect stream sink))

(defn flush [^PipedOutputStream stream]
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

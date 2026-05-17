(ns clojuressh.output-stream
  (:refer-clojure :exclude [flush])
  #?(:bb (:require [babashka.pods :as pods]))
  #?(:bb (:import)
     :clj (:import [java.io
                    PipedOutputStream PipedInputStream
                    OutputStream])))

#?(:bb (pods/load-pod 'epiccastle/bbssh "0.7.0"))
#?(:bb (require '[pod.epiccastle.bbssh.output-stream :as output-stream]))

(set! *warn-on-reflection* true)

(defn new
  "Create a new PipedOutputStream.
  Optional first argument can be a PipedInputStream to
  connect as the sink.
  "
  ([]
   #?(:bb (output-stream/new)
      :clj (PipedOutputStream.)))
  ([sink]
   #?(:bb (output-stream/new sink)
      :clj (PipedOutputStream. ^PipedInputStream sink))))

(defn close
  "Close the stream"
  [stream]
  #?(:bb (output-stream/close stream)
     :clj (.close ^PipedOutputStream stream)))

(defn write
  "`(write stream bytes)`
  Write a byte-array `bytes` to the stream.

  `(write stream byte-array offset length)`
  Write `length` bytes from `byte-array` beginning at `offset`
  to `stream`.
  "
  ([stream bytes]
   #?(:bb (output-stream/write stream bytes)
      :clj (.write ^PipedOutputStream stream ^bytes bytes)))
  ([stream byte-array offset length]
   #?(:bb (output-stream/write stream byte-array offset length)
      :clj (.write ^PipedOutputStream stream ^bytes byte-array ^int offset ^int length))))

(defn connect
  "Connect a PipedInputStream to this to act as a sink"
  [stream sink]
  #?(:bb (output-stream/connect stream sink)
     :clj (.connect ^PipedOutputStream stream ^PipedInputStream sink)))

(defn flush
  "Flush the stream"
  [stream]
  #?(:bb (output-stream/flush stream)
     :clj (.flush ^PipedOutputStream stream)))

(defn make-proxy
  "Make a java.io.PipedOutputStream"
  [stream]
  #?(:bb (output-stream/make-proxy stream)
     :clj (proxy [java.io.PipedOutputStream] []
            (close []
              (close stream))
            (write
              ([bytes]
               (write stream bytes))
              ([byte-array offset length]
               (write stream byte-array offset length)))
            (connect [sink]
              (connect stream sink))
            (flush []
              (flush stream)))))

#_(defn new-pod-proxy
  [callbacks]
  (proxy [OutputStream] []
    (close []
      ((:close callbacks)))
    (flush []
      ((:flush callbacks)))
    (write
      ([byte-array-or-number]
       ((:write callbacks)
        (if (number? byte-array-or-number)
          byte-array-or-number
          byte-array-or-number)))
      ([^bytes byte-array offset length]
       ((:write callbacks)
        (java.util.Arrays/copyOfRange byte-array ^int offset ^int (+ offset length)))))))

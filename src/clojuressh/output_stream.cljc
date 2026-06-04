(ns clojuressh.output-stream
  (:refer-clojure :exclude [flush])
  (:require [clojuressh.impl.load-pod]
            #?(:bb [pod.epiccastle.bbssh.output-stream :as output-stream]))
  #?(:bb (:import)
     :clj (:import [java.io
                    PipedOutputStream PipedInputStream
                    OutputStream])))

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

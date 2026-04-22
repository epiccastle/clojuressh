(ns cljssh.input-stream
  (:refer-clojure :exclude [read])
  (:require [cljssh.callbacks :as callbacks]
            [cljssh.cleaner :as cleaner])
  (:import [java.io
            PipedInputStream PipedOutputStream
            ByteArrayInputStream ByteArrayOutputStream
            InputStream]
           [java.util Arrays]))

(set! *warn-on-reflection* true)

(defn new
  ([]
   (PipedInputStream.))
  ([src-or-pipe-size]
   (if (int? src-or-pipe-size)
     (PipedInputStream.
      ^int src-or-pipe-size)
     (PipedInputStream.
      ^PipedOutputStream src-or-pipe-size)))
  ([^PipedOutputStream src ^int pipe-size]
   (PipedInputStream. src pipe-size)))

(defn close [^PipedInputStream stream]
  (.close stream))

(defn read
  ([^PipedInputStream stream]
   (.read stream))
  ([stream bytes]
   (let [arr (byte-array bytes)
         bytes-read
         (.read
          ^PipedInputStream stream
          arr
          0
          bytes)]
     [bytes-read
      (case bytes-read
        -1 nil
        0 ""
        (Arrays/copyOfRange arr 0 bytes-read))])))

(defn available [^PipedInputStream stream]
  (.available stream))

(defn connect [^PipedInputStream stream ^PipedOutputStream source]
  (.connect stream source))

(defn new-pod-proxy
  [reply-fn]
  (let [result
        (proxy [InputStream] []
          (available []
            (callbacks/call-method reply-fn :available []))
          (close []
            (callbacks/call-method reply-fn :close []))
          (mark [readlimit]
            (callbacks/call-method reply-fn :mark [readlimit]))
          (markSupported []
            (callbacks/call-method reply-fn :mark-supported []))
          (read
            ([]
             (callbacks/call-method reply-fn :read []))
            ([^bytes bytes]
             (let [[bytes-read base64]
                   (callbacks/call-method
                    reply-fn :read
                    [(count bytes)])
                   buffer base64]
               (when buffer
                 (System/arraycopy buffer 0 bytes 0 bytes-read))
               bytes-read))
            ([^bytes bytes offset length]
             (let [[bytes-read base64]
                   (callbacks/call-method
                    reply-fn :read
                    [length])
                   buffer base64]
               (when buffer
                 (System/arraycopy buffer 0 bytes offset bytes-read))
               bytes-read)))
          (reset []
            (callbacks/call-method reply-fn :reset []))
          (skip [n]
            (callbacks/call-method reply-fn :skip [n])))]
    (cleaner/register-delete-fn result #(reply-fn [:done] ["done"]))
    (reply-fn [:result result])
    nil))

(ns cljssh.input-stream
  (:refer-clojure :exclude [read])
  (:import [java.io
            PipedInputStream PipedOutputStream
            ByteArrayInputStream ByteArrayOutputStream
            InputStream]
           [java.util Arrays]))

(set! *warn-on-reflection* true)

(defn new
  "Create a new PipedInputStream on the pod heap. Return
  a reference to it for babashka use."
  ([]
   (PipedInputStream.))
  ([src-or-pipe-size]
   (if (int? src-or-pipe-size)
     (PipedInputStream.
      ^int src-or-pipe-size)
     (PipedInputStream.
      ^PipedOutputStream src-or-pipe-size)))
  ([^PipedOutputStream src pipe-size]
   (PipedInputStream. src ^int pipe-size)))

(defn close
  "Close the stream"
  [^PipedInputStream stream]
  (.close stream))

(defn read
  "`(read stream)`
  Read a single byte from the stream. Returns an int. Blocks
  if a byte is not available.

  `(read stream byte-array offset length)`
  Try and read `length` bytes from the `stream` and store them into
  a `byte-array` starting at `offset`. Returns the number of bytes
  successfully read. Does not block.
  "
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

(defn available
  "Return the number of bytes available and waiting to be read
  immediately in the stream"
  [^PipedInputStream stream]
  (.available stream))

(defn connect
  "Connect a bbssh PipedOutputStream to this stream."
  [^PipedInputStream stream ^PipedOutputStream source]
  (.connect stream source))

(defn new-pod-proxy
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

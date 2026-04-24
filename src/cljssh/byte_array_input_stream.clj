(ns cljssh.byte-array-input-stream
  (:refer-clojure :exclude [read])
  (:import [java.io
            PipedInputStream PipedOutputStream
            ByteArrayInputStream ByteArrayOutputStream
            InputStream]
           [java.util Arrays]))

(set! *warn-on-reflection* true)

(defn- new-from-string [^String string & [^String encoding]]
  (ByteArrayInputStream.
   ^bytes (.getBytes string (or encoding "utf-8"))))

(defn- new-from-bytes [^bytes string]
  (ByteArrayInputStream.
   ^bytes string))

(defn new
  [string-or-bytes & [encoding]]
  (if (string? string-or-bytes)
    (new-from-string string-or-bytes encoding)
    (new-from-bytes string-or-bytes)))

(defn available
  "Returns the number of remaining bytes that can be read (or skipped over) from this input stream."
  [^ByteArrayInputStream stream]
  (.available stream))

(defn close
  "Closing a ByteArrayInputStream has no effect."
  [^ByteArrayInputStream stream]
  (.close stream))

(defn mark
  "Set the current marked position in the stream."
  [^ByteArrayInputStream stream read-ahead-limit]
  (.mark stream read-ahead-limit))

(defn mark-supported
  "Tests if this InputStream supports mark/reset."
  [^ByteArrayInputStream stream]
  (.markSupported stream))

(defn read
  "`(read stream)`
  Read a single byte from the stream. Returns an int. Blocks
  if a byte is not available.

  `(read stream byte-array offset length)`
  Try and read `length` bytes from the `stream` and store them into
  a `byte-array` starting at `offset`. Returns the number of bytes
  successfully read. Does not block.
  "
  ([^ByteArrayInputStream stream]
   (.read stream))
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
         (Arrays/copyOfRange arr 0 bytes-read))])))

(defn reset
  "Resets the buffer to the marked position."
  [^ByteArrayInputStream stream]
  (.reset stream))

(defn skip
  "Skips `n` bytes of input from this input stream."
  [^ByteArrayInputStream stream n]
  (.skip stream n))

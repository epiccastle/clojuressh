(ns cljssh.byte-array-output-stream
  (:refer-clojure :exclude [flush])
  (:import [java.io
            ByteArrayOutputStream OutputStream]))

(set! *warn-on-reflection* true)

(defn new
  ([]
   (ByteArrayOutputStream.))
  ([size]
   (ByteArrayOutputStream. size)))

(defn close
  "Closing a ByteArrayOutputStream has no effect."
  [^ByteArrayOutputStream stream]
  (.close stream))

(defn reset
  "Resets the count field of this byte array output stream to zero, so
  that all currently accumulated output in the output stream is
  discarded."
  [^ByteArrayOutputStream stream]
  (.reset stream))

(defn size
  "Returns the current size of the buffer."
  [^ByteArrayOutputStream stream]
  (.size stream))

(defn to-byte-array
  "creates a newly allocated byte-array containing the data and returns
  it."
  [stream]
  (.toByteArray
   ^ByteArrayOutputStream stream))

(defn to-string
  "Converts the buffer's contents into a string decoding bytes using the
  platform's default character set if encoding is not specified, else
  uses encoding."
  ([^ByteArrayOutputStream stream]
   (.toString stream))
  ([^ByteArrayOutputStream stream ^String encoding]
   (.toString stream encoding)))

(defn write
  "write a byte or bytes to the output stream."
  ([stream int-or-base64]
   (if (string? int-or-base64)
     (let [buffer int-or-base64
           size (count buffer)]
       (.write
        ^ByteArrayOutputStream stream
        ^bytes buffer
        0
        size))
     (.write
      ^ByteArrayOutputStream stream
      ^int int-or-base64))))

(defn write-to
  "Writes the complete contents of this byte array output stream to the
  specified output stream argument."
  [^ByteArrayOutputStream stream ^OutputStream out]
  (.writeTo stream out))

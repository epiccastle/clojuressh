(ns clojuressh.impl.socket
  "Unix domain socket client operations implemented using
  java.net.UnixDomainSocketAddress and java.nio.channels.SocketChannel."
  (:refer-clojure :exclude [read])
  (:import [java.net UnixDomainSocketAddress StandardProtocolFamily]
           [java.nio ByteBuffer]
           [java.nio.channels SocketChannel]))

(defn open
  "Open a connection to the unix domain socket at `sock-path`.
  Returns an open SocketChannel on success, or nil on failure."
  [sock-path]
  (try
    (let [address (UnixDomainSocketAddress/of ^String sock-path)
          channel (SocketChannel/open StandardProtocolFamily/UNIX)]
      (.connect channel address)
      channel)))

(defn close
  "Close the given SocketChannel."
  [^SocketChannel channel]
  (when channel
    (.close channel)))

(defn write
  "Write all bytes in the byte-array `buffer` to `channel`.
  Returns the total number of bytes written."
  [^SocketChannel channel ^bytes buffer]
  (let [bb (ByteBuffer/wrap buffer)
        total (alength buffer)]
    (loop [written 0]
      (if (.hasRemaining bb)
        (let [n (.write channel bb)]
          (if (neg? n)
            written
            (recur (+ written n))))
        total))))

(defn read
  "Read exactly `size` bytes from `channel` and return a byte-array.
  Blocks until all bytes are read or end-of-stream is reached. If the
  stream ends before `size` bytes have been read, throws an exception."
  [^SocketChannel channel size]
  (let [buffer (byte-array size)
        bb (ByteBuffer/wrap buffer)]
    (loop []
      (when (.hasRemaining bb)
        (let [n (.read channel bb)]
          (when (neg? n)
            (throw (ex-info "Unix domain socket closed before expected bytes were read"
                            {:type ::premature-eof
                             :expected size
                             :read (.position bb)})))
          (recur))))
    buffer))

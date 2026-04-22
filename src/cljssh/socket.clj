(ns cljssh.socket
  (:refer-clojure :exclude [read]))

(defn open [sock-path]
  (BbsshUtils/ssh-open-auth-socket sock-path)
  )

(defn close [sock-fd]
  (BbsshUtils/ssh-close-auth-socket sock-fd)
  )

(defn write [sock-fd base64]
  (let [buffer base64]
    (BbsshUtils/ssh-auth-socket-write sock-fd buffer (count buffer))
    ))

(defn read [sock-fd size]
  (let [buffer (byte-array size)]
    (BbsshUtils/ssh-auth-socket-read sock-fd buffer size)
    buffer))

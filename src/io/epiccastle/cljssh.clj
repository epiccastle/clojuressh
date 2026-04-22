(ns io.epiccastle.cljssh
  "A Clojure library for using SSH in Clojure that is API compatible with bbssh.

  See https://github.com/epiccastle/bbssh for API reference.")

(defn ssh
  "Execute a command over SSH. API compatible with bbssh."
  [host cmd & {:as opts}]
  (throw (ex-info "Not yet implemented" {:host host :cmd cmd :opts opts})))

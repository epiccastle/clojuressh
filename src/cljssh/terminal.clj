(ns cljssh.terminal)

(defn is-terminal?
  "Returns true is stdout is connected to a terminal"
  []
  (pos? (BbsshUtils/is-stdout-a-tty))
  )

(defn get-width
  "return the width of the terminal"
  []
  (BbsshUtils/get-terminal-width)
  )

(defn get-height []
  (BbsshUtils/get-terminal-height)
  )

(defn enter-raw-mode
  "switch the present terminal into raw mode"
  [n]
  (BbsshUtils/enter-raw-mode n)
  )

(defn leave-raw-mode
  "switch the present terminal out of raw mode"
  [n]
  (BbsshUtils/leave-raw-mode n)
  )

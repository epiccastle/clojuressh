(ns clojuressh.channel-exec
  (:require [clojuressh.impl.load-pod]
            #?(:bb [pod.epiccastle.bbssh.channel-exec :as channel-exec]))
  #?(:bb (:import)
     :clj (:import [com.jcraft.jsch JSch Session UserInfo ChannelExec Channel ChannelSession]
                   [java.io InputStream OutputStream])))

(set! *warn-on-reflection* true)

(defn set-command [channel command]
  #?(:bb (channel-exec/set-command channel command)
     :clj (.setCommand ^ChannelExec channel ^String command)))

(defn connect [channel]
  #?(:bb (channel-exec/connect channel)
     :clj (.connect ^ChannelExec channel)))

(defn disconnect [channel]
  #?(:bb (channel-exec/disconnect channel)
     :clj (.disconnect ^ChannelExec channel)))

(defn set-input-stream
  ([channel input-stream]
   #?(:bb (channel-exec/set-input-stream channel input-stream)
      :clj (set-input-stream channel input-stream false)))
  ([channel input-stream dont-close?]
   #?(:bb (channel-exec/set-input-stream channel input-stream dont-close?)
      :clj (.setInputStream ^ChannelExec channel ^InputStream input-stream ^Boolean dont-close?))))

(defn set-output-stream
  ([channel output-stream]
   #?(:bb (channel-exec/set-output-stream channel output-stream)
      :clj (set-output-stream channel output-stream false)))
  ([channel output-stream dont-close?]
   #?(:bb (channel-exec/set-output-stream channel output-stream dont-close?)
      :clj (.setOutputStream ^ChannelExec channel ^OutputStream output-stream ^Boolean dont-close?))))

(defn set-error-stream
  ([channel error-stream]
   #?(:bb (channel-exec/set-error-stream channel error-stream)
      :clj (set-error-stream channel error-stream false)))
  ([channel error-stream dont-close?]
   #?(:bb (channel-exec/set-error-stream channel error-stream dont-close?)
      :clj (.setErrStream ^ChannelExec channel ^OutputStream error-stream ^Boolean dont-close?))))

(defn get-input-stream [channel]
  #?(:bb (channel-exec/get-input-stream channel)
     :clj (.getInputStream ^ChannelExec channel)))

(defn get-error-stream [channel]
  #?(:bb (channel-exec/get-error-stream channel)
     :clj (.getErrStream ^ChannelExec channel)))

(defn get-output-stream [channel]
  #?(:bb (channel-exec/get-output-stream channel)
     :clj (.getOutputStream ^ChannelExec channel)))

(defn set-pty [channel enable]
  #?(:bb (channel-exec/set-pty channel enable)
     :clj (.setPty ^ChannelExec channel ^boolean enable)))

(defn set-pty-size [channel col row width-pixels height-pixels]
  #?(:bb (channel-exec/set-pty-size channel col row width-pixels height-pixels)
     :clj (.setPtySize ^ChannelExec channel ^int col ^int row ^int width-pixels ^int height-pixels)))

(defn set-pty-type
  ([channel terminal-type]
   #?(:bb (channel-exec/set-pty-type channel terminal-type)
      :clj (.setPtyType ^ChannelExec channel ^String terminal-type)))
  ([channel terminal-type col row width-pixels height-pixels]
   #?(:bb (channel-exec/set-pty-type channel terminal-type col row width-pixels height-pixels)
      :clj (.setPtyType ^ChannelExec channel ^String terminal-type ^int col ^int row ^int width-pixels ^int height-pixels))))

(defn set-terminal-mode
  [channel terminal-mode]
  #?(:bb (channel-exec/set-terminal-mode channel terminal-mode)
     :clj (.setTerminalMode
           ^ChannelExec channel
           ^bytes terminal-mode)))

(defn set-agent-forwarding [channel enable]
  #?(:bb (channel-exec/set-agent-forwarding channel enable)
     :clj (.setAgentForwarding ^ChannelExec channel ^boolean enable)))

(defn set-x-forwarding [channel enable]
  #?(:bb (channel-exec/set-x-forwarding channel enable)
     :clj (.setAgentForwarding ^ChannelExec channel ^boolean enable)))

(defn set-env [channel name value]
  #?(:bb (channel-exec/set-env channel name value)
     :clj (.setEnv ^ChannelExec channel ^String name ^String value)))

(defn is-closed [channel]
  #?(:bb (channel-exec/is-closed channel)
     :clj (.isClosed ^ChannelExec channel)))

(defn is-connected [channel]
  #?(:bb (channel-exec/is-connected channel)
     :clj (.isConnected ^ChannelExec channel)))

(defn send-signal [channel signal]
  #?(:bb (channel-exec/send-signal channel signal)
     :clj (.sendSignal ^ChannelExec channel ^String signal)))

(defn get-exit-status [channel]
  #?(:bb (channel-exec/get-exit-status channel)
     :clj (.getExitStatus ^ChannelExec channel)))

(defn get-id [channel]
  #?(:bb (channel-exec/get-id channel)
     :clj (.getId ^ChannelExec channel)))

(defn is-eof [channel]
  #?(:bb (channel-exec/is-eof channel)
     :clj (.isEOF ^ChannelExec channel)))

(defn wait
  "waits until a ssh exec remote process has finished executing and then
  returns the exit code. Optionally pass a timeout value in
  milliseconds. If the timeout is reached and the process has not
  finished then returns `nil`."
  [channel & [timeout]]
  #?(:bb (channel-exec/wait channel timeout)
     :clj (let [status (get-exit-status channel)]
            (cond
              (<= 0 status)
              status

              (and timeout (<= timeout 0))
              nil

              timeout
              (let [deadline (+ (System/nanoTime) (* timeout 1000000))]
                (loop [remaining (- deadline (System/nanoTime))]
                  (when (pos? remaining)
                    (Thread/sleep ^long (min (inc (/ remaining 1000000)) 100))
                    (let [status (get-exit-status channel)]
                      (if (<= 0 status)
                        status
                        (recur (- deadline (System/nanoTime))))))))

              :else ;; no timeout, block forever
              (loop []
                (Thread/sleep 100)
                (let [status (get-exit-status channel)]
                  (if (<= 0 status)
                    status
                    (recur))))))))

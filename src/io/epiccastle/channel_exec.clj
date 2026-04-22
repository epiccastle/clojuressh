(ns io.epiccastle.channel-exec
  (:import [com.jcraft.jsch JSch Session UserInfo ChannelExec Channel ChannelSession]
           [java.io InputStream OutputStream])
  )

;; io.epiccastle.* are invoked on pod side.

(set! *warn-on-reflection* true)

(defn set-command [channel command]
  (.setCommand
   ^ChannelExec channel
   ^String command))

(defn connect [channel]
  (.connect
   ^ChannelExec channel))

(defn disconnect [channel]
  (.disconnect
   ^ChannelExec channel))

(defn set-input-stream [channel input-stream dont-close?]
  (.setInputStream
   ^ChannelExec channel
   ^InputStream input-stream
   ^Boolean dont-close?))

(defn set-output-stream [channel output-stream dont-close?]
  (.setOutputStream
   ^ChannelExec channel
   ^OutputStream output-stream
   ^Boolean dont-close?))

(defn set-error-stream [channel error-stream dont-close?]
  (.setErrStream
   ^ChannelExec channel
   ^OutputStream error-stream
   ^Boolean dont-close?))

(defn get-input-stream [channel]
  (.getInputStream
   ^ChannelExec channel))

(defn get-error-stream [channel]
  (.getErrStream
   ^ChannelExec channel))

(defn get-output-stream [channel]
  (.getOutputStream
   ^ChannelExec channel))

(defn set-pty [channel enable]
  (.setPty
   ^ChannelExec channel
   ^boolean enable))

(defn set-pty-size [channel col row width-pixels height-pixels]
  (.setPtySize
   ^ChannelExec channel
   ^int col
   ^int row
   ^int width-pixels
   ^int height-pixels))

(defn set-pty-type
  ([channel terminal-type]
   (.setPtyType
    ^ChannelExec channel
    ^String terminal-type))
  ([channel terminal-type col row width-pixels height-pixels]
   (.setPtyType
    ^ChannelExec channel
    ^String terminal-type
    ^int col
    ^int row
    ^int width-pixels
    ^int height-pixels)))

(defn set-terminal-mode
  [channel terminal-mode]
  (.setTerminalMode
   ^ChannelExec channel
   ^bytes (utils/decode-base64 terminal-mode)))

(defn set-agent-forwarding [channel enable]
  (.setAgentForwarding
   ^ChannelExec channel
   ^boolean enable))

(defn set-x-forwarding [channel enable]
  (.setAgentForwarding
   ^ChannelExec channel
   ^boolean enable))

(defn set-env [channel name value]
  (.setEnv
   ^ChannelExec channel
   ^String name
   ^String value))

(defn is-closed [channel]
  (.isClosed
   ^ChannelExec channel))

(defn is-connected [channel]
  (.isConnected
   ^ChannelExec channel))

(defn send-signal [channel signal]
  (.sendSignal
   ^ChannelExec channel
   ^String signal))

(defn get-exit-status [channel]
  (.getExitStatus
   ^ChannelExec channel))

(defn get-id [channel]
  (.getId
   ^ChannelExec channel))

(defn is-eof [channel]
  (.isEOF
   ^ChannelExec channel))

(ns cljssh.channel-exec
  (:import [com.jcraft.jsch JSch Session UserInfo ChannelExec Channel ChannelSession]
           [java.io InputStream OutputStream])
  )

(set! *warn-on-reflection* true)

(defn set-command [^ChannelExec channel ^String command]
  (.setCommand channel command))

(defn connect [^ChannelExec channel]
  (.connect channel))

(defn disconnect [^ChannelExec channel]
  (.disconnect channel))

(defn set-input-stream [^ChannelExec channel ^InputStream input-stream ^Boolean dont-close?]
  (.setInputStream channel input-stream dont-close?))

(defn set-output-stream [^ChannelExec channel ^OutputStream output-stream ^Boolean dont-close?]
  (.setOutputStream channel output-stream dont-close?))

(defn set-error-stream [^ChannelExec channel ^OutputStream error-stream ^Boolean dont-close?]
  (.setErrStream channel error-stream dont-close?))

(defn get-input-stream [^ChannelExec channel]
  (.getInputStream channel))

(defn get-error-stream [^ChannelExec channel]
  (.getErrStream channel))

(defn get-output-stream [^ChannelExec channel]
  (.getOutputStream channel))

(defn set-pty [^ChannelExec channel enable]
  (.setPty channel ^boolean enable))

(defn set-pty-size [^ChannelExec channel col row width-pixels height-pixels]
  (.setPtySize channel ^int col ^int row ^int width-pixels ^int height-pixels))

(defn set-pty-type
  ([^ChannelExec channel ^String terminal-type]
   (.setPtyType channel terminal-type))
  ([^ChannelExec channel ^String terminal-type col row width-pixels height-pixels]
   (.setPtyType channel terminal-type ^int col ^int row ^int width-pixels ^int height-pixels)))

(defn set-terminal-mode
  [^ChannelExec channel terminal-mode]
  (.setTerminalMode
   channel
   ^bytes terminal-mode))

(defn set-agent-forwarding [^ChannelExec channel enable]
  (.setAgentForwarding channel ^boolean enable))

(defn set-x-forwarding [^ChannelExec channel enable]
  (.setAgentForwarding channel ^boolean enable))

(defn set-env [^ChannelExec channel ^String name ^String value]
  (.setEnv channel name value))

(defn is-closed [^ChannelExec channel]
  (.isClosed channel))

(defn is-connected [^ChannelExec channel]
  (.isConnected channel))

(defn send-signal [^ChannelExec channel ^String signal]
  (.sendSignal channel signal))

(defn get-exit-status [^ChannelExec channel]
  (.getExitStatus channel))

(defn get-id [^ChannelExec channel]
  (.getId channel))

(defn is-eof [^ChannelExec channel]
  (.isEOF channel))

(ns clojuressh.terminal
  (:require [clojure.string :as str]
            #?(:bb [babashka.pods :as pods]))
  #?(:bb (:import)
     :clj (:import [java.io InputStream]
                   [com.sun.jna Function Memory Native NativeLibrary Pointer]
                   [com.sun.jna.ptr IntByReference])))

#?(:bb (pods/load-pod 'epiccastle/bbssh "0.7.0"))
#?(:bb (require '[pod.epiccastle.bbssh.terminal :as terminal]))

(defn is-terminal?
  "Returns true if stdout is connected to a terminal.

  Uses `System/console`, which returns nil when the JVM's standard
  streams are not attached to a terminal (e.g. redirected to a file
  or piped). No shell process is invoked."
  []
  #?(:bb (terminal/is-terminal?)
     :clj (some? (System/console))))

;; -----------------------------------------------------------------
;; Raw-mode support via JNA + POSIX termios
;; -----------------------------------------------------------------
;;
;; Port of the C `enter_raw_mode` / `leave_raw_mode` used by the
;; original BbsshUtils. The `quiet` argument (named `n` in the
;; original Clojure shim) suppresses the `perror(3)` diagnostic on
;; failure; errors do not throw.
;;
;;   enter_raw_mode(quiet):
;;     tcgetattr(stdin, &tio);     -- on failure: perror unless quiet; return
;;     _saved_tio = tio;
;;     tio.c_iflag |= IGNPAR;
;;     tio.c_iflag &= ~(ISTRIP | INLCR | IGNCR | ICRNL | IXON | IXANY | IXOFF);
;;     tio.c_iflag &= ~IUCLC;       (Linux only)
;;     tio.c_lflag &= ~(ISIG | ICANON | ECHO | ECHOE | ECHOK | ECHONL | IEXTEN);
;;     tio.c_oflag &= ~OPOST;
;;     tio.c_cc[VMIN]  = 1;
;;     tio.c_cc[VTIME] = 0;
;;     tcsetattr(stdin, TCSADRAIN, &tio);  -- on failure: perror unless quiet
;;
;;   leave_raw_mode(quiet):
;;     if (!_in_raw_mode) return;
;;     tcsetattr(stdin, TCSADRAIN, &_saved_tio);  -- on failure: perror unless quiet
;;
;; POSIX `struct termios` field offsets, widths and flag-bit values
;; all differ across platforms, so we pick a per-OS table at load
;; time. Linux (glibc/musl) and macOS are supported.

(def ^:private os-name
  (.toLowerCase ^String (or (System/getProperty "os.name") "")))

(def ^:private os
  (cond
    (.contains os-name "mac")     :mac
    (.contains os-name "darwin")  :mac
    (.contains os-name "linux")   :linux
    (.contains os-name "windows") :windows
    :else                         :linux))    ;; best-effort fallback

;; Per-OS termios descriptions. Values lifted from:
;;   - Linux:  glibc bits/termios-struct.h, bits/termios-c_iflag.h, etc.
;;   - macOS:  xnu bsd/sys/termios.h
(def ^:private termios-layout
  {:linux {:size          60
           :iflag-off      0  :iflag-width  4
           :oflag-off      4  :oflag-width  4
           :lflag-off     12  :lflag-width  4
           :cc-off        17
           :vmin-idx       6
           :vtime-idx      5
           :IGNPAR     0x0004
           :ISTRIP     0x0020
           :INLCR      0x0040
           :IGNCR      0x0080
           :ICRNL      0x0100
           :IXON       0x0400
           :IXANY      0x0800
           :IXOFF     0x1000
           :IUCLC      0x0200       ;; Linux only
           :OPOST      0x0001
           :ISIG       0x0001
           :ICANON     0x0002
           :ECHO       0x0008
           :ECHOE      0x0010
           :ECHOK      0x0020
           :ECHONL     0x0040
           :IEXTEN     0x8000
           :TCSADRAIN  1}
   :mac   {:size          72
           :iflag-off      0  :iflag-width  8
           :oflag-off      8  :oflag-width  8
           :lflag-off     24  :lflag-width  8
           :cc-off        32
           :vmin-idx      16
           :vtime-idx     17
           :IGNPAR     0x0004
           :ISTRIP     0x0020
           :INLCR      0x0040
           :IGNCR      0x0080
           :ICRNL      0x0100
           :IXON       0x0200
           :IXOFF      0x0400
           :IXANY      0x0800
           :IUCLC      0         ;; no IUCLC on macOS
           :OPOST      0x0001
           :ISIG       0x0080
           :ICANON     0x0100
           :ECHO       0x0008
           :ECHOE      0x0002
           :ECHOK      0x0004
           :ECHONL     0x0010
           :IEXTEN     0x0400
           :TCSADRAIN  1}})

(def ^:private ^:const STDIN_FILENO 0)

(def ^:private layout (get termios-layout os))

;; Allocate a buffer sized exactly for this platform's termios.
(defn- ^Memory alloc-termios []
  (Memory. (long (:size layout))))

;; JNA function lookup via the dynamic Function API — no typed
;; Library interface required. Both libc and kernel32 are loaded
;; lazily so merely requiring this namespace on the "wrong" OS
;; doesn't trigger a missing-library error.
;;
;; The `try` wrapper gives a clearer error if a future JDK upgrades
;; the current "restricted native-access" warning into a hard
;; failure. On JDK 22-25 today the load succeeds and prints a
;; warning to stderr; the catch is dormant. On a future JDK where
;; `--enable-native-access=ALL-UNNAMED` is mandatory, users get a
;; pointed message instead of an opaque IllegalCallerException.

(defn- load-native! [^String libname]
  (try
    (NativeLibrary/getInstance libname)
    (catch Throwable t
      (throw (ex-info
               (str "clojuressh.terminal could not load native library \"" libname "\". "
                    "This is usually because the JVM was started without "
                    "--enable-native-access=ALL-UNNAMED on a JDK version that "
                    "requires it. Add that flag to your :jvm-opts (in deps.edn) "
                    "or project.clj, or pass it directly to the `java` command.")
               {:libname libname}
               t)))))

(def ^:private libc
  (delay (load-native! "c")))

(def ^:private kernel32
  (delay (load-native! "kernel32")))

(defn- ^Function libc-fn [^String name]
  (.getFunction ^NativeLibrary @libc name))

(defn- ^Function k32-fn [^String name]
  (.getFunction ^NativeLibrary @kernel32 name))

(defn- tcgetattr* [fd ^Memory buf]
  (.invokeInt (libc-fn "tcgetattr") (object-array [(int fd) buf])))

(defn- tcsetattr* [fd action ^Memory buf]
  (.invokeInt (libc-fn "tcsetattr") (object-array [(int fd) (int action) buf])))

(defn- strerror [errno]
  (try
    (.invokeString (libc-fn "strerror") (object-array [(int errno)]) false)
    (catch Throwable _ (str "errno " errno))))

(defn- perror! [^String op]
  (let [e (Native/getLastError)]
    (binding [*out* *err*]
      (print op ": " (strerror e) \newline)
      (flush))))

;; -------- Windows console helpers --------

(def ^:private ^:const STD_INPUT_HANDLE  -10)        ;; (DWORD) -10
(def ^:private ^:const ENABLE_ECHO_INPUT 0x0004)
(def ^:private ^:const INVALID_HANDLE_VALUE -1)

(defn- ^Pointer get-std-handle [^long which]
  (.invokePointer (k32-fn "GetStdHandle")
                  (object-array [(int which)])))

(defn- get-console-mode
  "Returns the console mode (unsigned 32-bit) or nil on failure."
  [^Pointer h]
  (let [out (IntByReference.)
        ok  (.invokeInt (k32-fn "GetConsoleMode")
                        (object-array [h out]))]
    (when-not (zero? ok)
      (bit-and (long (.getValue out)) 0xFFFFFFFF))))

(defn- set-console-mode
  "Returns true on success."
  [^Pointer h ^long mode]
  (not (zero? (.invokeInt (k32-fn "SetConsoleMode")
                          (object-array [h (unchecked-int mode)])))))

;; Read/write a termios flag word of the appropriate width (4 or 8
;; bytes) at the given offset, using little-endian — correct for
;; x86_64 and aarch64, which is every relevant modern platform.
(defn- read-flag ^long [^Memory m ^long off ^long width]
  (case (int width)
    4 (bit-and (long (.getInt m off)) 0xFFFFFFFF)
    8 (.getLong m off)))

(defn- write-flag! [^Memory m ^long off ^long width ^long v]
  (case (int width)
    4 (.setInt m off (unchecked-int v))
    8 (.setLong m off v))
  nil)

;; State: a single saved termios snapshot + an "in raw mode" flag,
;; matching the `static` globals in the original C. The atom tracks
;; only what *this namespace* has done; it does not reflect raw-mode
;; changes made by other code. For a real query of the current
;; terminal state see the public `in-raw-mode?` function.
(defonce ^:private saved-tio (atom nil))       ;; byte[] or nil
(defonce ^:private raw-mode-flag (atom false))

(defn- enter-raw-mode-posix [quiet?]
  (let [buf (alloc-termios)]
    (if (neg? (tcgetattr* STDIN_FILENO buf))
      (do (when-not quiet? (perror! "tcgetattr")) nil)
      (do
        ;; Snapshot into a JVM byte[] so it outlives the native buffer.
        (let [snap (byte-array (:size layout))]
          (.read buf 0 snap 0 (:size layout))
          (reset! saved-tio snap))
        ;; c_iflag |= IGNPAR
        ;; c_iflag &= ~(ISTRIP|INLCR|IGNCR|ICRNL|IXON|IXANY|IXOFF [|IUCLC])
        (let [{:keys [iflag-off iflag-width oflag-off oflag-width
                      lflag-off lflag-width cc-off vmin-idx vtime-idx
                      IGNPAR ISTRIP INLCR IGNCR ICRNL IXON IXANY IXOFF IUCLC
                      OPOST ISIG ICANON ECHO ECHOE ECHOK ECHONL IEXTEN
                      TCSADRAIN]} layout
              iflag-clear (bit-or ISTRIP INLCR IGNCR ICRNL IXON IXANY IXOFF IUCLC)
              lflag-clear (bit-or ISIG ICANON ECHO ECHOE ECHOK ECHONL IEXTEN)
              iflag       (read-flag buf iflag-off iflag-width)
              oflag       (read-flag buf oflag-off oflag-width)
              lflag       (read-flag buf lflag-off lflag-width)
              iflag'      (bit-and (bit-or iflag IGNPAR) (bit-not iflag-clear))
              oflag'      (bit-and oflag (bit-not OPOST))
              lflag'      (bit-and lflag (bit-not lflag-clear))]
          (write-flag! buf iflag-off iflag-width iflag')
          (write-flag! buf oflag-off oflag-width oflag')
          (write-flag! buf lflag-off lflag-width lflag')
          ;; c_cc[VMIN] = 1; c_cc[VTIME] = 0;
          (.setByte buf (long (+ cc-off vmin-idx))  (byte 1))
          (.setByte buf (long (+ cc-off vtime-idx)) (byte 0))
          (if (neg? (tcsetattr* STDIN_FILENO TCSADRAIN buf))
            (do (when-not quiet? (perror! "tcsetattr")) nil)
            (do (reset! raw-mode-flag true) nil)))))))

(defn- leave-raw-mode-posix [quiet?]
  (when @raw-mode-flag
    (let [^bytes snap @saved-tio
          buf         (alloc-termios)]
      (.write buf 0 snap 0 (:size layout))
      (if (neg? (tcsetattr* STDIN_FILENO (:TCSADRAIN layout) buf))
        (when-not quiet? (perror! "tcsetattr"))
        (reset! raw-mode-flag false))))
  nil)

;; Windows version — mirrors the `_WIN32` branch of the original C.
;; The C code does NOT save the prior mode; it unconditionally clears
;; ENABLE_ECHO_INPUT on enter and unconditionally sets it on leave.
;; We match that behaviour exactly, and also match the C's silent
;; handling of errors (GetConsoleMode failure skips the SetConsoleMode
;; call; no perror).
(defn- enter-raw-mode-windows [_quiet?]
  (let [h (get-std-handle STD_INPUT_HANDLE)]
    (when-let [mode (get-console-mode h)]
      (set-console-mode h (bit-and mode (bit-not ENABLE_ECHO_INPUT)))))
  nil)

(defn- leave-raw-mode-windows [_quiet?]
  (let [h (get-std-handle STD_INPUT_HANDLE)]
    (when-let [mode (get-console-mode h)]
      (set-console-mode h (bit-or mode ENABLE_ECHO_INPUT))))
  nil)

(defn enter-raw-mode
  "Put stdin into raw mode, matching the semantics of the original
  BbsshUtils C implementation.

  `quiet` (zero or non-zero, like the C int) suppresses the
  `perror`-style diagnostic printed to stderr on syscall failure on
  POSIX. On Windows the argument is accepted but the C original is
  silent either way, so it is effectively ignored.

  Failures do not throw; on failure the previous mode is left in
  place."
  [quiet]
  #?(:bb (terminal/enter-raw-mode quiet)
     :clj (let [quiet? (not (zero? (int quiet)))]
            (if (= os :windows)
              (enter-raw-mode-windows quiet?)
              (enter-raw-mode-posix   quiet?)))))

(defn leave-raw-mode
  "Restore stdin echo/canonical settings after `enter-raw-mode`.

  On POSIX this restores the exact termios snapshot captured by the
  most recent successful `enter-raw-mode` call, and is a no-op if
  we are not currently in raw mode. On Windows it unconditionally
  re-enables ENABLE_ECHO_INPUT, matching the C original."
  [quiet]
  #?(:bb (terminal/leave-raw-mode quiet)
     :clj (let [quiet? (not (zero? (int quiet)))]
            (if (= os :windows)
              (leave-raw-mode-windows quiet?)
              (leave-raw-mode-posix   quiet?)))))

;; -----------------------------------------------------------------
;; Terminal-state query
;; -----------------------------------------------------------------
;;
;; There is no single POSIX flag that means "raw mode". We define
;; raw-ness as: ICANON and ECHO are both cleared on stdin's
;; termios. This is the invariant shared by every common raw-ish
;; mode (ssh's enter_raw_mode, cfmakeraw, ncurses cbreak+noecho,
;; termbox, JLine's raw mode, ...), so it's a reliable test
;; regardless of who put the terminal into that state.
;;
;; On Windows the analogous probe is: ENABLE_ECHO_INPUT is cleared
;; on stdin's console mode.

(defn- in-raw-mode-posix? []
  (let [buf (alloc-termios)]
    (when-not (neg? (tcgetattr* STDIN_FILENO buf))
      (let [{:keys [lflag-off lflag-width ICANON ECHO]} layout
            lflag (read-flag buf lflag-off lflag-width)]
        (and (zero? (bit-and lflag ICANON))
             (zero? (bit-and lflag ECHO)))))))

(defn- in-raw-mode-windows? []
  (let [h (get-std-handle STD_INPUT_HANDLE)]
    (when-let [mode (get-console-mode h)]
      (zero? (bit-and mode ENABLE_ECHO_INPUT)))))

(defn in-raw-mode?
  "Probe the current terminal state on stdin and return true if it
  looks like raw mode.

  On POSIX: true when both ICANON and ECHO are cleared on stdin's
  termios. This detects raw mode regardless of which library or
  process put the terminal into it, not just raw mode set by
  `enter-raw-mode` in this namespace.

  On Windows: true when ENABLE_ECHO_INPUT is cleared on stdin's
  console mode, matching the single bit that `enter-raw-mode`
  toggles there.

  Returns false (not nil) on any error — e.g. stdin is not a
  terminal, or the underlying syscall fails."
  []
  #?(:bb (terminal/in-raw-mode?)
     :clj (boolean
           (if (= os :windows)
             (in-raw-mode-windows?)
             (in-raw-mode-posix?)))))

;; -----------------------------------------------------------------
;; Save / restore arbitrary terminal state
;; -----------------------------------------------------------------
;;
;; These are intended as a general-purpose "snapshot the terminal
;; now, put it back later" mechanism, independent of the raw-mode
;; machinery. The returned value is opaque: callers should treat it
;; as a token to pass back to `restore-terminal-state` and not
;; otherwise inspect it.
;;
;; On POSIX the snapshot is the entire `struct termios` of stdin,
;; as a byte[]. On Windows it is the console-mode DWORD of stdin,
;; as a Long.

(defn save-terminal-state
  "Capture the current terminal state of stdin and return an opaque
  value that can later be passed to `restore-terminal-state` to put
  the terminal back the way it was.

  On POSIX the returned value is a byte[] containing the full
  `struct termios`. On Windows it is a Long containing the stdin
  console-mode DWORD. Returns nil if the underlying syscall fails
  (e.g. stdin is not a terminal)."
  []
  #?(:bb (terminal/save-terminal-state)
     :clj (if (= os :windows)
            (let [h (get-std-handle STD_INPUT_HANDLE)]
              (when-let [mode (get-console-mode h)]
                (long mode)))
            (let [buf (alloc-termios)]
              (when-not (neg? (tcgetattr* STDIN_FILENO buf))
                (let [bs (byte-array (:size layout))]
                  (.read buf 0 bs 0 (:size layout))
                  bs))))))

(defn restore-terminal-state
  "Restore stdin to the state previously captured by
  `save-terminal-state`. Pass exactly what was returned from
  `save-terminal-state`.

  Returns true on success, false on any failure (state is nil or
  the syscall fails)."
  [state]
  #?(:bb (terminal/restore-terminal-state state)
     :clj (cond
            (nil? state) false

            (= os :windows)
            (let [h (get-std-handle STD_INPUT_HANDLE)]
              (boolean (set-console-mode h (long state))))

            :else
            (let [^bytes bs state
                  buf       (alloc-termios)]
              (.write buf 0 bs 0 (:size layout))
              (not (neg? (tcsetattr* STDIN_FILENO (:TCSADRAIN layout) buf)))))))

;; -----------------------------------------------------------------
;; Terminal size querying via DSR (Device Status Report)
;; -----------------------------------------------------------------

(def ^:private ^:const esc 27)

(defn- write-stdout
  "Write a string to stdout and flush immediately."
  [^String s]
  (let [out System/out]
    (.write out (.getBytes s "US-ASCII"))
    (.flush out)))

(defn- read-byte!
  "Blocking read of one byte from stdin. Returns an int, or -1 on EOF."
  ^long []
  (.read ^InputStream System/in))

(defn- dsr-probe
  "Perform the DSR `ESC[6n` round-trip. Assumes the terminal is
  already in raw mode. Returns [rows cols] or nil."
  []
  ;; Move cursor to a very high row/column; the terminal clamps this to
  ;; its actual bottom-right. Save/restore cursor around the probe so
  ;; we don't disturb whatever the caller was drawing.
  (write-stdout (str (char esc) "[s"
                     (char esc) "[9999;9999H"
                     (char esc) "[6n"
                     (char esc) "[u"))
  ;; Skip bytes until we see ESC, then expect '['. Collect digits and
  ;; ';' until 'R'. This tolerates stray input bytes arriving before
  ;; the report.
  (loop [state :await-esc
         buf   (StringBuilder.)]
    (let [b (read-byte!)]
      (cond
        (neg? b) nil

        (= state :await-esc)
        (if (= b esc)
          (recur :await-bracket buf)
          (recur :await-esc buf))

        (= state :await-bracket)
        (if (= b (int \[))
          (recur :collect buf)
          ;; Not a CSI — resync.
          (recur :await-esc buf))

        (= state :collect)
        (cond
          (= b (int \R))
          (let [parts (str/split (.toString buf) #";")]
            (when (= 2 (count parts))
              (try
                [(Integer/parseInt (nth parts 0))
                 (Integer/parseInt (nth parts 1))]
                (catch NumberFormatException _ nil))))

          (or (<= (int \0) b (int \9))
              (= b (int \;)))
          (do (.append buf (char b))
              (recur :collect buf))

          :else
          ;; Unexpected byte; abandon and resync.
          (recur :await-esc (StringBuilder.)))))))

(defn- query-terminal-size
  "Query the terminal for its size by snapshotting the current
  terminal state, putting it into raw mode, performing an ANSI DSR
  (`ESC[6n`) round-trip, and then restoring the saved state.

  Returns `[rows cols]` or nil. Nil can mean: stdin is not a
  terminal, the terminal failed to respond, or the reply could not
  be parsed. The terminal state is always restored to what it was
  before the call, whether the probe succeeds or fails.

  This is a private helper used by `get-width` and `get-height`."
  []
  (let [saved (save-terminal-state)]
    (when saved
      ;; Preserve the module-level raw-mode bookkeeping across the
      ;; probe. `enter-raw-mode` mutates these atoms; we roll them
      ;; back after restoring the termios snapshot so callers who
      ;; are tracking raw mode via `enter-raw-mode` / `leave-raw-mode`
      ;; see no spurious change.
      (let [prev-flag @raw-mode-flag
            prev-tio  @saved-tio]
        (try
          (enter-raw-mode 1)
          ;; Guard: if enter-raw-mode silently failed we must not
          ;; run the probe, because reading from stdin in canonical
          ;; mode would block until the user pressed Enter.
          (when (in-raw-mode?)
            (dsr-probe))
          (finally
            (restore-terminal-state saved)
            (reset! raw-mode-flag prev-flag)
            (reset! saved-tio     prev-tio)))))))

(defn get-width
  "Return the width (columns) of the terminal.

  Uses `query-terminal-size`, which briefly puts the terminal into
  raw mode and queries it via ANSI DSR. The terminal's prior state
  is restored before this function returns. Returns nil if the
  terminal cannot be queried."
  []
  #?(:bb (terminal/get-width)
     :clj (when-let [[_ cols] (query-terminal-size)]
            cols)))

(defn get-height
  "Return the height (rows) of the terminal.

  See `get-width` for behaviour and preconditions."
  []
  #?(:bb (terminal/get-height)
     :clj (when-let [[rows _] (query-terminal-size)]
            rows)))

(def ctrl-c 3)
(def carriage-return 10)

(defn raw-mode-readline
  "Read input from stdin with terminal in raw mode.

  The terminal's prior state is restored on every exit path,
  including when the read loop throws."
  []
  #?(:bb (terminal/raw-mode-readline)
     :clj (do
            (enter-raw-mode 1)
            (try
              (loop [text ""]
                (let [c (.read *in*)]
                  (condp = c
                    ctrl-c nil
                    carriage-return text
                    (recur (str text (char c))))))
              (finally
                (leave-raw-mode 1))))))

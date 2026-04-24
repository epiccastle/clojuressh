(ns cljssh.impl.utils
  (:require [babashka.fs :as fs]
            [clojure.string :as string])
  (:import [java.nio.file.attribute PosixFilePermission FileTime]))

(def decoder (java.util.Base64/getDecoder))

(defn decode-base64 [base64]
  (.decode decoder base64))

(def encoder (java.util.Base64/getEncoder))

(defn encode-base64 [array]
  (.encodeToString encoder array))

(defn opt-decode-base64
  "If passed a string, decode it as base64 and return a byte array.
  If passed a byte array, just return it as is."
  [key]
  (if (string? key)
    (decode-base64 key)
    key))

(defn opt-get-bytes
  "If passed a string, return the underlying byte array using the
  encoding (defaults to utf8). If passed a byte arrayu, just return it
  as it is."
  [data & [encoding]]
  (if (string? data)
    (.getBytes data (or encoding "utf-8"))
    data))

(defn to-camel-case [^String a]
  (apply str (map string/capitalize (.split (name a) "-"))))

#_ (to-camel-case "one-two-three")
#_ (to-camel-case "one")

(defn string-to-byte-array [^String s]
  (byte-array (map int s)))

(defn boolean-to-yes-no [val]
  (if (boolean? val)
    (if val "yes" "no")
    val))

(defn escape-double-quotes [path]
  (string/replace path "\"" "\\\""))

(defn double-quote [string]
  (str "\"" string "\""))

(defn quote-path [path]
  (double-quote (escape-double-quotes path)))

;; (defn last-access-time [^java.io.File file]
;;   (utils/last-access-time (.getCanonicalPath file)))

;; (defn last-modified-time [^java.io.File file]
;;   (utils/last-modified-time (.getCanonicalPath file)))

;; (defn file-mode [^java.io.File file]
;;   (utils/file-mode (.getCanonicalPath file)))

(def permission->mode
  {PosixFilePermission/OWNER_READ     0400
   PosixFilePermission/OWNER_WRITE    0200
   PosixFilePermission/OWNER_EXECUTE  0100
   PosixFilePermission/GROUP_READ     0040
   PosixFilePermission/GROUP_WRITE    0020
   PosixFilePermission/GROUP_EXECUTE  0010
   PosixFilePermission/OTHERS_READ    0004
   PosixFilePermission/OTHERS_WRITE   0002
   PosixFilePermission/OTHERS_EXECUTE 0001})

(defn permission-set->mode [permission-set]
  (->> permission-set
       (reduce (fn [acc perm] (bit-or acc (permission->mode perm)))
               0)))

(def mode->permission
  (->> permission->mode
       (map reverse)
       (map vec)
       (into {})))

(defn mode->permission-set [mode]
  (->> mode->permission
       (map (fn [[perm-mode perm-value]]
              (when (pos? (bit-and mode perm-mode))
                perm-value)))
       (filter identity)
       (into #{})))

(defn create-file [file mode]
  (fs/create-file
   file
   {:posix-file-permissions
    (mode->permission-set mode)}))

(defn create-dirs [file mode]
  (fs/create-dirs
   file
   {:posix-file-permissions
    (mode->permission-set mode)}))

(defn update-file-times [file [mtime atime]]
  (fs/set-attribute file "basic:lastAccessTime" (FileTime/from atime))
  (fs/set-last-modified-time file mtime))

(defn last-modified-time [file]
  (-> (fs/last-modified-time file)
      .toInstant
      .getEpochSecond))

(defn last-access-time [file]
  (-> (fs/get-attribute file "basic:lastAccessTime")
      .toInstant
      .getEpochSecond))

(defn file-mode [file]
  (permission-set->mode
   (fs/posix-file-permissions file)))

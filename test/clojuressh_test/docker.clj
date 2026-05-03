(ns clojuressh-test.docker
  (:require [babashka.process :as process]
            [clojure.string :as string])
  (:import [java.lang.ref WeakReference]))

(defn run [command error-message]
  (let [{:keys [exit err out]}
        (process/sh command)]
    (assert (zero? exit) (str error-message ": out:" out " err:" err))
    out))

(defn run! [command]
  (process/sh command))

(defn build [{:keys [root-password]}]
  (run
    (format
     "docker build -t clojuressh/test-base --build-arg root_password=%s test"
     root-password)
    "docker build failed"))

(defn cleanup []
  (run! "docker container stop clojuressh-test")
  (run! "docker container rm clojuressh-test")
  nil)

(defn start [{:keys [ssh-port]}]
  (-> "docker run --name clojuressh-test -d -p %d:22 clojuressh/test-base"
      (format ssh-port)
      (run "docker run failed")
      string/trim))

(defn stop []
  (run! "docker container stop clojuressh-test"))

(defn exec [command]
  (run
    (str "docker exec clojuressh-test " command)
    "docker exec failed"))

(defn exec! [command]
  (run!
    (str "docker exec clojuressh-test " command)))

(defn cp-to [local-src remote-dest]
  (run
    (format "docker cp \"%s\" \"clojuressh-test:%s\"" local-src remote-dest)
    "docker cp failed"))

(defn cp-from [remote-src local-dest]
  (run
    (format "docker cp \"clojuressh-test:%s\" \"%s\"" remote-src local-dest)
    "docker cp failed"))

(defn put-file [contents remote-dest]
  (process/sh
   ["docker" "exec" "clojuressh-test" "ash" "-c"
    (format "echo '%s' > '%s'"
            contents
            remote-dest)]))

(defn put-dir
  "transfer a complete local directory to the docker container"
  [src-dir src-path dest-path]
  (process/sh "rm /tmp/clojuressh-tarball.tgz")
  (process/sh (format "tar -cvz -C '%s' -f /tmp/clojuressh-tarball.tgz '%s'" src-dir src-path))
  (exec "rm -f /tmp/clojuressh-tarball.tgz")
  (cp-to "/tmp/clojuressh-tarball.tgz" "/tmp/clojuressh-tarball.tgz")
  (exec
   (format "tar -xv -f /tmp/clojuressh-tarball.tgz -C '%s'"
           dest-path)))

(defn md5 [path]
  (-> (exec (format "md5sum '%s'" path))
      (string/split #" ")
      first))

(defn get-container-ip
  []
  (-> (process/sh
        "docker inspect -f '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' clojuressh-test")
      :out
      (string/trim)))

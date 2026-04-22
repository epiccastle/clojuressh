(ns build
  (:require [clojure.tools.build.api :as b]
            [deps-deploy.deps-deploy :as dd]))

(def lib 'io.epiccastle/cljssh)
(def version "0.1.0" #_(format "0.1.%s" (b/git-count-revs nil)))
(def class-dir "target/classes")
(def basis (delay (b/create-basis {:project "deps.edn"})))
(def jar-file (format "target/%s-%s.jar" (name lib) version))

(defn clean [_]
  (b/delete {:path "target"}))

(defn jar [_]
  (b/write-pom {:class-dir class-dir
                :lib lib
                :version version
                :basis @basis
                :src-dirs ["src"]
                :scm {:url "https://github.com/epiccastle/cljssh"
                      :connection "scm:git:git://github.com/epiccastle/cljssh.git"
                      :developerConnection "scm:git:ssh://git@github.com/epiccastle/cljssh.git"
                      :tag (str "v" version)}
                :pom-data
                [[:description "A Clojure library for using SSH in Clojure that is API compatible with bbssh"]
                 [:licenses
                  [:license
                   [:name "Eclipse Public License 1.0"]
                   [:url "https://www.eclipse.org/legal/epl-v10.html"]]]]})
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/jar {:class-dir class-dir
          :jar-file jar-file}))

(defn install [_]
  (clean nil)
  (jar nil)
  (b/install {:basis @basis
              :lib lib
              :version version
              :jar-file jar-file
              :class-dir class-dir}))

(defn deploy [_]
  (clean nil)
  (jar nil)
  (dd/deploy {:installer :remote
              :artifact jar-file
              :pom-file (b/pom-path {:lib lib :class-dir class-dir})}))

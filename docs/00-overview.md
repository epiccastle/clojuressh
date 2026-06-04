# Overview

A Clojure library for SSH support, API compatible with [bbssh](https://github.com/epiccastle/bbssh).

`clojuressh` is a port of the `bbssh` babashka pod into a native Clojure
library. A local installation of `ssh` is **not required**.

This library when loaded into babashka acts as a shim to the bbssh pod. Thus you can refer to this
in your `bb-deps.edn` and use it like you would in clojure, allowing you to write one codebase that
will run on both clojure and babashka. You can also refer to it in your clojure `deps.edn` or
`project.clj` and use it the same way.


## Coordinates

### tools.deps

```clojure
io.epiccastle/clojuressh {:mvn/version "0.7.0"}
```

### leiningen

```clojure
[io.epiccastle/clojuressh "0.7.0"]
```
## Quickstart on clojure

Here is a simple example that connects over ssh, runs a command, and
disconnects, returning the standard output. Put this in `src/testssh/core.clj`:

```clojure
(ns testssh.core
  (:require [clojuressh.core :as clojuressh]
            [clojuressh.session :as session]))

(defn -main []
  (let [session (clojuressh/ssh "localhost")]
    (-> (clojuressh/exec session "echo 'I am running remotely'" {:out :string})
        deref
        :out
        prn)
    (session/disconnect session)
    (shutdown-agents)))
```

Make a `deps.edn` like:

```clojure
{:paths ["src"]
 :deps {org.clojure/clojure {:mvn/version "1.12.5"}
        io.epiccastle/clojuressh {:mvn/version "0.7.0"}}}
```

Run your mainline with:

```
clj -M -m testssh.core
```

## Running on babashka

Using the same `src/testssh/core.clj` and `deps.edn` shown above, run your mainline with:

```
bb --config deps.edn -m testssh.core
```

## Copyright

Copyright (c) Crispin Wellington. All rights reserved.

The use and distribution terms for this software are covered by the
Eclipse Public License 2.0 which can be found in `LICENSE`.

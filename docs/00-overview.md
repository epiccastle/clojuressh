# Overview

clojuressh is a Clojure library that provides ssh support to Clojure code. It is API compatible with [bbssh](https://github.com/epiccastle/bbssh).

Project repository is [here](https://github.com/epiccastle/clojuressh)

A local installation of ssh is **not required**.

## Installation

Add clojuressh as a dependency in your `deps.edn`:

```clojure
io.epiccastle/clojuressh {:mvn/version "0.1.0"}
```

## Quickstart

Try writing the following into `test_clojuressh.clj`

```clojure
(ns test-clojuressh
  (:require [clojuressh.core :as clojuressh]
            [clojuressh.session :as session))

(let [session (clojuressh/ssh "localhost")]
  (-> (clojuressh/exec session "echo 'I am running over ssh'" {:out :string})
      deref
      :out
      prn)
  (session/disconnect session))
```

Then execute the file with Clojure. You will be prompted for your ssh password. Enter it and press return:

```bash-shell
$ clojure -M test_clojuressh.clj
Enter Password for crispin@localhost:
"I am running over ssh\n"
```

> **Note:** if you are running an ssh-agent and you have a relevant key you may not be asked for your password. clojuressh supports authentication by ssh agent.

## Copyright

Copyright (c) Crispin Wellington. All rights reserved.

The use and distribution terms for this software are covered by the
Eclipse Public License 2.0 which can be found in `LICENSE`.

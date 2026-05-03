# clojuressh

A Clojure library for SSH support, API compatible with [bbssh](https://github.com/epiccastle/bbssh).

`clojuressh` is a port of the `bbssh` babashka pod into a native Clojure
library. A local installation of `ssh` is **not required**.

## Coordinates

```clojure
io.epiccastle/clojuressh {:mvn/version "0.1.0"}
```

## Usage

Here is a simple example that connects over ssh, runs a command, and
disconnects, returning the standard output:

```clojure
(ns test-clojuressh
  (:require [clojuressh.core :as clojuressh]
            [clojuressh.session :as session]))

(let [session (clojuressh/ssh "remotehost" {:username "remote-user"})]
  (-> (clojuressh/exec session "echo 'I am running remotely'" {:out :string})
      deref
      :out
      prn)
  (session/disconnect session))
```

> **Note:** if you are running an ssh-agent and you have a relevant key
> loaded you may not be asked for a password. `clojuressh` supports
> authentication via ssh agent.

## API documentation

The full documentation [can be found here](https://epiccastle.io/clojuressh).

See also the docs in the [`docs/`](docs) directory for an overview, basics,
port forwarding, and how-to guides.

## Running on JDK 22+

`clojuressh` uses JNA to call a handful of native `libc` / `kernel32` functions
for terminal handling (raw mode, terminal size). On JDK 22 and newer, the
JVM prints a warning the first time any native library is loaded:

```
WARNING: A restricted method in java.lang.System has been called
WARNING: java.lang.System::load has been called by com.sun.jna.Native ...
WARNING: Use --enable-native-access=ALL-UNNAMED to avoid a warning for callers in this module
WARNING: Restricted methods will be blocked in a future release unless native access is enabled
```

This is a JDK-level warning about any code that performs native access,
not an error, and it applies to every JNA-using library on modern JDKs —
not just `clojuressh`. On a future JDK version (currently expected no sooner
than JDK 26) the warning will become a hard error unless you opt in.

### Silencing

To silence the warning today, and to future-proof your project, start the
JVM with `--enable-native-access=ALL-UNNAMED`. Pick whichever of these
matches your launcher:

**`deps.edn`** — add to the alias (or top-level) that runs your app:

```clojure
{:aliases
 {:run {:main-opts ["-m" "my.app"]
        :jvm-opts  ["--enable-native-access=ALL-UNNAMED"]}}}
```

**Leiningen `project.clj`**:

```clojure
:jvm-opts ["--enable-native-access=ALL-UNNAMED"]
```

**Plain `java`** (e.g. running an uberjar):

```
java --enable-native-access=ALL-UNNAMED -jar my-app.jar
```

**Environment variable** (applies to every JVM spawned in the shell):

```
export JDK_JAVA_OPTIONS=--enable-native-access=ALL-UNNAMED
```

No flag is required on JDK 21 and earlier. If you hit an
`IllegalCallerException` about native access on a JDK where the warning
has become a hard error, `clojuressh` will rethrow it wrapped in an
`ex-info` pointing you at this section.

### Printing the warning early

Often the warning prints right as clojuressh asks for a password. This is when
the library loads the underlying terminal libraries to switch the terminal
into raw mode. This can be very confusing to the user. It is quite simple to
trigger the warning to be printed at the start of the program so it doesn't
interrupt later. Simply run one of the relevantt terminal functions early in your
code. For example, run `clojuressh.terminal/in-raw-mode?` at the beginning of your
program:

```clojure
(ns test-clojuressh
  (:require [clojuressh.core :as clojuressh]
            [clojuressh.terminal :as terminal]))
(defn -main []
    (terminal/in-raw-mode?) ;; prints native access warning

    (let [session (clojuressh/ssh "hostname")]
      ...)
```

## Development

Run tests:

```
make test
```

Build a jar:

```
make jar
```

Install to local Maven repo:

```
make install
```

Deploy to Clojars:

```
make deploy
```

Remove build artifacts:

```
make clean
```

Start a REPL:

```
make repl
```

Run `make help` to list all available targets.

## License

Copyright (c) Crispin Wellington. All rights reserved.

Distributed under the Eclipse Public License version 2.0 which can be
found in `LICENSE`.

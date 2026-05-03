# clojuressh

A Clojure library for using SSH in Clojure that is API compatible with [bbssh](https://github.com/epiccastle/bbssh).

## Coordinates

```clojure
io.epiccastle/clojuressh {:mvn/version "0.1.0"}
```

## Usage

```clojure
(require '[io.epiccastle.clojuressh :as ssh])
```

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

Copyright © Epic Castle

Distributed under the Eclipse Public License version 1.0.

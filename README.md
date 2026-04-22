# cljssh

A Clojure library for using SSH in Clojure that is API compatible with [bbssh](https://github.com/epiccastle/bbssh).

## Coordinates

```clojure
io.epiccastle/cljssh {:mvn/version "0.1.0"}
```

## Usage

```clojure
(require '[io.epiccastle.cljssh :as ssh])
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

Copyright © Epic Castle

Distributed under the Eclipse Public License version 1.0.

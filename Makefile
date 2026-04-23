.PHONY: help test jar install deploy clean repl run

help:
	@echo "Available targets:"
	@echo "  make run      - Run the cljssh entry point"
	@echo "  make test     - Run the test suite"
	@echo "  make jar      - Build a jar file"
	@echo "  make install  - Install jar to local Maven repo (~/.m2)"
	@echo "  make deploy   - Deploy jar to Clojars (requires CLOJARS_USERNAME/CLOJARS_PASSWORD)"
	@echo "  make clean    - Remove build artifacts"
	@echo "  make version  - print the version string derived from the git tags"
	@echo "  make repl     - Start a Clojure REPL"

run:
	clojure -M -m cljssh

test:
	clojure -M:test

jar:
	clojure -T:build jar

install:
	clojure -T:build install

deploy:
	clojure -T:build deploy

clean:
	clojure -T:build clean

version:
	clojure -T:build version

repl:
	rlwrap clojure

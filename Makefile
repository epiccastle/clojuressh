.PHONY: help test jar install deploy clean repl run codox codox-upload

help:
	@echo "Available targets:"
	@echo "  make run          - Run the clojuressh entry point"
	@echo "  make test         - Run the test suite"
	@echo "  make jar          - Build a jar file"
	@echo "  make install      - Install jar to local Maven repo (~/.m2)"
	@echo "  make deploy       - Deploy jar to Clojars (requires CLOJARS_USERNAME/CLOJARS_PASSWORD)"
	@echo "  make clean        - Remove build artifacts"
	@echo "  make version      - print the version string derived from the git tags"
	@echo "  make repl         - Start a Clojure REPL"
	@echo "  make codox        - Build codox API documentation into target/docs"
	@echo "  make codox-upload - Upload generated docs to epiccastle.io"

run:
	clojure -M:run

test:
	-mkdir test/files/dir1/dir3
	umask 0000; clojure -M:test

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

codox:
	clojure -X:codox

codox-upload:
	rsync -av --delete target/docs/ www-data@epiccastle.io:~/epiccastle.io/public/clojuressh/

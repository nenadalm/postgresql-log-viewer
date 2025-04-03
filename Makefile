SOURCES := $(shell find deps.edn src/ extra-src/ test/ -type f)

target/plv-portal.jar: ${SOURCES}
	clojure -T:build uberjar-portal

target/plv-reveal.jar: ${SOURCES}
	clojure -T:build uberjar-reveal

.PHONY: test
test:
	clojure -M:cljfmt check
	clojure -M:clj-kondo
	clojure -M:test

.PHONY: clean
clean:
	clojure -T:build clean

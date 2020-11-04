.PHONY: test
test:
	clojure -M:cljfmt check
	clojure -M:clj-kondo
	clojure -M:test

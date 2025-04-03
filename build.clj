(ns build
  (:require
   [clojure.tools.build.api :as b]))

(defn clean [_]
  (b/delete {:path "target"}))

(defn uberjar-portal [_]
  (let [class-dir "target/portal/classes"
        basis (b/create-basis
               {:aliases [:portal]})]
    (b/copy-dir
     {:src-dirs ["src" "extra-src/portal" "resources"]
      :target-dir class-dir})
    (b/compile-clj
     {:basis basis
      :class-dir class-dir
      :compile-opts {:direct-linking true}})
    (b/uber
     {:basis basis
      :class-dir class-dir
      :main 'app.portal
      :uber-file "target/plv-portal.jar"})))

(defn uberjar-reveal [_]
  (let [class-dir "target/reveal/classes"
        basis (b/create-basis
               {:aliases [:reveal]})]
    (b/copy-dir
     {:src-dirs ["src" "extra-src/reveal" "resources"]
      :target-dir class-dir})
    (b/compile-clj
     {:basis basis
      :class-dir class-dir
      :compile-opts {:direct-linking true}})
    (b/uber
     {:basis basis
      :class-dir class-dir
      :main 'app.reveal
      :uber-file "target/plv-reveal.jar"})))

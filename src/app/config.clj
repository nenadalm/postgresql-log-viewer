(ns app.config
  (:require
   [clojure.java.io]))

(defn- app-version []
  (if-some [f (some-> "META-INF/app/config.properties"
                      clojure.java.io/resource
                      clojure.java.io/reader)]
    (let [properties (java.util.Properties.)]
      (.load properties f)
      (.get properties "version"))
    "unknown"))

(def config {:app.config/version (app-version)})

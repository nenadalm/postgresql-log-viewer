(ns app.portal
  (:require
   [app.postgresql-log.core :as log]
   [jsonista.core :as j]
   [app.config :refer [config]]
   [portal.api :as portal]
   [portal.viewer :as v])
  (:gen-class))

(defn- init-portal []
  (.addShutdownHook
   (Runtime/getRuntime)
   (proxy [Thread] []
     (run []
       (portal/close))))
  (portal/open {:theme :portal.colors/nord})
  (add-tap #'portal/submit))

(defn- format-log [log]
  (with-meta
    log
    {'clojure.core.protocols/nav (fn [_ k v]
                                   (case k
                                     :message (v/text (log/format-message log))
                                     v))}))

(defn -main [& _]
  (println (str "App info:\n"
                "  Version: " (:app.config/version config) "\n"
                "  UI: portal\n"))
  (init-portal)
  (loop []
    (-> (read-line)
        (j/read-value j/keyword-keys-object-mapper)
        format-log
        tap>)
    (recur)))

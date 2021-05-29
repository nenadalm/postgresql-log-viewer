(ns app.portal
  (:require
   [app.postgresql-log.core :as log]
   [app.postgresql-log.parser :as log-parser]
   [app.postgresql-log.reader :as log-reader]
   [portal.api :as portal])
  (:gen-class))

(defn- init-portal []
  (.addShutdownHook
   (Runtime/getRuntime)
   (proxy [Thread] []
     (run []
       (portal/close))))
  (portal/open)
  (portal/tap))

(defn- format-log [log]
  (with-meta
    log
    {'clojure.core.protocols/nav (fn [_ k v]
                                   (case k
                                     :log/message (log/format-message log)
                                     v))}))

(defn -main [& _]
  (init-portal)
  (log-reader/safe-read
   (doseq [line (log-reader/read-csv *in*)]
     (-> line
         log-parser/line->log
         format-log
         tap>))))

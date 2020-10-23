(ns app.portal
  (:require
   [app.postgresql-log.core :as log]
   [app.postgresql-log.parser :as log-parser]
   [portal.api :as portal]
   [clojure.data.csv :as csv]))

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
  (doseq [line (log-parser/drop-invalid-lines (csv/read-csv *in*))]
    (-> line
        log-parser/line->log
        format-log
        tap>)))

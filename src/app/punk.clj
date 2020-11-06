(ns app.punk
  (:require
   [app.postgresql-log.core :as log]
   [app.postgresql-log.parser :as log-parser]
   [app.postgresql-log.reader :as log-reader]
   [punk.adapter.jvm :as paj]
   [punk.core :as pc]))

(defn- wait-on-start []
  (loop [fx nil]
    (when-not fx
      (Thread/sleep 100)
      (recur (get-in @(:registrar pc/frame) [:fx :emit])))))

(defn- init-punk  []
  (println "[INFO]: Starting server on:" (str "http://localhost:" paj/port))
  (paj/start)
  (wait-on-start)
  (println "[INFO]: Server started."))

;; todo: this doesn't work
(defn- format-log [log]
  (with-meta
    log
    {'clojure.core.protocols/nav (fn [_ k v]
                                   (case k
                                     :log/message (log/format-message log)
                                     v))}))

(defn -main [& _]
  (init-punk)
  (log-reader/safe-read
   (doseq [line (log-reader/read-csv *in*)]
     (-> line
         log-parser/line->log
         format-log
         tap>))))

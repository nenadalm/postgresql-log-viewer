(ns app.reveal
  (:require
   [app.postgresql-log.core :as log]
   [jsonista.core :as j]
   [app.config :refer [config]]
   [vlaaad.reveal :as reveal]
   [vlaaad.reveal.ext :as ve])
  (:gen-class))

(defrecord Text [s])

(ve/defstream Text [v]
  (ve/raw-string (:s v) {:fill :string}))

(defn- init-reveal []
  (add-tap (reveal/ui)))

(defn- format-value [v]
  (if (string? v)
    (Text. v)
    v))

(defn- format-log [log]
  (with-meta
    log
    {'clojure.core.protocols/nav (fn [_ k v]
                                   (format-value
                                    (case k
                                      :log/message (log/format-message log)
                                      v)))}))

(defn -main [& _]
  (println (str "App info:\n"
                "  Version: " (:app.config/version config) "\n"
                "  UI: reveal\n"))
  (init-reveal)
  (loop []
    (-> (read-line)
        (j/read-value j/keyword-keys-object-mapper)
        format-log
        tap>)
    (recur)))

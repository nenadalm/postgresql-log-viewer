(ns app.postgresql-log.core
  (:require
   [app.postgresql-log.parser :as parser]
   [clojure.string])
  (:import
   [com.github.vertical_blank.sqlformatter SqlFormatter]))

(defn- format-query [query]
  (SqlFormatter/format query))

(defn- inline-params [query params]
  (reduce
   (fn [query [param val]]
     (clojure.string/replace-first query param val))
   query
   params))

(defn format-message [log]
  (let [message (parser/parse-message (:log/message log))]
    (case (:message/type message)
      ("statement" "execute") (format-query
                               (inline-params
                                (:message/content message)
                                (parser/parse-detail-params (:log/detail log))))
      (:message/content message))))

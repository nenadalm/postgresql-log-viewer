(ns app.postgresql-log.core
  (:require
   [app.postgresql-log.parser :as parser]
   [clojure.string])
  (:import
   [com.github.vertical_blank.sqlformatter SqlFormatter]
   [com.github.vertical_blank.sqlformatter.languages Dialect]))

(defn- format-query [query]
  (.format (SqlFormatter/of Dialect/PostgreSql) query))

(defn- inline-params [query params]
  (reduce
   (fn [query [param val]]
     (clojure.string/replace-first query param val))
   query
   params))

(defn format-message [log]
  (let [message (parser/parse-message (:message log))]
    (case (:message/type message)
      ("statement" "execute") (format-query
                               (inline-params
                                (:message/content message)
                                (parser/parse-detail-params (:detail log))))
      (:message/content message))))

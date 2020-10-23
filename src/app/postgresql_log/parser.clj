(ns app.postgresql-log.parser
  (:require
   [clojure.string]))

(def ^:private csv-header
  [:log/log-time
   :log/user-name
   :log/database-name
   :log/process-id
   :log/connection-from
   :log/session-id
   :log/session-line-num
   :log/command-tag
   :log/session-start-time
   :log/virtual-transaction-id
   :log/transaction-id
   :log/error-severity
   :log/sql-state-code
   :log/message
   :log/detail
   :log/hint
   :log/internal-query
   :log/internal-query-pos
   :log/context
   :log/query
   :log/query-pos
   :log/location
   :log/application-name])

(defn valid-line? [line]
  (= (count line) (count csv-header)))

(defn drop-invalid-lines [lines]
  (drop-while #(not (valid-line? %)) lines))

(defn line->log [line]
  (reduce-kv
   (fn [m k v]
     (if (= "" v)
       m
       (assoc m k v)))
   {}
   (zipmap
    csv-header
    line)))

(defn parse-message [message]
  (when message
    (if-let [res (re-matches #"(?s)(?<type>[^ :]*)[^:]*: (?<s>.*)" message)]
      (let [[_ type content] res]
        {:message/type type
         :message/content content})
      {:message/type :unknown
       :message/content message})))

(defn parse-detail [detail]
  (when detail
    (if-let [res (re-matches #"(?<type>.*)?: (?<content>.*)" detail)]
      (let [[_ type content] res]
        {:detail/type type
         :detail/content content})
      {:detail/type "unknown"
       :detail/content detail})))

(defn parse-params [detail]
  (when (and detail (= "parameters" (:detail/type detail)))
    (as-> (:detail/content detail)
          $
      (clojure.string/split $ #", ")
      (mapv #(clojure.string/split % #" = ") $)
      (rseq $))))

(defn parse-detail-params [detail]
  (-> detail
      parse-detail
      parse-params))

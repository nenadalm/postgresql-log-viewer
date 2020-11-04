(ns app.postgresql-log.reader
  (:require
   [clojure.data.csv :as csv]
   [app.postgresql-log.parser :as log-parser]))

(defn- read-csv* [reader]
  (let [res (log-parser/drop-invalid-lines (csv/read-csv reader))]
    (first res) ;; force realization of first line to throw if invalid
    res))

(defn read-csv [reader]
  (loop [retries 100] ;; as log can be read from line that is in middle of csv cell, we need to retry
    (if-let [result (try
                      [(read-csv* reader)]
                      (catch Exception e
                        (binding [*out* *err*]
                          (println "[WARNING]: CSV is not valid from read point. Remaining retries:" retries))
                        (when (zero? retries)
                          (throw e))))]
      (result 0)
      (recur (dec retries)))))

(defmacro safe-read
  "Ignores unexpected end of csv. This can happen when only part of postgresql file is read."
  [& body]
  `(try
     ~@body
     (catch java.io.EOFException ~'_
       (binding [*out* *err*]
         (println "[WARNING]: Invalid end of CSV. Probably caused by reading just part of the log where final entry is not whole.")))))

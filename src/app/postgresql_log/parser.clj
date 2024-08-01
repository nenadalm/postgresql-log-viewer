(ns app.postgresql-log.parser
  (:require
   [clojure.string]))

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
    (if-let [res (re-matches #"(?<type>[^ :]*)?: (?<content>.*)" detail)]
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

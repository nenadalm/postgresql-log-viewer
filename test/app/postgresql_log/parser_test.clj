(ns app.postgresql-log.parser-test
  (:require
   [app.postgresql-log.parser :as parser]
   [clojure.test :refer [deftest testing is]]))

(deftest parse-message-test []
  (is (nil? (parser/parse-message nil)))
  (is (= {:message/type "statement"
          :message/content "SELECT 1"}
         (parser/parse-message "statement: SELECT 1")))
  (is (= {:message/type "execute"
          :message/content "SELECT 1"}
         (parser/parse-message "execute <unnamed>: SELECT 1")))
  (is (= {:message/type "execute"
          :message/content "SELECT
1"}
         (parser/parse-message "execute <unnamed>: SELECT
1"))))

(deftest parse-detail-test []
  (is (nil? (parser/parse-detail nil)))
  (is (= {:detail/type "parameters"
          :detail/content "$1 = 'admin'"}
         (parser/parse-detail "parameters: $1 = 'admin'")))
  (is (= {:detail/type "parameters"
          :detail/content "$1 = 'user:admin', '{\"data\": \"dval\"}'"}
         (parser/parse-detail "parameters: $1 = 'user:admin', '{\"data\": \"dval\"}'")))
  (is (= {:detail/type "unknown"
          :detail/content "some detail"}
         (parser/parse-detail "some detail"))))

(deftest parse-params-test []
  (testing "single param"
    (is (= [["$1" "'admin'"]]
           (parser/parse-params
            {:detail/type "parameters"
             :detail/content "$1 = 'admin'"}))))
  (testing "miltiple params"
    (is (= [["$2" "3"]
            ["$1" "'admin'"]]
           (parser/parse-params
            {:detail/type "parameters"
             :detail/content "$1 = 'admin', $2 = 3"})))))

(deftest parse-detail-params-test []
  (is (= [["$2" "3"]
          ["$1" "'admin'"]]
         (parser/parse-detail-params "parameters: $1 = 'admin', $2 = 3"))))

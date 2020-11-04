(ns app.postgresql-log.parser-test
  (:require
   [app.postgresql-log.parser :as parser]
   [clojure.test :refer [deftest testing is]]))

(deftest valid-line?-test []
  (is (true? (parser/valid-line?
              ["2020-10-22 17:54:15.466 CEST"
               "postgres"
               "db"
               "1146674"
               "127.0.0.1:55272"
               "5f91aaa8.117f32"
               "5"
               "SELECT"
               "2020-10-22 17:52:08 CEST"
               "3/24597"
               "0"
               "LOG"
               "00000"
               "execute <unnamed>: SELECT username FROM user_user WHERE username = $1"
               "parameters: $1 = 'admin'"
               ""
               ""
               ""
               ""
               ""
               ""
               ""
               "PostgreSQL JDBC Driver"])))
  (testing "one missing item"
    (is (false? (parser/valid-line?
                 ["2020-10-22 17:54:15.466 CEST"
                  "postgres"
                  "db"
                  "1146674"
                  "127.0.0.1:55272"
                  "5f91aaa8.117f32"
                  "5"
                  "SELECT"
                  "2020-10-22 17:52:08 CEST"
                  "3/24597"
                  "0"
                  "LOG"
                  "00000"
                  "execute <unnamed>: SELECT username FROM user_user WHERE username = $1"
                  "parameters: $1 = 'admin'"
                  ""
                  ""
                  ""
                  ""
                  ""
                  ""
                  ""])))))

(deftest line->log-test []
  (testing "select with param"
    (is (= {:log/application-name "PostgreSQL JDBC Driver"
            :log/command-tag "SELECT"
            :log/connection-from "127.0.0.1:55272"
            :log/database-name "db"
            :log/detail "parameters: $1 = 'admin'"
            :log/error-severity "LOG"
            :log/log-time "2020-10-22 17:54:15.466 CEST"
            :log/message "execute <unnamed>: SELECT username FROM user_user WHERE username = $1"
            :log/process-id "1146674"
            :log/session-id "5f91aaa8.117f32"
            :log/session-line-num "5"
            :log/session-start-time "2020-10-22 17:52:08 CEST"
            :log/sql-state-code "00000"
            :log/transaction-id "0"
            :log/user-name "postgres"
            :log/virtual-transaction-id "3/24597"}
           (parser/line->log
            ["2020-10-22 17:54:15.466 CEST"
             "postgres"
             "db"
             "1146674"
             "127.0.0.1:55272"
             "5f91aaa8.117f32"
             "5"
             "SELECT"
             "2020-10-22 17:52:08 CEST"
             "3/24597"
             "0"
             "LOG"
             "00000"
             "execute <unnamed>: SELECT username FROM user_user WHERE username = $1"
             "parameters: $1 = 'admin'"
             ""
             ""
             ""
             ""
             ""
             ""
             ""
             "PostgreSQL JDBC Driver"]))))
  (testing "select without params"
    (is (= {:log/application-name "PostgreSQL JDBC Driver"
            :log/command-tag "SELECT"
            :log/connection-from "127.0.0.1:55272"
            :log/database-name "db"
            :log/error-severity "LOG"
            :log/log-time "2020-10-22 18:07:49.876 CEST"
            :log/message "execute <unnamed>: SELECT username FROM user_user"
            :log/process-id "1146674"
            :log/session-id "5f91aaa8.117f32"
            :log/session-line-num "6"
            :log/session-start-time "2020-10-22 17:52:08 CEST"
            :log/sql-state-code "00000"
            :log/transaction-id "0"
            :log/user-name "postgres"
            :log/virtual-transaction-id "3/24598"}
           (parser/line->log
            ["2020-10-22 18:07:49.876 CEST"
             "postgres"
             "db"
             "1146674"
             "127.0.0.1:55272"
             "5f91aaa8.117f32"
             "6"
             "SELECT"
             "2020-10-22 17:52:08 CEST"
             "3/24598"
             "0"
             "LOG"
             "00000"
             "execute <unnamed>: SELECT username FROM user_user"
             ""
             ""
             ""
             ""
             ""
             ""
             ""
             ""
             "PostgreSQL JDBC Driver"])))))

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

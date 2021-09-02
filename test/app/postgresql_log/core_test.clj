(ns app.postgresql-log.core-test
  (:require
   [app.postgresql-log.core :as core]
   [clojure.test :refer [deftest is testing]]))

(deftest format-message-test []
  (testing "query with params"
    (is (= "SELECT
  username
FROM
  user_user
WHERE
  username = 'admin',
  id = $3"
           (core/format-message
            {:log/detail "parameters: $1 = 'admin', $2 = 3"
             :log/message "execute <unnamed>: SELECT username FROM user_user WHERE username = $1, id = $3"}))))
  (testing "query with problematic json operator"
    (is (= "SELECT
  '{\"a\": 1}' :: jsonb -> 'a'"
           (core/format-message
            {:log/message "execute <unnamed>: SELECT '{\"a\": 1}'::jsonb->'a'"}))))
  (testing "duration"
    (is (= "3 ms"
           (core/format-message
            {:log/message "duration: 3 ms"})))))

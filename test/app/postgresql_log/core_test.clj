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
  username = 'admin'
  AND id = $3"
           (core/format-message
            {:detail "parameters: $1 = 'admin', $2 = 3"
             :message "execute <unnamed>: SELECT username FROM user_user WHERE username = $1 AND id = $3"}))))
  (testing "query with problematic json operator"
    (is (= "SELECT
  '{\"a\": 1}' :: jsonb -> 'a'"
           (core/format-message
            {:message "execute <unnamed>: SELECT '{\"a\": 1}'::jsonb->'a'"}))))
  (testing "query with problematic named function arguments"
    (is (= "SELECT
  MAKE_INTERVAL(mins => 15)"
           (core/format-message
            {:message "execute <unnamed>: SELECT MAKE_INTERVAL(mins => 15)"}))))
  (testing "duration"
    (is (= "3 ms"
           (core/format-message
            {:message "duration: 3 ms"})))))

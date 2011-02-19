(ns yaclot.test.core
  (:use [clojure.test])
  (:use [clojure.contrib.mock])
  (:use [yaclot.core]))

(deftest test-string-to-date
  (let [expect (parse-date "2011-02-12")]
    (is (= expect (convert "2011-02-12" (to-type java.util.Date))))
    (is (= expect (convert "2/12/11" (using-format "M/dd/yy" (to-type java.util.Date)))))))

(deftest test-date-to-string
  (let [dt (parse-date "2011-02-12")]
    (is (= "2011-02-12" (convert dt (to-type String))))
    (is (= "2/12/11"(convert dt (using-format "M/dd/yy" (to-type String)))))))

(deftest test-string-to-integer
  (is (= 1 (convert "1" (to-type Integer)))))

(deftest test-string-to-double
  (is (= (double 1) (convert "1" (to-type Double))))
  (is (= (double 0.3) (convert "0.3" (to-type Double)))))

(deftest test-string-to-big-decimal
  (is (= (BigDecimal. 1) (convert "1" (to-type BigDecimal))))
  (is (= (BigDecimal. "0.33") (convert "0.33" (to-type BigDecimal)))))

(deftest test-integer-to-string
  (is (= "1000" (convert 1000 (to-type String)))))

(deftest test-double-to-string
  (is (= "0.3" (convert (double 0.3) (to-type String)))))

(deftest test-big-decimal-to-string
  (is (= "0.3" (convert (BigDecimal. "0.3") (to-type String)))))

(deftest test-map-convert-1
  (let [dt (parse-date "2011-02-12")
        fmt {:d1 (to-type java.util.Date) 
             :d2 (using-format "M/dd/yy" (to-type String))}
        in {:d1 "2011-02-12"
            :d2 dt
            :d3 "Something else"}]
    (is (= 
          {:d1 dt :d2 "2/12/11" :d3 "Something else"}
          (map-convert in fmt)))))
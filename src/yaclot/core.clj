(ns yaclot.core)

; Helpers

(defn to-type 
  ([type] (to-type type {}))
  ([type conversion-params] (assoc conversion-params :type type)))

(defn using-format
  ([format] (using-format format {}))
  ([format conversion-params] (assoc conversion-params :format format)))

; Core

(defmulti convert (fn [val conversion-params] [(class val) (:type conversion-params)]))

(defmacro defconvert 
  ([from-type to-type f] 
    `(do
       (defmethod convert [~from-type ~to-type] [val# params#]
         (~f val#))
       (defmethod convert [~from-type ~from-type] [val# params#] (identity val#))
       (defmethod convert [~to-type ~to-type] [val# params#] (identity val#))
       ))
  ([from-type to-type f default-conversion-params]
    `(do
       (defmethod convert [~from-type ~to-type] [val# params#]
         (~f val# (merge ~default-conversion-params params#)))
       (defmethod convert [~from-type ~from-type] [val# params#] (identity val#))
       (defmethod convert [~to-type ~to-type] [val# params#] (identity val#)))))

(defn map-convert [in conversion-params]
  (into {}
    (for [[k v] in :let [params (k conversion-params)]]
      (if params
        [k (convert v params)]
        [k v]))))

; Invidicual Converters

(defn parse-date 
  ([arg] (parse-date arg "yyyy-MM-dd"))
  ([arg format] 
    (if (coll? format)
      (if-let [res (first (filter identity (map #(try (parse-date arg %) (catch java.text.ParseException e)) format)))]
        res
        (throw (java.text.ParseException. (str "Unparseable date: " arg) 0)))
      (.parse (java.text.SimpleDateFormat. format) arg))))

(defn format-date [dt format]
  (.format (java.text.SimpleDateFormat. format) dt))

(defconvert 
  String
  java.util.Date
  (fn [v conversion-params] (parse-date v (:format conversion-params)))
  (using-format "yyyy-MM-dd"))

(defconvert 
  java.util.Date
  String
  (fn [v conversion-params] (format-date v (:format conversion-params)))
  (using-format "yyyy-MM-dd"))

(defconvert java.util.Date Long #(.getTime %))

(defconvert Long java.util.Date #(new java.util.Date %))

(defconvert String Integer #(Integer/parseInt %))

(defconvert String Double #(Double/parseDouble %))

(defconvert String BigDecimal #(new BigDecimal %))

(defconvert String Number #(new BigDecimal %))

(defconvert String Boolean #(Boolean/valueOf %))

(defn format-number [n fmt]
  (if fmt
    (format fmt n)
    (str n)))

(defconvert
  Number
  String
  (fn [v conversion-params] (format-number v (:format conversion-params)))
  nil)

(defconvert nil Object (fn [_]))
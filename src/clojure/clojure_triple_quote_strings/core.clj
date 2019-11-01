(ns clojure-triple-quote-strings.core
  (:import clojure_triple_quote_strings.ClojureLispReaderPatcher))

(defn patch-clojure-reader!
  []
  (. ClojureLispReaderPatcher (replaceStringReader)))


;; demo
(comment
  (patch-clojure-reader!)

  (defn foo
    """Short Description.

    Long description...
    Here we may have nice unescaped "quotes"
    """
    [x]
    x)

  (:doc (meta #'foo))
  ;; => "Short Description.\n\n  Long description...\n  Here we may have nice unescaped \"quotes\"\n  "
)

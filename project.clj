(defproject clojure-triple-quote-strings "0.1.0-SNAPSHOT"
  :description "An attempt to extend Clojure reader with triple-quoted string literals."
  :url "http://example.com/FIXME"
  :license {:name "Unlicense"
            :url "https://unlicense.org/"}
  :dependencies [[org.clojure/clojure "1.10.1"]]
  :plugins [[cider/cider-nrepl "0.23.0-SNAPSHOT"]
            [refactor-nrepl "2.5.0-SNAPSHOT"]]
  :source-paths      ["src/clojure"]
  :java-source-paths ["src/java"]
  :repl-options {:init-ns clojure-triple-quote-strings.core}
  :aot :all)

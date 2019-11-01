# clojure-triple-quote-strings

This is an experimental attempt to extend Clojure's reader with triple-quote string literals.

The goal is to be able to do this:

```clojure
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
```

The example above already works, but the status is still experimental, not yet ready for use.

In case you want to play with it, you can check out this git repo and visit `core.clj`.

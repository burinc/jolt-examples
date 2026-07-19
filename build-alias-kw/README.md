# build-alias-kw — `joltc build` rejects `::alias/kw` keywords

Minimal reproduction: alias-resolved auto keywords (`::str/trim-mode`
with `[clojure.string :as str]` required) work under `joltc run` and
the REPL, but `joltc build` fails while scanning requires:

```sh
$ joltc -M:run
config: {:clojure.string/trim-mode :both}
ok — alias-resolved keywords work at runtime

$ joltc build -m app.core -o app
Unhandled exception: Invalid token: ::str/trim-mode
  trace:
    rdr-read-keyword
    ...
    bld-ns-requires
    dfs
    bld-require-closure
    build-binary
```

## Why

The normal loader reads and **evaluates** one form at a time — the
`(ns …)` form runs first and registers its aliases, so later
`::alias/kw` reads resolve. `joltc build`'s dependency scanner
(`bld-ns-requires` → `ei-read-all`, `host/chez/build.ss`) reads **all**
top-level forms up front without evaluating the ns form, so
`chez-resolve-alias` finds nothing and `rdr-read-keyword` throws.
Self-namespaced `::kw` (no alias) builds fine.

Hit in practice porting the liquid editor to jolt (b12n-liquid-jlt):
worked around by rewriting 411 `::alias/kw` occurrences to
fully-qualified `:ns/kw`.

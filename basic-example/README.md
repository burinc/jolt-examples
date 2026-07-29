# basic-example

A jolt app that starts a minimal app.

## Run in dev

Interpreted, with the nREPL server on `127.0.0.1:7888`:

```
jolt run -m app.core
```

Start nREPL with:

```
jolt nrepl
```

## Compile for release

A self-contained native binary (no `jolt`, no JVM, ~MBs):

```
jolt build -m app.core -o basic-example
./basic-example
```

## Requirements

- `jolt` on PATH.
- The jolt-lang/nrepl dependency is fetched automatically from git on first run.

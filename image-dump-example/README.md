# image-dump-example

A reactive todo board that can **export its state to a file and import it back** —
an example of [`jolt.image`](https://jolt-lang.github.io/docs/rfc/0009-program-image-dump-restore.html),
built with [glimmer](https://github.com/jolt-lang/glimmer), a reagent-style
reactive GUI toolkit over GTK4 for the [Jolt](https://github.com/jolt-lang/jolt)
Clojure dialect.

## State export / import

The navbar at the top has **export** and **import**. Export writes the current
board to `todos.jimg`; import replaces the board with whatever is in that file.
Add a few todos, export, change things, then import to get the exported board
back. The file survives restarts, and can be moved to another machine — even one
on a different CPU architecture — and imported by the same build of the app
there.

What gets written is `@state`, the plain-data value, not the ratom:

```clojure
(image/dump! "todos.jimg" @state)          ; export
(reset! state (image/read-image "todos.jimg"))  ; import
```

Dumping the ratom itself would drag glimmer's watch closures in, and an
anonymous closure has no name to write, so `jolt.image` would refuse it. That is
the general rule for images: data travels, and functions travel only when they
are named. `(image/scan @state)` returns an empty vector here, which is how you
check a value is writable before trying.

Restoring the root value is enough to restore the whole UI: the cursors and
reactions are derived from that one atom, so they recompute and every component
follows.

## What else it showcases

- **reactive atom** — one `atom` holds the entire board (tasks, filter, sort
  flag, draft, next id); it's the single source of truth.
- **cursors** — writable lenses over `[:draft]`, `[:filter]`, and
  `[:sort-done-last]`; writing a cursor updates the root atom and everything
  derived from it.
- **reactions** — read-only derived cells: `remaining`, `done-count`, and the
  `visible` task list (depends on the filter and sort cells at once).
- **Form-1 components** (`app.widgets`) — `stat-card`, `filter-bar`,
  `command-bar`; plain functions returning hiccup.
- **Form-2 component** (`app.core/task-board`) — creates state, cursors, and
  reactions once on mount, then renders from them.
- **every event kind** — `:on-change` and `:on-activate` (entry), `:on-click`
  (buttons), `:on-toggled` (checkbutton).
- **keyed list rendering** driven by a reaction — each row is keyed by task id,
  so adding, deleting, reordering (sort), and filtering reuse the right widgets
  instead of recreating by position.
- **interactive rows** — every row has its own toggle checkbox and delete button
  whose handlers close over the task id (not a list index), plus bulk mutations
  (`complete all` / `mark all active`, `clear completed`) and live counts.

## Layout

```
image-dump-example/
├── deps.edn             ; :local/root ../../glimmer inherits its source + GTK4 native libs
└── src/app/
    ├── core.clj         ; state, cursors, reactions, mutations, export/import, run
    └── widgets.clj      ; Form-1 reusable components, incl. the navbar
```

## Run it

```sh
jolt -M:run        # or: jolt run
```

This opens the window and blocks until you close it.

## Develop it live from your editor

Start an nREPL server and connect your editor (Calva, CIDER, Cursive):

```sh
jolt nrepl-server        # writes .nrepl-port; ^C to stop
```

Then evaluate `(app.core/-main)` to open the window. The eval returns right away
and the window keeps running, so you can keep working in the same session.

Two kinds of edits show up live, both in the same window:

- **State**, the reagent way: mutate a ratom (`(swap! ...)` / `(reset! ...)`) and
  the parts of the UI that deref it re-render.
- **Component code**: redefine a component function, re-evaluate it, then call
  `(glimmer.core/reload!)` to re-render the running window in place.
  `reload!` re-runs the root and re-resolves the child components it renders, so
  redefined children take effect. To swap the root itself after redefining it,
  pass it: `(glimmer.core/reload! app.core/todo-app)`. Reloading rebuilds the
  tree, so the task list resets to its defaults.

The GUI runs on the process main thread while your evaluations run on nREPL
worker threads. glimmer marshals every re-render (reactive updates and `reload!`)
back onto the main loop for you, since GTK (and AppKit on macOS) reject widget
mutation off the main thread.

## Build a standalone binary

```sh
jolt build -m app.core
./target/release/image-dump-example
```

The binary loads the GTK4/glib shared libraries at startup; they must be
installed (Homebrew on macOS: `brew install gtk4`).

## Design note

Task rows are keyed by task id (`[task-row {:key id} ...]`). glimmer's reconciler
matches keyed children by identity rather than position, so a row's widgets and
its once-wired signals follow the same task as the list is added to, deleted
from, reordered (the done-last sort), or filtered. That's what lets each row own
a toggle and delete handler bound to its id without ever capturing a stale index.
Signals are still wired once at mount; the handlers close over the id and the
root atom (stable), never over a position or a value. See glimmer's README for
how keyed reconciliation works.

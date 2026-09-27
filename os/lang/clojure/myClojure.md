# Clojure Syntax & Reader Guide

Reference guide for Clojure syntax, data types, collections, and evaluation model based on the [Clojure Official Guides](https://clojure.org/guides/learn/syntax).

---

## 1. Literal Data Types

### Numeric Types

```clojure
42        ; integer
-1.5      ; floating point
22/7      ; ratio
```

### Character & String Types

```clojure
"hello"         ; string
\e              ; character
#"[0-9]+"       ; regular expression
```

### Symbols and Identifiers

```clojure
map             ; symbol
+               ; symbol (most punctuation allowed)
clojure.core/+  ; namespaced symbol
nil             ; null value
true false      ; booleans
:alpha          ; keyword
:release/alpha  ; keyword with namespace
```

---

## 2. Literal Collections

Clojure provides literal syntax for four foundational immutable collection types:

```clojure
'(1 2 3)        ; list (sequential, linked list)
[1 2 3]         ; vector (indexed, random access)
#{1 2 3}        ; set (unique elements)
{:a 1, :b 2}    ; map (key-value pairs)
```

---

## 3. Evaluation Model

In Clojure, source code is read as characters by the **Reader**. The Reader produces Clojure data structures (forms), which the **Compiler** then compiles to JVM bytecode.

- **Unit of Source**: A Clojure expression (form), whether from a `.clj` file or interactively in a REPL.
- **Code is Data (Homoiconicity)**: Macros intercept data structures produced by the Reader and transform them before compilation.

### Delaying Evaluation with Quoting

To treat expressions or symbols as literal data rather than code to evaluate:

```clojure
user=> 'x
x

user=> '(1 2 3)
(1 2 3)

user=> (+ 3 4)
7
```

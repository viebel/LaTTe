# LaTTe

```text
             ((((
            ((((
             ))))
          _ .---.
         ( |`---'|
          \|     |
          : .___, :
           `-----'  -Karl
```

**LaTTe** : a Laboratory for Type Theory experiments (in clojure)

[![Clojars Project](https://img.shields.io/clojars/v/latte.svg)](https://clojars.org/latte)

## What?

LaTTe is a **proof assistant library** based on type theory (a variant of
λD as described in the book [Type theory and formal proof: an introduction](http://www.cambridge.org/fr/academic/subjects/computer-science/programming-languages-and-applied-logic/type-theory-and-formal-proof-introduction)).
 
The specific feature of LaTTe is its design as a library (unlike most proof assistant, generally designed as tools) tightly integrated with the Clojure language. It is of course fully implemented in Clojure, but most importantly all the definitional aspects of the assistant (definitions, theorem and axioms) are handled using Clojure namespaces, definitions and macros.

For example, the fact that logical implication is reflexive can be stated *directly as a Clojure top-level form*:

```clojure
(defthm impl-refl
  "Implication is reflexive."
  [[A :type]]
  (==> A A))
```
in plain text:
> assuming a type `A`, then `A` implies `A`.

The proof of the theorem can be also constructed as a Clojure form:

  - either giving a lambda-term as a direct proof (exploiting the proposition-as-type, proof-as-term correspondance) :

```clojure
(proof impl-refl
  :term (lambda [x A] x))
```
(i.e. the identity function is a proof of reflexivity for implication)

  - or using a declarative *proof script*:

```clojure
(proof impl-refl
       :script
       (assume [x A]
         (have concl A :by x)
         (qed concl)))
```

... which, with some training, can be read as a "standard" mathematical proof:

> assuming `A` holds, as an hypothesis named `x`
> we can deduce `A` by `x`
> hence `A` implies `A` as stated (QED).
  
Of course, all the proofs are *checked for correctness*. Without the introduction
 of an inconsistent axiom (and assuming the correctness of the implementation of the LaTTe kernel),
 *no mathematical inconsistency* can be introduced by the `proof` form.

## Yes, but what?

LaTTe helps you formalize mathematic concepts and construct formal proofs of theorems (propositions) about such concepts.
Given the tight integration with the Clojure language, *existing Clojure development environments* (e.g. Cider) can be used as an interactive proof assistant.

## How?

There will be a *tutorial* at some point ...

## Who?

LaTTe may be of some interest for you:

  - **obviously** if you are interested in type theory and the way it can be implemented on a Computer. LaTTe has been implemented with readability and simplicify in mind (more so than efficiency or correctness),
  - **probably** if you are interested in the "mechanical" formalization of mathematics, intuitionistic logic, etc. (although you might not learn much, you may be interested in contributing definitions, theorems and proofs),
  - **maybe** if you are curious about the lambda-calculus (the underlying theory of your favorite programming language) and dependent types (a current trend) and what you can do with these besides programming.

## When?

LaTTe is, at least for now, an experiment more than a finalized product, but it is already usable.

A few non-trivial formalizations have been conducted using LaTTe:

 - some **typed set theory**: https://github.com/fredokun/latte-sets
 - a (starting) formalization of **integer arithmetics**: https://github.com/fredokun/latte-integers 
 - a (gorilla REPL) document about **Knaster-Tarski fixed point theorem(s)**: https://github.com/fredokun/fixed-points

Contributions such as mathematical content or enhancement/correction of the underlying machinery are very much welcomed.

----
Copyright (C) 2015-2016 Frederic Peschanski (MIT license, cf. `LICENSE`)

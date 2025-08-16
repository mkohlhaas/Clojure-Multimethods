(ns multimethods.core
  (:require [clojure.math :as m]))

;; derive
;; parents
;; ancestors
;; descendants
;; isa?
;; defmulti  (dispatch-fn)
;; defmethod (dispatch-val)

::rect
; :multimethods.core/rect

;; ;;;;;;;;;
;; Hierarchy
;; ;;;;;;;;;

(derive ::rect   ::shape)
(derive ::square ::rect)

(parents ::rect)
; #{:multimethods.core/shape}

(ancestors ::square)
; #{:multimethods.core/shape :multimethods.core/rect}

(descendants ::shape)
; #{:multimethods.core/rect :multimethods.core/square}

;; ;;;;
;; isa?
;; ;;;;

(isa? 42 42) ; true

(isa? ::square ::shape)  ; true
(isa? ::shape  ::square) ; false

(derive java.util.Map        ::collection)
(derive java.util.Collection ::collection)

(isa? java.util.HashMap ::collection) ; true

(isa? String Object) ; true

(ancestors java.util.ArrayList)
; #{java.util.AbstractCollection
;   java.lang.Iterable
;   :multimethods.core/collection
;   java.util.Collection
;   java.lang.Object
;   java.io.Serializable
;   java.util.List
;   java.util.RandomAccess
;   java.util.AbstractList
;   java.lang.Cloneable
;   java.util.SequencedCollection}

(isa? [::square ::rect] [::shape ::shape]) ; true

;; ;;;;;;;;;;;;;;;;;;;
;; isa? based dispatch
;; ;;;;;;;;;;;;;;;;;;;

;; `class` is the dispatch function
(defmulti  foo class)

;; `::collection` and `String` are the dispatch values
(defmethod foo ::collection [_c] :a-collection)
(defmethod foo String       [_s] :a-string)

(ancestors clojure.lang.PersistentVector)
; #{clojure.lang.Seqable
;   ...
;   :multimethods.core/collection
;   ...
;   java.lang.Runnable}

(class [])                   ; clojure.lang.PersistentVector
(class (java.util.HashMap.)) ; java.util.HashMap
(class "bar")                ; java.lang.String

(isa? clojure.lang.PersistentVector ::collection) ; true
(isa? java.util.HashMap             ::collection) ; true
(isa? java.lang.String              String)       ; true

(foo [])                   ; :a-collection
(foo (java.util.HashMap.)) ; :a-collection
(foo "bar")                ; :a-string

;; ;;;;;;;;;;;;;
;; prefer-method
;; ;;;;;;;;;;;;;

(defmulti  bar (fn [x y] [x y]))

(defmethod bar [::rect  ::shape] [_x _y] :rect-shape)
(defmethod bar [::shape ::rect]  [_x _y] :shape-rect)

(comment
  (bar ::rect ::rect))
; (err) Multiple methods in multimethod 'bar' match dispatch value

;; tie breaker
;; prefer         `[::rect ::shape]` over `[::shape ::rect])`
(prefer-method bar [::rect ::shape]        [::shape ::rect])

(bar ::rect ::rect) ; :rect-shape

;; ;;;;;;;;;;;;
;; Area Example
;; ;;;;;;;;;;;;

;; constructor functions
(defn rect   [wd ht]  {:Shape :Rect :wd wd :ht ht})
(defn circle [radius] {:Shape :Circle :radius radius})

(def r (rect 4 13)) ; {:Shape :Rect, :wd 4, :ht 13}
(def c (circle 12)) ; {:Shape :Circle, :radius 12}

;; `:Shape` is the dispatch function
(defmulti  area :Shape)

;; dispatch values
(:Shape r) ; :Rect
(:Shape c) ; :Circle

;;              dispatch-val
(defmethod area :Rect        [rect]   (* (:wd rect) (:ht rect)))
(defmethod area :Circle      [circle] (* m/PI (:radius circle) (:radius circle)))
(defmethod area :default     [_x]     :oops)

(area r)  ; 52
(area c)  ; 452.3893421169302
(area {}) ; :oops

(ns strmatch.logic.brute-force
  (:use [strmatch.logic.common :only [discrepancy-index color-array]]))

; for a needle at a given index, we want to highlight all the
; contiguous values from the left which match up with the haystack.
; we also want a red at the end for a mismatch.
(defn- colors-for [needle haystack padding]
  (vec (let [discrep (discrepancy-index needle haystack padding)
             length-of-match (or discrep (count needle))]
         (concat 
           (map (fn [i] { :color :green
                         :index i })
                (range 0 length-of-match))
           (if discrep [{ :color :red
                          :index length-of-match}] [])))))

(defn match
  ([needle haystack] (vec (reverse (match needle haystack 0 nil))))
  ([needle haystack index result]
   (let [discrepancy (discrepancy-index needle haystack index)
         colors (colors-for needle haystack index)
         entry {:index index :colors colors}
         next-result (cons entry result)
         not-at-end (<= index (count haystack))]
     (if (and discrepancy not-at-end)
       (recur needle haystack (inc index) next-result)
       next-result))))

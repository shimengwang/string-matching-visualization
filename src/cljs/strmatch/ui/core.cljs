(ns strmatch.ui.core
  (:require [strmatch.logic.kmp-matcher]
            [strmatch.logic.brute-force]
            [strmatch.logic.boyer-moore])
  (:use [jayq.core :only [$ val css html append delegate anim dequeue]]
        [jayq.util :only [log]])
  (:use-macros [jayq.macros :only [queue]]))


(def $go ($ :#go))
(def $needle-input ($ :#needle-input))
(def $haystack-input ($ :#haystack-input))

(def $needle ($ :#needle))
(def $haystack ($ :#haystack))

(delegate $go "" :click
          (fn [e]
            (let [needle (val $needle-input)
                  haystack (val $haystack-input)]
              (show-match needle haystack))))

(defn match-fn
  []
  ({"naive"  strmatch.logic.brute-force/match
    "kmp" strmatch.logic.kmp-matcher/match
    "boyer_moore" strmatch.logic.boyer-moore/match
    }
   (.-value (first ($ ".algbutton:checked")))))

(defn set-dimens
  [w h]
  (html ($ :#display)
        (str
          (map (fn [y]
                 (str "<tr>"
                      (map (fn [x]
                             (str "<td class='cell' id='display" x "x" y "'></td>"))
                           (range 0 w))
                      "</tr>"))
               (range 0 h)))))

(def color-for
  {:green "#81F13D"
   :red "#FF0000"
   :white "#FFFFFF"})

(defn set-cell
  [x y value color]
  (let [el ($ (str "#display" x "x" y))]
    (html el value)
    (when color
      (css el "background-color" (color-for color)))))

(defn set-row
  [row value colors]
  (let [cs (vec (seq value))]
    (doseq [x (range 0 (count cs))]
      (set-cell x row (cs x) (colors x)))))

; (defn show-match
;   [needle haystack]
;   (let [match-result ((match-fn) needle haystack)
;         results match-result]
;     (set-dimens (count haystack) (inc (count results)))
;     (set-row 0 haystack #{})
;     (doall
;       (map-indexed
;         (fn [i result]
;           (set-row (inc i)
;                    (str (apply str (repeat (:index result) " ")) needle)
;                    (vec (:colors result))))
;         results))))

(def div-width 30)

(defn set-value-divs 
  [$elem value]
  (.empty $elem)
  (doseq [[i char] (map-indexed vector value)]
    (append $elem 
            (str "<div "
                 "style='width:" div-width "px;float:left'"
                 "id='cell" i "'"
                 "class='cell'"
                 ">" (if (= char \space) "&nbsp;" char)
                 "</div>"))))

(defn color [$elem col index]
  (let [actual-color (color-for col)]
    (queue $elem
           (anim (.children $elem (str "#cell" index))
                 {:background-color actual-color}
                 200)
           (dequeue $elem))
    $elem))

(defn animate-match
  [match-result $elem]
  (loop [results match-result]
    (if (empty? results)
      $elem
      (let [index (:index (first results))
            colors (:colors (first results))]
        (queue $elem
               (-> $elem
                   .children
                   (css :background-color "#FFFFFF"))
               (dequeue $elem))
        (-> $elem
            (anim {:left (* div-width index)} 250)
            (.delay 250)
            )
        (doseq [color-event colors]
          (-> $elem
              (color (:color color-event)
                     (:index color-event))
              (.delay 500)))
        (recur (rest results))))))

(defn show-match
  [needle haystack]
  (let [match-result ((match-fn) needle haystack)]
    (set-value-divs $haystack haystack)
    (set-value-divs $needle needle)
    (animate-match match-result $needle)))

(ns reaper.features.tfidf
  (:require
    [reaper.util :as util]
    [reaper.tools.stop-words :refer [tokens->stop-words]]
    [reaper.tools.porter-stemmer :refer [tokens->stemmed-tokens]]
    [reaper.tools.tfidf :refer [corpus->tfidf-model tfidf-model->top-n-terms]]))

(defn make-tfidf-vectorizer
  "Produces a fn that takes a string and computes a tf*idf vector representation.
If passed a query seq of tokens it will produce a vector with n elements, and
|query| additional elements."
  [corpus n & {:keys [remove-stopwords
                      stem]
               :or {remove-stopwords false
                    stem false}}]
  (let
    [tfidf-model (corpus->tfidf-model
                   corpus
                   :remove-stopwords remove-stopwords
                   :stem stem)
     top-n-terms (tfidf-model->top-n-terms tfidf-model n)
     w->idx (zipmap top-n-terms (range n))]
    (fn [s]
      (let
        [s (util/tokenize-lower s)
         s (if remove-stopwords (tokens->stop-words s) s)
         s (if stem (tokens->stemmed-tokens s) s)]
        (reduce #(assoc %1 (w->idx %2) (get tfidf-model %2 0))
                (vec (repeat n 0))
                (filter #(some #{%} (keys w->idx)) s))))))

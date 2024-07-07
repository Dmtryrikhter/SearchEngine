package searchengine.services;

import searchengine.model.Lemma;

import java.util.Comparator;

public class ComparatorLemmas implements Comparator<Lemma> {
    @Override
    public int compare(Lemma o1, Lemma o2) {
        return o2.getFrequency() - o1.getFrequency();
    }
}

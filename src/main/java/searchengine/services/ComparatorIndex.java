package searchengine.services;

import searchengine.model.Index;

import java.util.Comparator;

public class ComparatorIndex implements Comparator<Index> {
    @Override
    public int compare(Index o1, Index o2) {
        return o1.getPageId() - o2.getPageId();
    }
}

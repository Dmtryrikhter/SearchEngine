package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.search.ResponseSearch;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final Lemmatizer lemmatizer = new Lemmatizer();
    private final List<Lemma> lemmas = new ArrayList<>();
    private List<Index> pages = new ArrayList<>();

    @Override
    public ResponseSearch search( String query,  String site,
                                  String offset,  String limit) {
        try {
            HashMap<String, Integer> queryLemmaList = lemmatizer.getLemmas(query);
            for (Map.Entry<String, Integer> entry : queryLemmaList.entrySet()) {
                Lemma lemma = lemmaRepository.findLemmaByLemma(entry.getKey());
                if (lemma.getFrequency() < 30) {
                    lemmas.add(lemma);
                }
            }
            lemmas.sort(new ComparatorLemmas());
            for (Lemma lemma : lemmas) {
                pages = sortedPage(lemma);
            }
            List<Index> pagesList = pages;
            TreeMap<Float, Integer> pagesRelevance = getRelevance(pagesList);

        } catch (IOException e) {
            throw new RuntimeException(e);

        }
        return new ResponseSearch();
    }
    private List<Index> sortedPage(Lemma lemma) {
        List<Index> result = new ArrayList<>();
        if (pages.isEmpty()) {
            List<Index> list = indexRepository.findAllByLemmaId(lemma.getId());
            result.addAll(list);
        }else {
            for (Index index : pages) {
                List<Index> list = indexRepository.findAllByLemmaId(lemma.getId());
                for (Index index1 : list) {
                    if (index1.getPageId() == index.getPageId()) {
                        result.add(index);
                    }
                }
            }
        }
        return result;
    }
    private HashMap<Float, Integer> getAbsoluteRelevance(List<Index> pagesList) {
        HashMap<Float, Integer> pagesARelevance = new HashMap<>();
        if (!pagesList.isEmpty()) {
            pagesList.sort(new ComparatorIndex());
            int pageId = pagesList.get(0).getPageId();
            float absoluteRelevance = 0f;
            for (Index index : pagesList) {
                if (index.getPageId() == pageId) {
                    absoluteRelevance += index.getRank();
                }
            }
            pagesARelevance.put(absoluteRelevance, pagesList.get(0).getId());
            pagesList.remove(0);
            getAbsoluteRelevance(pagesList);
        }
        return pagesARelevance;
    }
    private TreeMap<Float, Integer> getRelevance(List<Index> pagesList) {
        TreeMap<Float, Integer> pagesRelevance = new TreeMap<>();
        HashMap<Float, Integer> pagesAbsoluteRelevance = getAbsoluteRelevance(pagesList);
        float relevance;
        for (Map.Entry<Float, Integer> entry : pagesAbsoluteRelevance.entrySet()) {
            float maxAbsoluteRelevance = 0f;
            for (Map.Entry<Float, Integer> entry1 : pagesAbsoluteRelevance.entrySet()) {
                maxAbsoluteRelevance += entry1.getKey();
            }
            relevance = entry.getKey() / maxAbsoluteRelevance;
            pagesRelevance.put(relevance, entry.getValue());
        }
        return pagesRelevance;
    }
}

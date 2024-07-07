package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import searchengine.dto.search.DataDTO;
import searchengine.dto.search.ResponseSearch;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.SiteEntity;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final Lemmatizer lemmatizer = new Lemmatizer();
    private final List<Lemma> lemmas = new ArrayList<>();
    private List<Index> pages = new ArrayList<>();
    private DataDTO[] dtos;
    private DataDTO dataDTO;
    private ResponseSearch responseSearch;

    @Override
    public ResponseSearch search( String query,  String site,
                                  String offset,  String limit) {
        try {
            if (limit == null) {
                dtos = new DataDTO[20];
            }else {
                dtos = new DataDTO[Integer.parseInt(limit)];
            }
            getLemmasForQuery(query);
            for (Lemma lemma : lemmas) {
                pages = sortedPage(lemma);
            }
            List<Index> pagesList = pages;
            TreeMap<Float, Integer> pagesRelevance = getRelevance(pagesList);
            int i = 0;
            for (Map.Entry<Float, Integer> entry : pagesRelevance.entrySet()) {
                i++;
                Page page = pageRepository.findById(entry.getValue()).get();
                SiteEntity siteEntity = siteRepository.findById(page.getSiteId()).get();
                dataDTO = new DataDTO();
                dataDTO.setRelevance(entry.getKey());
                dataDTO.setSiteName(siteEntity.getName());
                dataDTO.setSite(page.getPath());
                dataDTO.setTitle(getTitle(siteEntity.getUrl() + page.getPath()));
                dataDTO.setSnippet(getSnippet(page.getContent(), query));
                dtos[i] = dataDTO;
            }
            responseSearch = new ResponseSearch();
            responseSearch.setCount(dtos.length);
            responseSearch.setData(dtos);

        } catch (IOException e) {
            throw new RuntimeException(e);

        }
        return responseSearch;
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
    private String getTitle(String pageLink) throws IOException {
        Document document = Jsoup.connect(pageLink).get();
        Elements elements = document.select("meta[property=og:title]");
        return elements.attr("content");
    }
    private String getSnippet(String content, String query) throws IOException {
        StringBuilder sb = new StringBuilder();
        List<Integer> correctIndices = new ArrayList<>();
        int index = content.toLowerCase().indexOf(query.toLowerCase());
        while (index != -1) {
            correctIndices.add(index);
            index = content.toLowerCase().indexOf(query.toLowerCase(), index + 1);
        }
        for (int i = 0; i < correctIndices.size(); i++) {
            int start = Math.max(0, correctIndices.size() - 100);
            int end = Math.min(content.length(), correctIndices.get(i) + query.length() + 100);
            String snippetPart = content.substring(start, end);
            String text = Jsoup.parse(snippetPart).text().replaceAll("<.*?>", "");
            text = text.toLowerCase().replaceAll(query.toLowerCase(), "<b>" + query.toLowerCase() + "<b>");
            sb.append("...").append(text).append("...");
        }
        return sb.toString();
    }
    private void getLemmasForQuery(String query) throws IOException {
        HashMap<String, Integer> queryLemmaList = lemmatizer.getLemmas(query);
        for (Map.Entry<String, Integer> entry : queryLemmaList.entrySet()) {
            Lemma lemma = lemmaRepository.findLemmaByLemma(entry.getKey());
            if (lemma.getFrequency() < 30) {
                lemmas.add(lemma);
            }
        }
        lemmas.sort(new ComparatorLemmas());
    }
}

package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.config.ConnectionList;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.ResponseIndexing;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.SiteEntity;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
@Service
@RequiredArgsConstructor
public class IndexingServiseImpl implements IndexingService{
    private final SitesList sitesList;
    private final IndexRepository indexRepository;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final ConnectionList connectionList;

    private boolean stop = false;
    private StringBuilder regex;
    ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();

    @Override
    public ResponseIndexing startIndexing(){
        stop = false;
        for (Site site : sitesList.getSites()) {
            forkJoinPool.submit(() ->siteIndexing(site));
        }
        ResponseIndexing responseIndexing = new ResponseIndexing();
        if (!forkJoinPool.isQuiescent()) {
            responseIndexing.setResult(true);
        }else {
            responseIndexing.setResult(false);
            responseIndexing.setMessage("Индексация уже запущена.");
        }
        return responseIndexing;
    }

    @Override
    public ResponseIndexing stopIndexing() {
        ResponseIndexing responseIndexing = new ResponseIndexing();
        if (!forkJoinPool.isQuiescent()) {
            responseIndexing.setResult(true);
            forkJoinPool.shutdown();
            stop = true;
            siteRepository.flush();
            pageRepository.flush();
        }else {
            responseIndexing.setResult(false);
            responseIndexing.setMessage("Индексация не запущена");
        }
        return responseIndexing;
    }
    @Override
    public ResponseIndexing pageIndexing(String pageLink) throws IOException {
        ResponseIndexing responseIndexing = new ResponseIndexing();
        Connection connection = Jsoup.connect(pageLink);
        String path = pageLink.substring(8);
        if (!pageRepository.findAllPageByPath(path.substring(path.indexOf('/'))).isEmpty()) {
            Page p = pageRepository.findAllPageByPath(path.substring(path.indexOf('/'))).get(0);
            regex = new StringBuilder();
            regex.append("(^").append(siteRepository.findById(p.getSiteId()).get().getUrl()).append(")");
            pageRepository.deletePageByPath(pageLink.replaceAll(regex.toString(),""));
            List<Index> indexes = indexRepository.findIndexByPageId(p.getId());
            indexes.forEach(index -> lemmaRepository.deleteById(index.getLemmaId()));
            indexes.forEach(index -> indexRepository.deleteById(index.getId()));
            pageIndexing(pageLink);
        } else {
            for (Site site : sitesList.getSites()) {
                if (pageLink.contains(site.getUrl())) {
                    pageRepository.save(pageProcessing(connection, site));
                    Document document = connection.get();
                    Page page = pageRepository.findAllPageByPath(path.substring(path.indexOf('/'))).get(0);
                    lemmasAndIndexProcessing(document.text(), page.getSiteId(), page.getId());
                    responseIndexing.setResult(true);
                    responseIndexing.setMessage("");
                } else {
                    responseIndexing.setResult(false);
                    responseIndexing.setMessage("Данная страница находится за пределами сайтов,\n" +
                            "указанных в конфигурационном файле.");
                }
            }
        }
        return responseIndexing;

    }

    public Page pageProcessing(Connection connection, Site site) {
        try {
            Document doc = connection.get();
            Page page = new Page();
            page.setCode(connection.response().statusCode());
            regex = new StringBuilder();
            regex.append("(^").append(site.getUrl()).append(")");
            page.setPath(doc.baseUri().replaceAll(regex.toString(),""));
            page.setContent(doc.toString());
            StringBuilder sb = new StringBuilder();
            sb.append(doc.baseUri().split("/")[0])
                    .append("//").append(doc.baseUri().split("/")[1])
                    .append(doc.baseUri().split("/")[2]).append("/");
            int siteId = 0;
            if (!siteRepository.findAllSiteByUrl(sb.toString()).isEmpty()) {
                siteId = siteRepository.findAllSiteByUrl(sb.toString()).get(0).get().getId();
            } else if (siteRepository.findAllSiteByUrl(sb.toString()).isEmpty()) {
                SiteEntity site1 = new SiteEntity();
                site1.setUrl(sb.toString());
                site1.setName(site.getName());
                site1.setStatusTime(LocalDateTime.now());
                site1.setStatus("INDEXED");
                siteRepository.save(site1);
                siteId = siteRepository.findAllSiteByUrl(sb.toString()).get(0).get().getId();
            }
            page.setSiteId(siteId);
            return page;
        }catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    public void siteIndexing(Site site) {
        if (!siteRepository.findAllSiteByUrl(site.getUrl()).isEmpty()) {
            siteRepository.deleteSiteByName(site.getName());
            siteIndexing(site);

        }else {
            SiteEntity siteEntity1 = new SiteEntity();
            SiteCrawler siteCrawler = new SiteCrawler(site.getUrl(), connectionList);
            siteEntity1.setName(site.getName());
            siteEntity1.setUrl(site.getUrl());
            siteEntity1.setStatusTime(LocalDateTime.now());
            siteEntity1.setStatus("INDEXING");
            siteRepository.save(siteEntity1);
            Set<String> invoke = forkJoinPool.invoke(siteCrawler);
            for (String pageLink : invoke) {
                if (stop) {
                    break;
                }
                forkJoinPool.submit(() -> pageIndexing(pageLink));
            }
            if (!forkJoinPool.isTerminated()) {
                siteEntity1.setStatusTime(LocalDateTime.now());
                siteEntity1.setStatus("INDEXED");
                siteRepository.save(siteEntity1);
            }else if(forkJoinPool.isTerminated()){
                siteEntity1.setStatusTime(LocalDateTime.now());
                siteEntity1.setStatus("FAILED");
                siteRepository.save(siteEntity1);
            }
        }
    }
    public void lemmasAndIndexProcessing(String text, Integer siteId, Integer pageId) {
        try {
            Lemmatizer lemmatizer = new Lemmatizer();
            HashMap<String, Integer> lemmas = lemmatizer.getLemmas(text);
            for (Map.Entry<String, Integer> entry : lemmas.entrySet()) {
                Lemma lemma = lemmaRepository.findLemmaByLemma(entry.getKey());
                Index index = new Index();
                if (lemma != null) {
                    lemma.setFrequency(lemma.getFrequency() + 1);
                    lemmaRepository.save(lemma);
                }else {
                    lemma = new Lemma();
                    lemma.setLemma(entry.getKey());
                    lemma.setSiteId(siteId);
                    lemma.setFrequency(1);
                    lemmaRepository.save(lemma);
                }
                index.setLemmaId(lemma.getId());
                index.setPageId(pageId);
                index.setRank(entry.getValue());
                indexRepository.save(index);
            }
        }catch (IOException ex) {
            ex.printStackTrace();
        }
    }


}

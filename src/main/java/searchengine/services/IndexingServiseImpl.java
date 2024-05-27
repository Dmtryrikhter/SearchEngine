package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.ResponseIndexing;
import searchengine.model.Page;
import searchengine.model.SiteEntity;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
@Service
@RequiredArgsConstructor
public class IndexingServiseImpl implements IndexingService{
    private final SitesList sitesList;
    private final IndexRepository indexRepository;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();

    @Override
    public ResponseIndexing startIndexing(){
        ResponseIndexing responseIndexing = new ResponseIndexing();
        if (forkJoinPool.isQuiescent()) {
            responseIndexing.setResult(true);
        }else {
            responseIndexing.setResult(false);
            responseIndexing.setMessage("Индексация уже запущена.");
        }
        siteIndexing();
        return responseIndexing;
    }
    public void pageIndexing(String pageLink) {
        try {
            Connection connection = Jsoup.connect(pageLink);
            Document doc = connection.get();
            Page page = new Page();
            page.setCode(connection.response().statusCode());
            page.setPath(doc.baseUri().substring(doc.baseUri().lastIndexOf('/')));
            page.setContent(doc.toString());
            StringBuilder sb = new StringBuilder();
            sb.append(doc.baseUri().split("/")[0])
                    .append("//").append(doc.baseUri().split("/")[1])
                    .append(doc.baseUri().split("/")[2]).append("/");
            page.setSiteId(siteRepository.findAllSiteByUrl(sb.toString()).get(0).get().getId());
            pageRepository.save(page);
        }catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    public void siteIndexing() {
        for (Site site : sitesList.getSites()) {
            for (int i = 0; i <= siteRepository.count(); i++) {
                if (siteRepository.findById(i).isPresent()) {
                    SiteEntity siteEntity = siteRepository.findById(i).get();
                    if (siteEntity.getUrl().strip().equals(site.getUrl().strip())) {
                        siteRepository.delete(siteEntity);
                    }
                }
            }
            SiteEntity siteEntity1 = new SiteEntity();
            SiteCrawler siteCrawler = new SiteCrawler(site.getUrl());
            siteEntity1.setName(site.getName());
            siteEntity1.setUrl(site.getUrl());
            siteEntity1.setStatusTime(LocalDateTime.now());
            siteEntity1.setStatus("INDEXING");
            siteRepository.save(siteEntity1);
            for (int i = 0; i < sitesList.getSites().size(); i++) {
                Set<String> invoke = forkJoinPool.invoke(siteCrawler);
                System.out.println(invoke);
                for (String pageLink : invoke) {
                    try {
                        pageIndexing(pageLink);
                        Thread.sleep(150);
                    } catch (InterruptedException exception) {
                        exception.printStackTrace();
                    }
                }
            }
            if (forkJoinPool.isTerminated()) {
                siteEntity1.setStatusTime(LocalDateTime.now());
                siteEntity1.setStatus("INDEXED");
                siteRepository.save(siteEntity1);
            } else {
                siteEntity1.setStatusTime(LocalDateTime.now());
                siteEntity1.setStatus("FAILED");
                siteRepository.save(siteEntity1);
            }
        }
    }

}

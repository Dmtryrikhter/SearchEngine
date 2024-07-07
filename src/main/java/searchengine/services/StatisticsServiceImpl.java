package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.Page;
import searchengine.model.SiteEntity;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SitesList sites;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    @Override
    public StatisticsResponse getStatistics() {
        StatisticsResponse response = new StatisticsResponse();
        response.setResult(false);
        StatisticsData data = new StatisticsData();
        TotalStatistics total = new TotalStatistics();
        total.setSites(sites.getSites().size());
        total.setIndexing(true);
        total.setSites((int) siteRepository.count());
        total.setPages((int) pageRepository.count());
        total.setLemmas((int) lemmaRepository.count());
        for (SiteEntity site : siteRepository.findAll()) {
            total.setIndexing(!site.getStatus().equals("FAILED"));
        }
        data.setTotal(total);
        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        for(Site site : sites.getSites()) {
            detailed.add(createItem(site));
        }
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }
    private DetailedStatisticsItem createItem(Site site) {
        DetailedStatisticsItem item = new DetailedStatisticsItem();
        item.setUrl(site.getUrl());
        item.setName(site.getName());
        SiteEntity siteEntity = siteRepository.findAllSiteByUrl(site.getUrl()).get(0).get();
        item.setStatus(siteEntity.getStatus());
        LocalDateTime localDateTime = siteEntity.getStatusTime();
        ZonedDateTime zdt = ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
        long date = zdt.toInstant().toEpochMilli();
        item.setStatusTime(date);
        List<Optional<Page>> pages = pageRepository.findAllBySiteId(siteEntity.getId());
        if (siteEntity.getStatus().equals("FAILED")) {
            item.setError("Ошибка индексации: главная " + System.lineSeparator() +
                    "страница сайта недоступна");

        }
        item.setPages(pages.size());
        item.setLemmas(lemmaRepository.findAllLemmaBySiteId(siteEntity.getId()).size());
        return item;
    }
}

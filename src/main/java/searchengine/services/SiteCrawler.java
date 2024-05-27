package searchengine.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.RecursiveTask;

public class SiteCrawler extends RecursiveTask<Set<String>> {
    private final String url;
    private final Set<String> resultSet = new LinkedHashSet<>();

    public SiteCrawler(String url) {
        this.url = url;
    }

    protected Set<String> connect() {
        Set<String> set = new LinkedHashSet<>();
        try {
            Document doc = Jsoup.connect(url).timeout(40 * 10000)
                    .userAgent("Mozilla / 5.0 (Windows NT 10.0; Win64; x64) AppleWebKit / " +
                            "537.36 (KHTML, как Gecko) Chrome / 122.0.0.0 YaBrowser / 24.4.0.0 Safari /537.36")
                    .referrer("http://www.google.com")
                    .get();
            Elements links = doc.select("a[href]");
            for (Element element : links) {
                String link = element.absUrl("href").replaceAll("/$", "");
                if (!resultSet.add(link) || !link.endsWith(".png") ||
                        !link.endsWith(".jpg") || !link.endsWith("#")) {
                    set.add(link);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return set;
    }

    @Override
    protected Set<String> compute() {
        Set<SiteCrawler> tasks = new HashSet<>();
        try {
            Thread.sleep(1500);
            for (String link : connect()) {

                if (!link.trim().equals(url)) {
                    resultSet.add(link);
                } else {
                    resultSet.add(link);
                    SiteCrawler p = new SiteCrawler(link.trim());
                    p.fork();
                    tasks.add(p);
                }
                if (link.trim().endsWith(".pdf")) {
                    resultSet.remove(link);
                }
                if (link.endsWith("#")) {
                    resultSet.remove(link);
                }
                if (!link.startsWith("http")) {
                    resultSet.remove(link);
                }
                if (!link.contains(url)) {
                    resultSet.remove(link);
                }

            }
            for (SiteCrawler parser : tasks) {
                resultSet.addAll(parser.join());
            }


        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return resultSet;
    }

    @Override
    public String toString() {
        return "Parser + " + " " + url;
    }
}

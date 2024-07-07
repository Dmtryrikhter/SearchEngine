package searchengine.services;

import searchengine.dto.search.ResponseSearch;

public interface SearchService {
    ResponseSearch search(String query,  String site,
                          String offset,  String limit);
}

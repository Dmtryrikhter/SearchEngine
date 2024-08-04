package searchengine.services;

import searchengine.dto.indexing.ResponseIndexing;

import java.io.IOException;

public interface IndexingService {
    ResponseIndexing startIndexing();
    ResponseIndexing stopIndexing();
    ResponseIndexing pageIndexing(String url) throws IOException;
}

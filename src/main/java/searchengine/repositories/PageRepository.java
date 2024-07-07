package searchengine.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Page;
import searchengine.model.SiteEntity;

import java.util.List;
import java.util.Optional;


public interface PageRepository extends JpaRepository<Page, Integer> {
    List<Optional<Page>> findAllPageByPath(String path);
    List<Optional<Page>> findAllBySiteId(Integer siteId);
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM page p WHERE p.path = path;")
    void deletePageByPath(@Param("path") String path);
}

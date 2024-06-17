package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.SiteEntity;

import java.util.List;
import java.util.Optional;

public interface SiteRepository extends JpaRepository<SiteEntity, Integer> {
    List<Optional<SiteEntity>> findAllSiteByUrl(String url);
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM site s WHERE s.name = name;")
    void deleteSiteByName(@Param("name") String name);
}

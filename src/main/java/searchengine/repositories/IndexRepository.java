package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Index;
import searchengine.model.Lemma;

import java.util.List;

public interface IndexRepository extends JpaRepository<Index, Integer> {

    List<Index> findIndexByPageId(Integer pageId);
    List<Index> findAllByLemmaId(Integer lemmaId);
}

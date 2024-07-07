package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Lemma;

import java.util.List;
import java.util.Optional;

public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
    Lemma findLemmaByLemma(String name);
    List<Optional<Lemma>> findAllLemmaBySiteId(Integer siteId);

}

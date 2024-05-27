package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "`index`")
public class Index {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @Column(name = "`page_id`", columnDefinition = "INT", nullable = false)
    private int pageId;
    @Column(name = "`lemma_id`", columnDefinition = "INT", nullable = false)
    private int lemmaId;
    @Column(name = "`rank`", columnDefinition = "FLOAT", nullable = false)
    private float rank;
}

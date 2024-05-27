package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.persistence.Index;

@Getter
@Setter
@Entity
@Table(name = "page", indexes = {
        @Index(name = "page_index", columnList = "path", unique = true)
})
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "INT")
    private int id;
    @Column(name = "site_id", columnDefinition = "INT", nullable = false)
    private int siteId;
    @Column(columnDefinition = "TEXT", nullable = false)
    private String path;
    @Column(columnDefinition = "INT", nullable = false)
    private int code;
    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;
}

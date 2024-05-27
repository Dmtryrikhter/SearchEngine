package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "`site`")
public class SiteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "`id`", nullable = false)
    private int id;
    @Column(columnDefinition = "ENUM('INDEXING', 'INDEXED','FAILED')", nullable = false)
    private String status;
    @Column(name = "`status_time`", columnDefinition = "DATETIME", nullable = false)
    private LocalDateTime statusTime;
    @Column(name = "`lust_error`", columnDefinition = "TEXT")
    private String lustError;
    @Column(name = "`url`", columnDefinition = "VARCHAR(255)", nullable = false)
    private String url;
    @Column(name = "`name`", columnDefinition = "VARCHAR(255)", nullable = false)
    private String name;

}

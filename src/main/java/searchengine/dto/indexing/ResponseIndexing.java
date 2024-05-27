package searchengine.dto.indexing;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResponseIndexing {
    private boolean result;
    private String message;
}

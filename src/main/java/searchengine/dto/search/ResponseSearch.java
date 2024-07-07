package searchengine.dto.search;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResponseSearch {
    private int count;
    private DataDTO[] data;
}

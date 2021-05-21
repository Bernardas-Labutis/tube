package lt.vu.tube.requests;

import lombok.*;

import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class RenameRequest {
    private UUID id;
    private Integer version;
    private String newName;
}

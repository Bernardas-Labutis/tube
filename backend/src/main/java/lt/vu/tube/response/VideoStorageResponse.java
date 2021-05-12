package lt.vu.tube.response;

import java.io.Serializable;

public class VideoStorageResponse implements Serializable {
    private static final long serialVersionUID = 2695842416789153178L;
    private Long usedStorageBytes;
    private Long maxStorageBytes;

    public VideoStorageResponse(){}

    public VideoStorageResponse(Long usedStorageBytes, Long maxStorageBytes) {
        this.usedStorageBytes = usedStorageBytes;
        this.maxStorageBytes = maxStorageBytes;
    }

    public Long getUsedStorageBytes() {
        return usedStorageBytes;
    }

    public Long getMaxStorageBytes() {
        return maxStorageBytes;
    }
}

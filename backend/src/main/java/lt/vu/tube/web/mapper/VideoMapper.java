package lt.vu.tube.web.mapper;

import lt.vu.tube.dto.VideoDTO;
import lt.vu.tube.entity.Video;

public class VideoMapper {
    public static VideoDTO mapToVideoDto(Video video) {
        if (video == null) {
            return null;
        }
        return new VideoDTO(
                video.getId().toString(),
                video.getId().toString(),
                video.getFileName(),
                video.getCreated(),
                video.getFileSize(),
                video.getPublic(),
                video.getOwner().getUsername(),
                video.getVersion());
    }
}

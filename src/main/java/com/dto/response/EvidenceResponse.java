package com.dto.response;
import java.time.LocalDateTime;


import com.entity.Evidence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class EvidenceResponse {
    private Long id;
    private String originalFileName;
    private String fileType;
    private Long fileSize;
    private Evidence.EvidenceType evidenceType;
    private String description;
    private String uploadedBy;
    private boolean verified;
    private LocalDateTime uploadedAt;
    private String downloadUrl;
}

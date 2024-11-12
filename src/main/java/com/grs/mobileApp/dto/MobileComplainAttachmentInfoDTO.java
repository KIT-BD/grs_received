package com.grs.mobileApp.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MobileComplainAttachmentInfoDTO {
    private Long id;
    private Long complaintId;
    private String filePath;
    private String fileType;
    private String fileTitle;
    private String fileOriginalName;
    private Long createdBy;
    private Long modifiedBy;
    private String createdAt;
    private String updatedAt;
    private String status;
}

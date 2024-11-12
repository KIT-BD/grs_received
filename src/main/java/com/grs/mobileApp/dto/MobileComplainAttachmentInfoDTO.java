package com.grs.mobileApp.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MobileComplainAttachmentInfoDTO {
    private Long id;
    private Long complaint_Id;
    private String file_path;
    private String file_type;
    private String file_title;
    private String file_original_name;
    private Long created_by;
    private Long modified_by;
    private String created_at;
    private String updated_at;
    private String status;
}

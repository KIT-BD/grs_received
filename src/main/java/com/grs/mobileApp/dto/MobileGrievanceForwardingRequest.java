package com.grs.mobileApp.dto;

import com.grs.api.model.request.FileDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@Builder
public class MobileGrievanceForwardingRequest {
    private Long complaint_id;
    private Long office_id;
    private String username;
    private String note;
    private String deadline;
    private List<MobileOfficerDTO> officers;
    private List<FileDTO> files;
    private List<String> file_name_by_user;
}
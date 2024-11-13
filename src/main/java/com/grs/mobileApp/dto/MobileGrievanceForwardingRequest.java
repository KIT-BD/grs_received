package com.grs.mobileApp.dto;

import com.grs.api.model.request.FileDTO;
import lombok.*;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MobileGrievanceForwardingRequest {
    private Long complaint_id;
    private Long office_id;
    private String username;
    private String note;
    private String deadline;
    private String officers;
    private List<FileDTO> files;
    private String file_name_by_user;
    private String other_service;
    private Long service_id;
}
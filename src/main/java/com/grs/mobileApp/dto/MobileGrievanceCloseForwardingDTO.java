package com.grs.mobileApp.dto;

import com.grs.api.model.request.FileDTO;
import com.grs.core.domain.GrievanceCurrentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MobileGrievanceCloseForwardingDTO {


    private long complaint_id;
    private long office_id;
    private long username;
    private long to_employee_record_id;
    private String action;
    private String closingNoteGRODecision;
    private String closingNoteMainReason;
    private String closingNoteSuggestion;
    private List<Long> deptAction;
    private String departmentalActionReason;
    private List<FileDTO> files;
    private String fileNameByUser;


}

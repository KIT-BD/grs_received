package com.grs.mobileApp.dto;

import com.grs.api.model.request.FileDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MobileInvestigationReportForwardingDTO {

    private Long complaint_id;
    private String note;
    private List<FileDTO> file;
}

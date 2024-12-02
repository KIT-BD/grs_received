package com.grs.mobileApp.dto;

import com.grs.api.model.request.FileDTO;
import lombok.*;


import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class MobileOpinionForwardingDTO {

    private Long complaint_id;
    private Long office_id;
    private String note;
    private List<FileDTO> files;
    private String fileNameByUser;
    private String deadLine;


}

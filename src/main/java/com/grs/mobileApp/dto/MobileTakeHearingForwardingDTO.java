package com.grs.mobileApp.dto;

import com.grs.api.model.request.FileDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MobileTakeHearingForwardingDTO {
    private Long grievanceId;
    private String note;
    private List<FileDTO> files;
}

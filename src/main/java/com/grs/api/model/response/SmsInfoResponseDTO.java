package com.grs.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by HP on 2/7/2018.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SmsInfoResponseDTO {
    private String smsID;
    private String msisdn;
}

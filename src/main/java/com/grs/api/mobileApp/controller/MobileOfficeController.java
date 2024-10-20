package com.grs.api.mobileApp.controller;

import com.grs.api.mobileApp.dto.MobileCustomOfficeLayerDTO;
import com.grs.api.mobileApp.dto.MobileOfficeDTO;
import com.grs.api.mobileApp.dto.MobileOfficeLayerDTO;
import com.grs.api.mobileApp.service.MobileOfficeService;
import com.grs.core.domain.projapoti.Office;
import com.grs.core.domain.projapoti.OfficeLayer;
import com.grs.core.service.OfficeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mobile/offices")
public class MobileOfficeController {

    @Autowired
    private MobileOfficeService mobileOfficeService;

    @Autowired
    private OfficeService officeService;

    @GetMapping("/officelayers")
    public ResponseEntity<Map<String, Object>> getOfficeLayers() {
        List<MobileOfficeLayerDTO> officeLayers = mobileOfficeService.getOfficeLayers();
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", officeLayers);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{layer_level}")
    public ResponseEntity<Map<String, Object>> getMobileOfficeLayers(
            @PathVariable("layer_level") Integer layerLevel) {

        List<Office> offices = officeService.getOfficesByLayerLevel(layerLevel, true, false);
        Map<Integer, MobileOfficeDTO> mobileOffices = mobileOfficeService.convertToMobileOfficeDto(offices);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", mobileOffices);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/layer-level/{layer_level}/custom-layers")
    public ResponseEntity<Map<String, Object>> getCustomOfficeLayersForMobile(
            @PathVariable("layer_level") Integer layerLevel) {

        List<MobileCustomOfficeLayerDTO> mobileOfficeLayers = mobileOfficeService.getCustomOfficeLayersForMobileByLayerLevel(layerLevel);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", mobileOfficeLayers);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/layer-level/{layer_level}/custom-layers/{custom_layer_id}/offices")
    public ResponseEntity<Map<String, Object>> getOfficesByCustomLayerIdForMobile(
            @PathVariable("layer_level") Integer layerLevel,
            @PathVariable("custom_layer_id") Integer customLayerId) {

        List<OfficeLayer> officeLayerList = officeService.getOfficeLayersByLayerLevelAndCustomLayerId(layerLevel, customLayerId);
        List<Office> offices = officeService.getOfficesByOfficeLayer(officeLayerList, true);

        Map<Integer, MobileOfficeDTO> mobileOffices = mobileOfficeService.convertToMobileOfficeDto(offices);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", mobileOffices);

        return ResponseEntity.ok(response);
    }

}

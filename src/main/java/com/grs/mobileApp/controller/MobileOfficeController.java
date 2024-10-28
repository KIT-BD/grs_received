package com.grs.mobileApp.controller;

import com.grs.mobileApp.dto.MobileCustomOfficeLayerDTO;
import com.grs.mobileApp.dto.MobileOfficeDTO;
import com.grs.mobileApp.dto.MobileOfficeLayerDTO;
import com.grs.mobileApp.dto.MobileOfficeOriginDTO;
import com.grs.mobileApp.service.MobileOfficeService;
import com.grs.core.domain.projapoti.Office;
import com.grs.core.service.OfficeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/doptor/api")
public class MobileOfficeController {

    @Autowired
    private MobileOfficeService mobileOfficeService;

    @Autowired
    private OfficeService officeService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> handleApiRequests(
            @RequestParam("api_url") String apiUrl,
            @RequestParam(value = "layer_levels", required = false) String layerLevelsParam,
            @RequestParam(value = "custom_layer_ids", required = false) String customLayerIdsParam,
            @RequestParam(value = "office_origin_ids", required = false) String officeOriginIdsParam
    ) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");

        try {
            switch (apiUrl) {
                case "custom-layer-level":
                    if (layerLevelsParam != null) {
                        // Handle /api/doptor/api?api_url=custom-layer-level&layer_levels=3
                        Integer layerLevel = Integer.parseInt(layerLevelsParam);
                        List<MobileCustomOfficeLayerDTO> mobileOfficeLayers = mobileOfficeService.getCustomOfficeLayersForMobileByLayerLevel(layerLevel);
                        response.put("data", mobileOfficeLayers);
                    } else {
                        // Handle /api/doptor/api?api_url=custom-layer-level
                        List<MobileOfficeLayerDTO> officeLayers = mobileOfficeService.getOfficeLayers();
                        response.put("data", officeLayers);
                    }
                    break;

                case "offices":
                    if (layerLevelsParam != null) {
                        // Handle /api/doptor/api?api_url=offices&layer_levels=1
                        Integer layerLevel = Integer.parseInt(layerLevelsParam);
                        List<Office> offices = officeService.getOfficesByLayerLevel(layerLevel, true, false);
                        Map<Integer, MobileOfficeDTO> mobileOffices = mobileOfficeService.convertToMobileOfficeDto(offices);
                        response.put("data", mobileOffices);
                    } else if (customLayerIdsParam != null) {
                        // Handle /api/doptor/api?api_url=offices&custom_layer_ids=40
                        Long customLayerId = Long.parseLong(customLayerIdsParam);
                        List<Office> offices = mobileOfficeService.getOfficesByCustomLayerId(customLayerId);
                        Map<Integer, MobileOfficeDTO> mobileOffices = mobileOfficeService.convertToMobileOfficeDto(offices);
                        response.put("data", mobileOffices);
                    } else if (officeOriginIdsParam != null) {
                        // Handle /api/doptor/api?api_url=offices&office_origin_ids=38
                        Long officeOriginId = Long.parseLong(officeOriginIdsParam);
                        Map<Integer, MobileOfficeDTO> mobileOffices = mobileOfficeService.findByOfficeOriginIdForMobile(officeOriginId);
                        response.put("data", mobileOffices);
                    } else {
                        response.put("status", "error");
                        response.put("message", "Missing required parameter for 'offices'");
                        return ResponseEntity.badRequest().body(response);
                    }
                    break;

                case "office-origins":
                    if (layerLevelsParam != null) {
                        // Handle /api/doptor/api?api_url=office-origins&layer_levels=4
                        Integer layerLevel = Integer.parseInt(layerLevelsParam);
                        List<MobileOfficeOriginDTO> mobileOfficeOrigins = mobileOfficeService.getOfficeOriginsForMobile(layerLevel);
                        response.put("data", mobileOfficeOrigins);
                    } else {
                        response.put("status", "error");
                        response.put("message", "Missing required parameter 'layer_levels' for 'office-origins'");
                        return ResponseEntity.badRequest().body(response);
                    }
                    break;

                default:
                    response.put("status", "error");
                    response.put("message", "Invalid api_url");
                    return ResponseEntity.badRequest().body(response);
            }
            return ResponseEntity.ok(response);

        } catch (NumberFormatException e) {
            response.put("status", "error");
            response.put("message", "Invalid parameter value");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}










//package com.grs.api.mobileApp.controller;
//
//import com.grs.api.mobileApp.dto.MobileCustomOfficeLayerDTO;
//import com.grs.api.mobileApp.dto.MobileOfficeDTO;
//import com.grs.api.mobileApp.dto.MobileOfficeLayerDTO;
//import com.grs.api.mobileApp.dto.MobileOfficeOriginDTO;
//import com.grs.api.mobileApp.service.MobileOfficeService;
//import com.grs.core.domain.projapoti.Office;
//import com.grs.core.service.OfficeService;
//import lombok.experimental.var;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/doptor/api")
//public class MobileOfficeController {
//
//    @Autowired
//    private MobileOfficeService mobileOfficeService;
//
//    @Autowired
//    private OfficeService officeService;
//
//    @GetMapping
//    public ResponseEntity<?> getOffice(
//            @RequestParam("api_url") String apiUrl,
//            @RequestParam(value = "layer_levels", required = false) Integer layerLevels,
//            @RequestParam(value = "office_origin_ids", required = false) Integer officeOriginIds,
//            @RequestParam(value = "custom_layer_ids", required = false) Integer customLayerIds
//    ){
//        Map<String,Object> response = mobileOfficeService.getResponse(apiUrl, layerLevels, officeOriginIds, customLayerIds);
//        return null;
//    }
//
//    @GetMapping("/office-layers")
//    public ResponseEntity<Map<String, Object>> getOfficeLayers() {
//        List<MobileOfficeLayerDTO> officeLayers = mobileOfficeService.getOfficeLayers();
//        Map<String, Object> response = new HashMap<>();
//        response.put("status", "success");
//        response.put("data", officeLayers);
//        return ResponseEntity.ok(response);
//    }
//
//    @GetMapping("/office-layers/{layer_level}")
//    public ResponseEntity<Map<String, Object>> getMobileOfficeLayers(
//            @PathVariable("layer_level") Integer layerLevel) {
//
//        List<Office> offices = officeService.getOfficesByLayerLevel(layerLevel, true, false);
//        System.out.println(offices);
//        Map<Integer, MobileOfficeDTO> mobileOffices = mobileOfficeService.convertToMobileOfficeDto(offices);
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("status", "success");
//        response.put("data", mobileOffices);
//
//        return ResponseEntity.ok(response);
//    }
//
//
//    @GetMapping("/layer-level/{layer_level}/custom-layers")
//    public ResponseEntity<Map<String, Object>> getCustomOfficeLayersForMobile(
//            @PathVariable("layer_level") Integer layerLevel) {
//
//        List<MobileCustomOfficeLayerDTO> mobileOfficeLayers = mobileOfficeService.getCustomOfficeLayersForMobileByLayerLevel(layerLevel);
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("status", "success");
//        response.put("data", mobileOfficeLayers);
//
//        return ResponseEntity.ok(response);
//    }
//
//    @GetMapping("/layer-level/custom-layers/{custom_layer_id}/offices")
//    public ResponseEntity<Map<String, Object>> getOfficesByCustomLayerIdForMobile(
//            @PathVariable("custom_layer_id") Long customLayerId) {
//
//        List<Office> offices = mobileOfficeService.getOfficesByCustomLayerId(customLayerId);
//
//        Map<Integer, MobileOfficeDTO> mobileOffices = mobileOfficeService.convertToMobileOfficeDto(offices);
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("status", "success");
//        response.put("data", mobileOffices);
//
//        return ResponseEntity.ok(response);
//    }
//
//
//    @GetMapping("/office-origin/{layer_level}")
//    public ResponseEntity<Map<String, Object>> getOfficeOriginsForMobile(
//            @PathVariable("layer_level") Integer layerLevel) {
//
//        List<MobileOfficeOriginDTO> mobileOfficeOrigins = mobileOfficeService.getOfficeOriginsForMobile(layerLevel);
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("status", "success");
//        response.put("data", mobileOfficeOrigins);
//
//        return ResponseEntity.ok(response);
//    }
//
//
//    @GetMapping("/office-origin/{office_origin_id}/offices")
//    public ResponseEntity<Map<String, Object>> getOfficesForMobile(
//            @PathVariable("office_origin_id") Long officeOriginId) {
//
//        Map<Integer, MobileOfficeDTO> mobileOffices = mobileOfficeService.findByOfficeOriginIdForMobile(officeOriginId);
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("status", "success");
//        response.put("data", mobileOffices);
//
//        return ResponseEntity.ok(response);
//    }
//
//
//
//}

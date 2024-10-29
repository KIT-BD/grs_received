package com.grs.mobileApp.controller;

import com.grs.api.model.response.CitizenCharterDTO;
import com.grs.api.model.response.ServiceOriginDTO;
import com.grs.core.dao.CitizenCharterDAO;
import com.grs.core.domain.grs.CitizenCharter;
import com.grs.mobileApp.dto.*;
import com.grs.mobileApp.service.MobileOfficeService;
import com.grs.core.domain.projapoti.Office;
import com.grs.core.service.OfficeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class MobileOfficeController {

    @Autowired
    private MobileOfficeService mobileOfficeService;

    @Autowired
    private OfficeService officeService;

    @Autowired
    private CitizenCharterDAO citizenCharterDAO;

    @GetMapping("/api/doptor/api")
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

    @GetMapping("/api/doptor/office")
    public Map<String, Object> searchOfficeByName(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String nameBn) {
        List<Office> offices = mobileOfficeService.searchOffices(name, nameBn);

        List<Map<String, Object>> searchedOfficesList = new ArrayList<>();

        for (Office office : offices) {
            Map<String, Object> officeInfo = new HashMap<>();
            officeInfo.put("id", office.getId());
            officeInfo.put("nameBn", office.getNameBangla());
            officeInfo.put("name", office.getNameEnglish());
            officeInfo.put("code", "");
            officeInfo.put("division", office.getDivisionId());
            officeInfo.put("district", office.getDistrictId());
            officeInfo.put("upazila", office.getUpazilaId());
            officeInfo.put("phone", "");
            officeInfo.put("mobile", "");
            officeInfo.put("digitalNothiCode", "");
            officeInfo.put("fax", "");
            officeInfo.put("email", "");
            officeInfo.put("website", office.getWebsiteUrl());
            officeInfo.put("ministry", office.getOfficeMinistry().getId());
            officeInfo.put("layer", office.getOfficeLayer().getId());
            officeInfo.put("origin", office.getOfficeOriginId());
            officeInfo.put("customLayer", office.getOfficeLayer().getCustomLayerId());
            officeInfo.put("parentOfficeId", office.getParentOfficeId());

            searchedOfficesList.add(officeInfo);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", searchedOfficesList);

        return response;
    }

    @RequestMapping(value = "/api/service/list", method = RequestMethod.GET)
    public ResponseEntity<?> getServiceList(@RequestParam("office_id") Long officeId) {
        List<ServiceOriginDTO> services = this.officeService.getServices(officeId);

        List<MobileServiceListDTO> data = services.stream()
                .map(mobileOfficeService::mapToMobileServiceListDTO)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/api/citizen-charter/infoNew", method = RequestMethod.GET)
    public ResponseEntity<?> getCitizenCharterInfoNew(@RequestParam("office_id") Long officeId) {

        // Fetch visions data
        MobileVisionDTO vision = mobileOfficeService.getVisionByOfficeId(officeId);

        List<CitizenCharter> citizenCharters = citizenCharterDAO.findByOffice(officeId);
//        System.out.println("CitizenCharters: " + citizenCharters.get(0));

        // Map citizen charter details
        List<MobileCitizenCharterDetailsInfoDTO> listCitizenCharters = citizenCharters.stream()
                .map(mobileOfficeService::mapToCitizenCharterDetailsInfoDTO)
                .collect(Collectors.toList());

        // Prepare CitizenCharterDetailsInfo response
        Map<String, Object> citizenCharterDetailsInfo = new HashMap<>();
        citizenCharterDetailsInfo.put("status", "success");
        citizenCharterDetailsInfo.put("data", listCitizenCharters);

        // Prepare response
        Map<String, Object> data = new HashMap<>();
        data.put("visions", vision);
        data.put("CitizenCharterDetailsInfo", citizenCharterDetailsInfo);


        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }
}

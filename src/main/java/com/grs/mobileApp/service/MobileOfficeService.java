package com.grs.mobileApp.service;

import com.grs.api.model.response.CitizenCharterDTO;
import com.grs.api.model.response.ServiceOriginDTO;
import com.grs.api.model.response.organogram.TreeNodeOfficerDTO;
import com.grs.core.dao.CitizenCharterOriginDAO;
import com.grs.core.dao.OfficeDAO;
import com.grs.core.domain.grs.CitizenCharter;
import com.grs.core.domain.grs.CitizensCharterOrigin;
import com.grs.core.domain.projapoti.CustomOfficeLayer;
import com.grs.core.domain.projapoti.Office;
import com.grs.core.domain.projapoti.OfficeLayer;
import com.grs.core.domain.projapoti.OfficeOrigin;
import com.grs.core.repo.projapoti.CustomOfficeLayerRepo;
import com.grs.core.repo.projapoti.OfficeLayerRepo;
import com.grs.core.repo.projapoti.OfficeRepo;
import com.grs.core.service.OfficeOrganogramService;
import com.grs.core.service.OfficeService;
import com.grs.mobileApp.dto.*;
import com.grs.utils.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MobileOfficeService {

    @Autowired
    private OfficeService officeService;

    @Autowired
    private OfficeLayerRepo officeLayerRepo;

    @Autowired
    private CustomOfficeLayerRepo customOfficeLayerRepo;

    @Autowired
    private OfficeDAO officeDAO;

    @Autowired
    private OfficeRepo officeRepo;

    @Autowired
    private CitizenCharterOriginDAO citizenCharterOriginDAO;

    @Autowired
    private OfficeOrganogramService officeOrganogramService;


    public List<MobileOfficeLayerDTO> getOfficeLayers() {
        List<MobileOfficeLayerDTO> officeLayers = new ArrayList<>();

        officeLayers.add(new MobileOfficeLayerDTO(1, "মন্ত্রণালয়/বিভাগ", "Ministry/Division"));
        officeLayers.add(new MobileOfficeLayerDTO(2, "অধিদপ্তর/পরিদপ্তর", "Directorate"));
        officeLayers.add(new MobileOfficeLayerDTO(3, "অন্যান্য দপ্তর/সংস্থা", "Other Offices/Organizations"));
        officeLayers.add(new MobileOfficeLayerDTO(4, "বিভাগীয় পর্যায়ের কার্যালয়", "Divisional Office"));
        officeLayers.add(new MobileOfficeLayerDTO(7, "আঞ্চলিক কার্যালয়", "Regional Office"));
        officeLayers.add(new MobileOfficeLayerDTO(5, "জেলা পর্যায়ের কার্যালয়", "District Office"));
        officeLayers.add(new MobileOfficeLayerDTO(6, "উপজেলা পর্যায়ের কার্যালয়", "Upazilla Office"));
        officeLayers.add(new MobileOfficeLayerDTO(0, "সেল", "Cell"));

        return officeLayers;
    }

    public Map<Integer, MobileOfficeDTO> convertToMobileOfficeDto(List<Office> offices) {
        Map<Integer, MobileOfficeDTO> response = new HashMap<>();

        for (Office office : offices) {
            MobileOfficeDTO mobileOffice = new MobileOfficeDTO();

            mobileOffice.setId(office.getId() != null ? office.getId() : null);
            mobileOffice.setOffice_name_bng(office.getNameBangla() != null ? office.getNameBangla() : null);
            mobileOffice.setOffice_name_eng(office.getNameEnglish() != null ? office.getNameEnglish() : null);

            mobileOffice.setGeo_division_id(office.getDivisionId());
            mobileOffice.setGeo_district_id(office.getDistrictId());
            mobileOffice.setGeo_upazila_id(office.getUpazilaId());

            mobileOffice.setDigital_nothi_code("");
            mobileOffice.setOffice_phone("");
            mobileOffice.setOffice_mobile("");
            mobileOffice.setOffice_fax("");
            mobileOffice.setOffice_email("");
            mobileOffice.setOffice_web(office.getWebsiteUrl() != null ? office.getWebsiteUrl() : null);

            mobileOffice.setOffice_ministry_id(office.getOfficeMinistry() != null ? office.getOfficeMinistry().getId() : null);
            mobileOffice.setOffice_layer_id(office.getOfficeLayer() != null ? office.getOfficeLayer().getId() : null);
            mobileOffice.setOffice_origin_id(office.getOfficeOriginId() != null ? office.getOfficeOriginId() : null);
            mobileOffice.setCustom_layer_id(office.getOfficeLayer() != null ? office.getOfficeLayer().getCustomLayerId() : null);
            mobileOffice.setParent_office_id(office.getParentOfficeId() != null ? office.getParentOfficeId() : null);

            MobileOfficeLayerDuplicateDTO officeLayer = new MobileOfficeLayerDuplicateDTO();
            officeLayer.setId(office.getOfficeLayer() != null ? office.getOfficeLayer().getId() : null);
            officeLayer.setLayer_name_eng(office.getOfficeLayer() != null ? office.getOfficeLayer().getLayerNameEnglish() : null);
            officeLayer.setLayer_name_bng(office.getOfficeLayer() != null ? office.getOfficeLayer().getLayerNameBangla() : null);
            officeLayer.setLayer_level(office.getOfficeLayer() != null ? office.getOfficeLayer().getLayerLevel() : null);
            mobileOffice.setOffice_layer(officeLayer);

            response.put(mobileOffice.getId().intValue(), mobileOffice);
        }
        return response;
    }


    public List<MobileCustomOfficeLayerDTO> getCustomOfficeLayersForMobileByLayerLevel(Integer layerLevel) {
        List<CustomOfficeLayer> customOfficeLayers = officeService.getCustomOfficeLayersByLayerLevel(layerLevel);

        List<MobileCustomOfficeLayerDTO> mobileOfficeLayerDtos = new ArrayList<>();

        for (CustomOfficeLayer layer : customOfficeLayers) {
            String nameEn = MessageUtils.getCustomLayerEnglishName(layer.getName());
            mobileOfficeLayerDtos.add(new MobileCustomOfficeLayerDTO(layer.getId(), layer.getName(), layer.getLayerLevel(), nameEn));
        }

        return mobileOfficeLayerDtos;
    }

    public List<Office> getOfficesByCustomLayerId(Long customLayerId) {
        CustomOfficeLayer customOfficeLayer = customOfficeLayerRepo.findById(customLayerId);

        List<OfficeLayer> officeLayerList = officeLayerRepo.findByCustomLayerId(customOfficeLayer.getId().intValue());

        return officeService.getOfficesByOfficeLayer(officeLayerList, true);
    }


    public List<MobileOfficeOriginDTO> getOfficeOriginsForMobile(Integer layerLevel) {
        List<OfficeOrigin> officeOrigins = officeService.getOfficeOriginsByLayerLevel(layerLevel, true, false);

        List<MobileOfficeOriginDTO> mobileOfficeOrigins = new ArrayList<>();

        for (OfficeOrigin origin : officeOrigins) {
            MobileOfficeOriginDTO mobileOrigin = new MobileOfficeOriginDTO();
            mobileOrigin.setId(origin.getId());
            mobileOrigin.setOffice_name_bng(origin.getOfficeNameBangla());
            mobileOrigin.setOffice_name_eng(origin.getOfficeNameEnglish());
            mobileOrigin.setOffice_ministry_id(null);
            mobileOrigin.setOffice_layer_id(origin.getOfficeLayerId());
            mobileOrigin.setParent_office_id(origin.getParentOfficeOriginId());
            mobileOrigin.setOffice_level(null);
            mobileOrigin.setOffice_sequence(null);

            MobileOfficeLayerDuplicateDTO officeLayerDto = new MobileOfficeLayerDuplicateDTO();
            OfficeLayer officeLayer = officeLayerRepo.findOne(origin.getOfficeLayerId());
            if (officeLayer != null) {
                officeLayerDto.setId(officeLayer.getId());
                officeLayerDto.setLayer_name_eng(officeLayer.getLayerNameEnglish());
                officeLayerDto.setLayer_name_bng(officeLayer.getLayerNameBangla());
                officeLayerDto.setLayer_level(officeLayer.getLayerLevel());
                mobileOrigin.setOffice_layer(officeLayerDto);
            }

            mobileOfficeOrigins.add(mobileOrigin);
        }

        return mobileOfficeOrigins;
    }

    public Map<Integer, MobileOfficeDTO> findByOfficeOriginIdForMobile(Long officeOriginId) {

        List<Office> offices = this.officeDAO.findByOfficeOriginId(officeOriginId);

        return this.convertToMobileOfficeDto(offices);
    }

    public List<Office> searchOffices(String name, String nameBn) {
        if (name != null) {
            return officeRepo.findByOfficeNameEng(name);
        }
        else {
            return officeRepo.findByOfficeNameBng(nameBn);
        }
    }

    public MobileServiceListDTO mapToMobileServiceListDTO(ServiceOriginDTO serviceOriginDTO) {
        MobileServiceListDTO dto = new MobileServiceListDTO();
        dto.setId(serviceOriginDTO.getServiceId());
        dto.setOffice_origin_id(serviceOriginDTO.getOfficeOriginId());
        dto.setOffice_origin_unit_id(serviceOriginDTO.getOfficeOriginUnitId());
        dto.setOffice_origin_unit_organogram_id(serviceOriginDTO.getOfficeOriginUnitOrganogramId());
        dto.setOffice_origin_name_bng(serviceOriginDTO.getOfficeOriginName());
        dto.setOffice_origin_name_eng(null); // Assuming English name is not available
        dto.setService_type(serviceOriginDTO.getServiceType() != null ? serviceOriginDTO.getServiceType().name() : null);
        dto.setService_name_bng(serviceOriginDTO.getServiceNameBangla());
        dto.setService_name_eng(serviceOriginDTO.getServiceNameEnglish());
        dto.setService_procedure_bng(serviceOriginDTO.getServiceProcedureBangla());
        dto.setService_procedure_eng(serviceOriginDTO.getServiceProcedureEnglish());
        dto.setDocuments_and_location_bng(serviceOriginDTO.getDocumentAndLocationBangla());
        dto.setDocuments_and_location_eng(serviceOriginDTO.getDocumentAndLocationEnglish());
        dto.setPayment_method_bng(serviceOriginDTO.getPaymentMethodBangla());
        dto.setPayment_method_eng(serviceOriginDTO.getPaymentMethodEnglish());
        dto.setService_time(serviceOriginDTO.getServiceTime());
        dto.setStatus(serviceOriginDTO.getStatus() != null && serviceOriginDTO.getStatus() ? 1 : 0);
        dto.setCreated_by(null); // Assuming created_by is not available
        dto.setModified_by(null); // Assuming modified_by is not available
        dto.setCreated_at(null); // Assuming created_at is not available
        dto.setModified_at(null); // Assuming modified_at is not available
        return dto;
    }

    public MobileCitizenCharterDetailsInfoDTO mapToCitizenCharterDetailsInfoDTO(CitizenCharter dto) {
        MobileCitizenCharterDetailsInfoDTO infoDTO = new MobileCitizenCharterDetailsInfoDTO();
        infoDTO.setId(dto.getId());
        infoDTO.setOffice_id(dto.getOfficeId());
        infoDTO.setOffice_origin_id(dto.getOfficeOriginId());
        infoDTO.setService_id(dto.getServiceOrigin().getId());
        infoDTO.setSo_office_id(dto.getSoOfficeId());
        infoDTO.setSo_office_unit_id(dto.getSoOfficeUnitId());
        infoDTO.setSo_office_unit_organogram_id(dto.getSoOfficeUnitOrganogramId());
        infoDTO.setService_name_bng(dto.getServiceNameBangla());
        infoDTO.setService_name_eng(dto.getServiceNameEnglish());
        infoDTO.setService_procedure_bng(dto.getServiceProcedureBangla());
        infoDTO.setService_procedure_eng(dto.getServiceProcedureEnglish());
        infoDTO.setDocuments_and_location_bng(dto.getDocumentAndLocationBangla());
        infoDTO.setDocuments_and_location_eng(dto.getDocumentAndLocationEnglish());
        infoDTO.setPayment_method_bng(dto.getPaymentMethodBangla());
        infoDTO.setPayment_method_eng(dto.getPaymentMethodEnglish());
        infoDTO.setService_time(dto.getServiceTime());
        infoDTO.setService_type(dto.getServiceOrigin() != null ? dto.getServiceOrigin().getServiceType().name() : null);
        infoDTO.setIs_disabled_for_admin(null); // Assuming not available
        infoDTO.setStatus(dto.getStatus() != null && dto.getStatus() ? 1 : 0);
        infoDTO.setOrigin_status(dto.getOriginStatus() != null && dto.getOriginStatus() ? 1 : 0);
        infoDTO.setCreated_at(null); // Assuming not available
        infoDTO.setModified_at(null); // Assuming not available
        infoDTO.setCreated_by(null); // Assuming not available
        infoDTO.setModified_by(null); // Assuming not available
        return infoDTO;
    }

    public MobileVisionDTO getVisionByOfficeId(Long officeId) {

        CitizensCharterOrigin origin = citizenCharterOriginDAO.findByOfficeOriginId(officeService.getOffice(officeId).getOfficeOriginId());

        if (origin != null) {
            MobileVisionDTO visionDTO = new MobileVisionDTO();
            visionDTO.setId(origin.getId());
            visionDTO.setOffice_origin_id(origin.getOfficeOriginId());
            visionDTO.setOffice_origin_name_bng(origin.getOfficeOriginNameBangla());
            visionDTO.setOffice_origin_name_eng(origin.getOfficeOriginNameEnglish());
            visionDTO.setLayer_level(origin.getLayerLevel() != null ? origin.getLayerLevel().intValue() : null);
            visionDTO.setVision_bng(origin.getVisionBangla());
            visionDTO.setVision_eng(origin.getVisionEnglish());
            visionDTO.setMission_bng(origin.getMissionBangla());
            visionDTO.setMission_eng(origin.getMissionEnglish());
            visionDTO.setExpectations_bng(origin.getExpectationBangla());
            visionDTO.setExpectations_eng(origin.getExpectationEnglish());
            return visionDTO;
        } else {
            return null;
        }
    }

    public MobileResponse getOfficeUnitDesignationEmployeeMap(Authentication authentication, Long officeId) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return MobileResponse.builder()
                    .data("Please login.")
                    .status("error")
                    .build();
        }

        Office office = officeRepo.findOfficeById(officeId);
        String rootNodeId = "units_" + office.getOfficeMinistry().getId() + "_" + office.getId() + "_" + office.getParentOfficeId() + "_root";

        List<TreeNodeOfficerDTO> treeRootNodeOfficerDTOList = officeOrganogramService.getSOOrganogram(rootNodeId, authentication);

        List<Map<String, Object>> formattedData = new ArrayList<>();

        for (TreeNodeOfficerDTO rootNode : treeRootNodeOfficerDTOList) {
            String postNodeId = rootNode.getId().replace("_root", "_post");

            List<TreeNodeOfficerDTO> postNodeOfficerDTOList = officeOrganogramService.getSOOrganogram(postNodeId, authentication);

            List<Map<String, Object>> postNodes = new ArrayList<>();
            for (TreeNodeOfficerDTO officer : postNodeOfficerDTOList) {
                Map<String, Object> postNodeData = new HashMap<>();
                postNodeData.put("id", Long.valueOf(officer.getId().split("_")[3]));
                postNodeData.put("office_id", officeId);
                postNodeData.put("office_name_bng", office.getNameBangla());
                postNodeData.put("office_unit_organogram_id", Long.valueOf(officer.getId().split("_")[3]));
                postNodeData.put("office_unit_id", Long.valueOf(rootNode.getId().split("_")[3]));
                postNodeData.put("employee_record_id", null);
                postNodeData.put("unit_name_bng", rootNode.getText().replaceAll("<[^>]*>", ""));
                postNodeData.put("label", String.format("%s, %s", officer.getDesignation(), officer.getName()));
                postNodeData.put("designation", officer.getDesignation());
                postNodeData.put("name", officer.getName());
                postNodeData.put("name_en", "");
                postNodes.add(postNodeData);
            }

            // Format root node data
            Map<String, Object> rootNodeData = new HashMap<>();
            rootNodeData.put("id", Long.valueOf(rootNode.getId().split("_")[3]));
            rootNodeData.put("label", rootNode.getText().replaceAll("<[^>]*>", ""));
            rootNodeData.put("nodes", postNodes);

            formattedData.add(rootNodeData);
        }

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("id", officeId);
        responseData.put("label", "শাখাসমূহ");
        responseData.put("nodes", formattedData);

        return MobileResponse.builder()
                .data(Collections.singletonList(responseData))
                .status("success")
                .build();
    }


}

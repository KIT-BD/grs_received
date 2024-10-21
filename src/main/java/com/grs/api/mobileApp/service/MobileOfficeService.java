package com.grs.api.mobileApp.service;

import com.grs.api.mobileApp.dto.*;
import com.grs.core.dao.OfficeDAO;
import com.grs.core.domain.projapoti.CustomOfficeLayer;
import com.grs.core.domain.projapoti.Office;
import com.grs.core.domain.projapoti.OfficeLayer;
import com.grs.core.domain.projapoti.OfficeOrigin;
import com.grs.core.repo.projapoti.CustomOfficeLayerRepo;
import com.grs.core.repo.projapoti.OfficeLayerRepo;
import com.grs.core.service.OfficeService;
import com.grs.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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

}

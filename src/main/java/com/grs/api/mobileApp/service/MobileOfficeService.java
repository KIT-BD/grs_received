package com.grs.api.mobileApp.service;

import com.grs.api.mobileApp.dto.MobileCustomOfficeLayerDTO;
import com.grs.api.mobileApp.dto.MobileOfficeDTO;
import com.grs.api.mobileApp.dto.MobileOfficeLayerDTO;
import com.grs.api.mobileApp.dto.MobileOfficeLayerDuplicateDTO;
import com.grs.core.domain.projapoti.CustomOfficeLayer;
import com.grs.core.domain.projapoti.Office;
import com.grs.core.service.OfficeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MobileOfficeService {

    private final OfficeService officeService;

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
            mobileOffice.setId(office.getId().intValue());
            mobileOffice.setOffice_name_bng(office.getNameBangla());
            mobileOffice.setOffice_name_eng(office.getNameEnglish());
            mobileOffice.setGeo_division_id(office.getDivisionId());
            mobileOffice.setGeo_district_id(office.getDistrictId() != null ? office.getDistrictId() : 0);
            mobileOffice.setGeo_upazila_id(office.getUpazilaId() != null ? office.getUpazilaId() : 0);
            mobileOffice.setDigital_nothi_code("");
            mobileOffice.setOffice_phone("");
            mobileOffice.setOffice_mobile("");
            mobileOffice.setOffice_fax("");
            mobileOffice.setOffice_email("");
            mobileOffice.setOffice_web(office.getWebsiteUrl());
            mobileOffice.setOffice_ministry_id(office.getOfficeMinistry() != null ? office.getOfficeMinistry().getId().intValue() : 0);
            mobileOffice.setOffice_layer_id(office.getOfficeLayer() != null ? office.getOfficeLayer().getId().intValue() : 0);
            mobileOffice.setOffice_origin_id(office.getOfficeOriginId() != null ? office.getOfficeOriginId().intValue() : 0);
            mobileOffice.setCustom_layer_id(office.getOfficeLayer().getCustomLayerId() != null ? office.getOfficeLayer().getCustomLayerId() : 0);
            mobileOffice.setParent_office_id(office.getParentOfficeId() != null ? office.getParentOfficeId().intValue() : 0);

            // Add office layer details if available
            if (office.getOfficeLayer() != null) {
                MobileOfficeLayerDuplicateDTO officeLayer = new MobileOfficeLayerDuplicateDTO();
                officeLayer.setId(office.getOfficeLayer().getId().intValue());
                officeLayer.setLayer_name_eng(office.getOfficeLayer().getLayerNameEnglish());
                officeLayer.setLayer_name_bng(office.getOfficeLayer().getLayerNameBangla());
                officeLayer.setLayer_level(office.getOfficeLayer().getLayerLevel());

                mobileOffice.setOffice_layer(officeLayer);
            }

            response.put(mobileOffice.getId(), mobileOffice);
        }
        return response;
    }


    public List<MobileCustomOfficeLayerDTO> getCustomOfficeLayersForMobileByLayerLevel(Integer layerLevel) {
        // Fetch the original custom office layers
        List<CustomOfficeLayer> customOfficeLayers = officeService.getCustomOfficeLayersByLayerLevel(layerLevel);

        // Convert to the mobile DTO format
        List<MobileCustomOfficeLayerDTO> mobileOfficeLayerDtos = new ArrayList<>();

        for (CustomOfficeLayer layer : customOfficeLayers) {
            String nameEn = getEnglishName(layer.getName());
            mobileOfficeLayerDtos.add(new MobileCustomOfficeLayerDTO(layer.getId(), layer.getName(), layer.getLayerLevel(), nameEn));
        }

        return mobileOfficeLayerDtos;
    }

    private String getEnglishName(String nameBng) {
        switch (nameBng) {
            case "কর্তৃপক্ষ / অথোরিটি":
                return "Authority";
            case "ইনস্টিটিউট":
                return "Institute";
            case "অন্যান্য প্রতিষ্ঠান":
                return "Other Institutions";
            case "একাডেমি/ প্রশিক্ষণ কেন্দ্র":
                return "Academy / Training Center";
            case "ব্যুরো":
                return "Bureau";
            case "কর্পোরেশন":
                return "Corporation";
            case "কাউন্সিল":
                return "Council";
            case "কমিশন":
                return "Commission";
            case "কোম্পানি":
                return "Company";
            case "পরিদপ্তর":
                return "Directorate";
            case "পরিষদ":
                return "Council";
            case "প্লান্ট/স্টেশন/ফিল্ড":
                return "Plant / Station / Field";
            case "প্রোগ্রাম / প্রকল্প":
                return "Program / Project";
            case "মন্ত্রণালয়":
                return "Ministry";
            case "অধিদপ্তর":
                return "Department";
            case "বিভাগীয় কার্যালয়":
                return "Divisional Office";
            case "জেলা কার্যালয়":
                return "District Office";
            case "উপজেলা কার্যালয়":
                return "Upazila Office";
            case "উপজেলা ভূমি অফিস":
                return "Upazila Land Office";
            case "বিভাগ":
                return "Division";
            case "সংস্থা":
                return "Agency";
            case "ইউনিট":
                return "Unit";
            case "বোর্ড":
                return "Board";
            case "আঞ্চলিক কার্যালয়":
                return "Regional Office";
            case "ফাউন্ডেশন":
                return "Foundation";
            case "বিশ্ববিদ্যালয়":
                return "University";
            case "কলেজ":
                return "College";
            case "স্কুল":
                return "School";
            case "টেকনিক্যাল/ভোকেশনাল প্রতিষ্ঠান":
                return "Technical / Vocational Institution";
            case "জোনাল অফিস":
                return "Zonal Office";
            case "মেট্রোপলিটন":
                return "Metropolitan";
            case "মিশন ও অন্যান্য":
                return "Mission and Others";
            case "সার্কেল অফিস":
                return "Circle Office";
            default:
                return ""; // Fallback for unknown entries
        }
    }



}

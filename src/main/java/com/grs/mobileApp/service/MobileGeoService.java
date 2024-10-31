package com.grs.mobileApp.service;

import com.grs.core.domain.projapoti.District;
import com.grs.core.repo.projapoti.UpazilaRepo;
import com.grs.core.service.GeoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MobileGeoService {

    private final GeoService geoService;
    private final UpazilaRepo upazilaRepo;

    public List<?> findGeo(
            String apiURL,
            Integer param
    ){
        if (apiURL.equals("district")){
            if(param != null){
                return geoService.getDistrictsByDivision(param);
            } else {
                return geoService.getDistricts();
            }
        } else if (apiURL.equals("division")){
            return geoService.getDivisions();
        } else if (apiURL.equals("upazilla")){
            if (param != null){
                return geoService.getUpazilas(param);
            } else {
                return upazilaRepo.findAll();
            }
        }
        return null;
    }
}

package com.grs.mobileApp.service;

import com.grs.core.domain.grs.*;
import com.grs.core.repo.grs.EducationRepo;
import com.grs.core.repo.grs.NationalityAndCountryRepo;
import com.grs.core.repo.grs.OccupationRepo;
import com.grs.core.repo.grs.SpProgrammeRepo;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Data
@Service
@RequiredArgsConstructor
public class MobilePublicAPIService {

    private final OccupationRepo occupationRepo;
    private final EducationRepo educationRepo;
    private final NationalityAndCountryRepo nationalityAndCountryRepo;
    private final SpProgrammeRepo spProgrammeRepo;
    public List<Occupation> getOccupationList(){
        return occupationRepo.findAll();
    }

    public List<Education> getQualificationList(){
        return educationRepo.findAll();
    }

    public List<CountryInfo> getNationalityList(){
        return nationalityAndCountryRepo.findAll();
    }

    public List<SpProgramme> getSpProgrammeList(){
        return spProgrammeRepo.findAll();
    }

}

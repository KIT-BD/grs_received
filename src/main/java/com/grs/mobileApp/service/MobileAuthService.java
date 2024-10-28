package com.grs.mobileApp.service;

import com.grs.core.domain.grs.Complainant;
import com.grs.core.repo.grs.ComplainantRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MobileAuthService {

    private final ComplainantRepo complainantRepo;

    public Complainant findByMobileNumber(String mobileNumber){
        return complainantRepo.findByPhoneNumber(mobileNumber);
    }
}

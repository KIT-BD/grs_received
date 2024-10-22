package com.grs.api.mobileApp.service;

import com.grs.api.model.UserInformation;
import com.grs.api.model.UserType;
import com.grs.api.model.request.GrievanceRequestDTO;
import com.grs.core.dao.CitizenCharterDAO;
import com.grs.core.domain.GrievanceCurrentStatus;
import com.grs.core.domain.MediumOfSubmission;
import com.grs.core.domain.grs.CitizenCharter;
import com.grs.core.domain.grs.Complainant;
import com.grs.core.domain.grs.Grievance;
import com.grs.core.repo.grs.GrievanceRepo;
import com.grs.core.service.ComplainantService;
import com.grs.utils.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MobileGrievanceService {

    private final GrievanceRepo grievanceRepo;
    private final ComplainantService complainantService;
    private final CitizenCharterDAO citizenCharterDAO;

    public List<Grievance> findGrievancesByUser(Long id){
        return grievanceRepo.findGrievancesByComplainantId(id);
    }

    public Grievance submitGrievance(UserInformation userInformation, GrievanceRequestDTO grievanceRequestDTO) {
        boolean isGrsUser = false;
        boolean offlineGrievanceUploaded = false;
        Long uploaderGroOfficeUnitOrganogramId = null;
        Long userId;

        if (userInformation == null) {
            userId = 0L;
            isGrsUser = true;
        } else {
            isGrsUser = userInformation.getUserType().equals(UserType.COMPLAINANT);
            userId = isGrsUser ? userInformation.getUserId() : userInformation.getOfficeInformation().getEmployeeRecordId();
        }

        boolean isAnonymous = (grievanceRequestDTO.getIsAnonymous() != null && grievanceRequestDTO.getIsAnonymous());
        if (grievanceRequestDTO.getOfflineGrievanceUpload() != null && grievanceRequestDTO.getOfflineGrievanceUpload()) {
            offlineGrievanceUploaded = true;
            uploaderGroOfficeUnitOrganogramId = userInformation != null && userInformation.getOfficeInformation() != null ? userInformation.getOfficeInformation().getOfficeUnitOrganogramId() : null;
            if (!isAnonymous) {
                Complainant complainant = this.complainantService.findComplainantByPhoneNumber(grievanceRequestDTO.getPhoneNumber());
                userId = complainant.getId();
            }
            isGrsUser = true;
        }

        Long officeId = Long.valueOf(grievanceRequestDTO.getOfficeId());
        CitizenCharter citizenCharter = null;

        // Check for null or empty serviceId before parsing
        if (grievanceRequestDTO.getServiceId() != null && !grievanceRequestDTO.getServiceId().isEmpty()) {
            citizenCharter = this.citizenCharterDAO.findByOfficeIdAndServiceId(officeId, Long.valueOf(grievanceRequestDTO.getServiceId()));
        }

        GrievanceCurrentStatus currentStatus = (officeId == 0 ? GrievanceCurrentStatus.CELL_NEW : GrievanceCurrentStatus.NEW);
        MediumOfSubmission medium = MediumOfSubmission.ONLINE;

        if (currentStatus.equals(GrievanceCurrentStatus.NEW) && grievanceRequestDTO.getOfflineGrievanceUpload() != null && grievanceRequestDTO.getOfflineGrievanceUpload()) {
            medium = MediumOfSubmission.CONVENTIONAL_METHOD;
        } else if (currentStatus.equals(GrievanceCurrentStatus.NEW) && grievanceRequestDTO.getIsSelfMotivated() != null && grievanceRequestDTO.getIsSelfMotivated()) {
            medium = MediumOfSubmission.SELF_MOTIVATED_ACCEPTANCE;
        }

        Grievance grievance = Grievance.builder()
                .details(grievanceRequestDTO.getBody())
                .subject(grievanceRequestDTO.getSubject())
                .officeId(officeId)
                .officeLayers(grievanceRequestDTO.getOfficeLayers())
                .serviceOrigin(citizenCharter == null ? null : citizenCharter.getServiceOrigin())
                .serviceOriginBeforeForward(citizenCharter == null ? null : citizenCharter.getServiceOrigin())
                .grsUser(isGrsUser)
                .complainantId(isAnonymous ? 0L : userId)
                .grievanceType(grievanceRequestDTO.getServiceType())
                .grievanceCurrentStatus(currentStatus)
                .isAnonymous(isAnonymous)
                .trackingNumber(StringUtil.isValidString(grievanceRequestDTO.getServiceTrackingNumber()) ? grievanceRequestDTO.getServiceTrackingNumber() : null)
                .otherService(grievanceRequestDTO.getServiceOthers())
                .otherServiceBeforeForward(grievanceRequestDTO.getServiceOthers())
                .serviceReceiver(grievanceRequestDTO.getServiceReceiver())
                .serviceReceiverRelation(grievanceRequestDTO.getRelation())
                .isOfflineGrievance(offlineGrievanceUploaded)
                .uploaderOfficeUnitOrganogramId(uploaderGroOfficeUnitOrganogramId)
                .isSelfMotivatedGrievance(grievanceRequestDTO.getIsSelfMotivated() != null && grievanceRequestDTO.getIsSelfMotivated())
                .sourceOfGrievance(grievanceRequestDTO.getSourceOfGrievance())
                .complaintCategory(grievanceRequestDTO.getGrievanceCategory())
                .mediumOfSubmission(medium.name())
                .spProgrammeId(StringUtil.isValidString(grievanceRequestDTO.getSpProgrammeId()) ? Integer.parseInt(grievanceRequestDTO.getSpProgrammeId()) : null)
                .geoDivisionId(StringUtil.isValidString(grievanceRequestDTO.getDivision()) ? Integer.parseInt(grievanceRequestDTO.getDivision()) : null)
                .geoDistrictId(StringUtil.isValidString(grievanceRequestDTO.getDistrict()) ? Integer.parseInt(grievanceRequestDTO.getDistrict()) : null)
                .geoUpazilaId(StringUtil.isValidString(grievanceRequestDTO.getUpazila()) ? Integer.parseInt(grievanceRequestDTO.getUpazila()) : null)
                .build();

        grievance.setStatus(true);
        grievance = grievanceRepo.save(grievance);
        return grievance;
    }
}

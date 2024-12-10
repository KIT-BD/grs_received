package com.grs.mobileApp.controller;

import com.grs.api.model.request.GrievanceForwardingNoteDTO;
import com.grs.api.model.response.EmployeeRecordDTO;
import com.grs.api.model.response.GenericResponse;
import com.grs.api.model.response.dashboard.latest.GRSStatisticDTO;
import com.grs.api.model.response.file.FileDerivedDTO;
import com.grs.core.dao.EmployeeRecordDAO;
import com.grs.core.dao.GRSStatisticsDAO;
import com.grs.core.dao.GrievanceForwardingDAO;
import com.grs.core.dao.OfficeUnitDAO;
import com.grs.core.domain.grs.*;
import com.grs.core.domain.projapoti.EmployeeRecord;
import com.grs.core.domain.projapoti.OfficeUnit;
import com.grs.core.model.ListViewType;
import com.grs.core.service.GrievanceService;
import com.grs.core.service.OfficesGroService;
import com.grs.mobileApp.dto.*;
import com.grs.mobileApp.service.MobileGrievanceService;
import com.grs.mobileApp.service.MobilePublicAPIService;
import com.grs.api.model.response.GrievanceForwardingEmployeeRecordsDTO;
import com.grs.core.domain.projapoti.Office;
import com.grs.core.repo.projapoti.OfficeRepo;
import com.grs.core.service.GrievanceForwardingService;
import com.grs.utils.BanglaConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import com.grs.api.model.UserInformation;
import com.grs.core.service.ComplainantService;
import com.grs.utils.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MobileGrievanceController {

    @Autowired
    private MobileGrievanceService mobileGrievanceService;
    private final ComplainantService complainantService;
    private final GrievanceForwardingService grievanceForwardingService;
    @Autowired
    private GrievanceService grievanceService;
    @Autowired
    private GrievanceForwardingDAO grievanceForwardingDAO;
    @Autowired
    private OfficesGroService officesGroService;
    @Autowired
    private EmployeeRecordDAO employeeRecordDAO;
    @Autowired
    private OfficeUnitDAO officeUnitDAO;
    @Autowired
    private GRSStatisticsDAO grsStatisticsDAO;

    @GetMapping("/api/grievance/total")
    public Map<String,Object> viewDashboardData(
            Authentication authentication,
            @RequestParam("officeId") Long officeId
    ){
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        GRSStatisticDTO dashboardData = grsStatisticsDAO.getGRSStatistics(userInformation, officeId, year, month);

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();

        if (dashboardData == null){
            response.put("data", null);
            response.put("status", "error");

            return response;
        }

        data.put("total_complaint", dashboardData.totalSubmittedGrievance);
        data.put("running_complaint", dashboardData.runningGrievances);
        data.put("closed_complaint", dashboardData.resolvedGrievances);
        data.put("time_passed_complaint", dashboardData.timeExpiredGrievances);
        data.put("total_appeal", dashboardData.appealTotal);
        data.put("running_appeal", dashboardData.appealRunning);
        data.put("time_passed_appeal", dashboardData.appealTimeExpired);
        data.put("closed_appeal", dashboardData.appealResolved);
        data.put("grievance_disposal_rate", dashboardData.resolveRate);
        data.put("appeal_rate", dashboardData.appealResolveRate);
        data.put("total_response", dashboardData.totalRating);
        data.put("average_rating", dashboardData.averageRating);
        data.put("safetynet_wise_grievance_list", null);
        data.put("graph_wise_grievance_list", null);
        data.put("office_wise_grievance_list", null);

        response.put("data", data);
        response.put("status", "success");

        return response;
    }

    @PostMapping(value = "/api/public-grievance/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MobileResponse savePublicGrievance(
            @RequestParam(value = "officeId", required = false) String officeId,
            @RequestParam("description") String description,
            @RequestParam("subject") String subject,
            @RequestParam(value = "sp_programme_id", required = false) String spProgrammeId,
            @RequestParam("mobile_number") String mobileNumber,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam(value = "division_id", required = false) Integer divisionId,
            @RequestParam(value = "district_id", required = false) Integer districtId,
            @RequestParam(value = "upazila_id", required = false) Integer upazilaId,
            @RequestParam("complaint_category") Integer complaintCategory,
            @RequestParam(value = "fileNameByUser", required = false) String fileNameByUser,
            @RequestPart(value = "files[]", required = false) List<MultipartFile> files,
            Principal principal) throws Exception {

        // Call the service method
        MobileGrievanceSubmissionResponseDTO response = mobileGrievanceService.savePublicGrievanceService(
                officeId, description, subject, spProgrammeId, mobileNumber, name,
                email, divisionId, districtId, upazilaId, complaintCategory,
                fileNameByUser, files, principal
        );

        return MobileResponse.builder()
                .status("success")
                .data(response)
                .build();
    }

    @PostMapping("/api/grievance/save")
    public MobileResponse submitMobileGrievanceWithLogin(
            Authentication authentication,
            @RequestParam(value = "officeId", required = false) Long officeId,
            @RequestParam(value = "service_id", required = false) String serviceId,
            @RequestParam("description") String description,
            @RequestParam("subject") String subject,
            @RequestParam("complainant_id") Long complainantId,
            @RequestParam("is_grs_user") Boolean isGrsUser,
            @RequestParam(value = "fileNameByUser", required = false) String fileNameByUser,
            @RequestParam(value = "files[]", required = false) List<MultipartFile> files,
            Principal principal
    ) throws Exception {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        Long authComplainantId = userInformation.getUserId();
        if (!Objects.equals(authComplainantId, complainantId)){
            return MobileResponse.builder()
                    .status("error")
                    .data("Invalid token for current user")
                    .build();
        }
        Complainant complainant = complainantService.findOne(complainantId);

        MobileGrievanceSubmissionResponseDTO responseDTO = mobileGrievanceService.saveGrievanceWithLogin(
                authentication,
                complainant,
                officeId,
                serviceId,
                description,
                subject,
                isGrsUser,
                fileNameByUser,
                files,
                principal
        );

        return MobileResponse.builder()
                .status("success")
                .data(responseDTO)
                .build();
    }

    @GetMapping("/api/grievance/list")
    public MobileResponse getGrievances(
            Authentication authentication,
            @RequestParam("complainant_id") Long id
    ) throws ParseException {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        Long complainantId = userInformation.getUserId();

        if (!Objects.equals(complainantId, id)){
            return MobileResponse.builder()
                    .status("error")
                    .data("Invalid token for complainant id")
                    .build();
        }

        if (complainantId == null){
            return MobileResponse.builder()
                    .status("error")
                    .data(new ArrayList<>())
                    .build();
        }

        List<MobileGrievanceResponseDTO> grievanceList = mobileGrievanceService.findGrievancesByUser(complainantId);

        return MobileResponse.builder()
                .status("success")
                .data(grievanceList)
                .build();
    }

    @PostMapping(value = "/api/administrative-grievance/request-appeal")
    public Map<String, Object> sendForAppeal(
            @RequestParam("complaint_id") Long complaint_id,
            @RequestParam("note") String note,
            Authentication authentication) {
        GrievanceForwardingNoteDTO grievanceForwardingNoteDTO = GrievanceForwardingNoteDTO.builder()
                .grievanceId(complaint_id)
                .note(note)
                .build();
        GenericResponse genericResponse = grievanceForwardingService.appealToOfficer(grievanceForwardingNoteDTO, authentication);

        Map<String, Object> response = new HashMap<>();
        response.put("status", genericResponse.isSuccess() ? "success" : "error");
        response.put("data", grievanceService.findGrievanceById(complaint_id));
        response.put("message", genericResponse.getMessage());
        return response;
    }

    @GetMapping("/api/grievance/complainant/movement")
    public Map<String,Object> getMovementForComplainant(
            Authentication authentication,
            @RequestParam("complaint_id") Long id
    ) throws ParseException {
        if (id == null){
            Map<String, Object> response = new HashMap<>();
            response.put("data", "Complaint could not be found");
            response.put("status", "error");

            return response;
        }
        List<GrievanceForwardingEmployeeRecordsDTO> grievanceList = grievanceForwardingService.getAllComplaintMovementHistoryByGrievance(id, authentication);
        List<MobileGrievanceForwardingDTO> forwardingDTOList = new ArrayList<>();

        for (GrievanceForwardingEmployeeRecordsDTO g : grievanceList){
            forwardingDTOList.add(
                    MobileGrievanceForwardingDTO.builder()
                            .id(null)
                            .complaint_id(Math.toIntExact(id))
                            .note(g.getComment())
                            .action(g.getAction())
                            .to_employee_record_id(null)
                            .from_employee_record_id(null)
                            .to_office_unit_organogram_id(null)
                            .from_office_unit_organogram_id(null)
                            .to_office_id(null)
                            .from_office_id(null)
                            .to_office_unit_id(null)
                            .from_office_unit_id(null)
                            .is_current(null)
                            .is_cc(g.getIsCC() ? 1 : 0)
                            .is_committee_head(g.getIsCommitteeHead() ? 1 : 0)
                            .is_committee_member(g.getIsCommitteeMember() ? 1 : 0)
                            .to_employee_name_bng(g.getToGroNameBangla())
                            .from_employee_name_bng(g.getFromGroNameBangla())
                            .to_employee_name_eng(g.getToGroNameEnglish())
                            .from_employee_name_eng(g.getFromGroNameEnglish())
                            .to_employee_designation_bng(g.getToDesignationNameBangla())
                            .from_employee_designation_bng(g.getFromDesignationNameBangla())
                            .to_office_name_bng(g.getToOfficeNameBangla())
                            .from_office_name_bng(g.getFromOfficeNameBangla())
                            .to_employee_unit_name_bng(g.getToOfficeUnitNameBangla())
                            .from_employee_unit_name_bng(g.getFromOfficeUnitNameBangla())
                            .from_employee_username(g.getFromGroUsername())
                            .from_employee_signature(null)
                            .created_at(String.valueOf(new Date() {{ String[] p=g.getCreatedAtEng().split("/"); int d=Integer.parseInt(p[0]),m=Integer.parseInt(p[1])-1,y=Integer.parseInt(p[2]); Calendar c=Calendar.getInstance(TimeZone.getTimeZone("UTC")); c.set(y,m,d,6,45,2); c.set(Calendar.MILLISECOND,0); setTime(c.getTimeInMillis()); } @Override public String toString(){ return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'").format(this); }}))
                            .updated_at(null)
                            .created_by(null)
                            .modified_by(null)
                            .status(null)
                            .deadline_date(null)
                            .current_status(null)
                            .is_seen(null)
                            .assigned_role(g.getAssignedRole())
                            .complain_movement_attachment(g.getFiles())
                            .build()

            );
        }


        Map<String, Object> response = new HashMap<>();
        response.put("data", forwardingDTOList);
        response.put("status", "success");

        return response;
    }

    @GetMapping("/api/grievance/movement")
    public Map<String,Object> getMovement(
            Authentication authentication,
            @RequestParam("complaint_id") Long id
    ) throws ParseException {
        if (id == null){
            Map<String, Object> response = new HashMap<>();
            response.put("data", "Complaint could not be found");
            response.put("status", "error");

            return response;
        }
        List<GrievanceForwardingEmployeeRecordsDTO> grievanceList = grievanceForwardingService.getAllComplainantComplaintMovementHistoryByGrievance(id, authentication);
        List<MobileGrievanceForwardingDTO> forwardingDTOList = new ArrayList<>();

        for (GrievanceForwardingEmployeeRecordsDTO g : grievanceList){
            forwardingDTOList.add(
                    MobileGrievanceForwardingDTO.builder()
                            .id(null)
                            .complaint_id(Math.toIntExact(id))
                            .note(g.getComment())
                            .action(g.getAction())
                            .to_employee_record_id(null)
                            .from_employee_record_id(null)
                            .to_office_unit_organogram_id(null)
                            .from_office_unit_organogram_id(null)
                            .to_office_id(null)
                            .from_office_id(null)
                            .to_office_unit_id(null)
                            .from_office_unit_id(null)
                            .is_current(null)
                            .is_cc(g.getIsCC() ? 1 : 0)
                            .is_committee_head(g.getIsCommitteeHead() ? 1 : 0)
                            .is_committee_member(g.getIsCommitteeMember() ? 1 : 0)
                            .to_employee_name_bng(g.getToGroNameBangla())
                            .from_employee_name_bng(g.getFromGroNameBangla())
                            .to_employee_name_eng(g.getToGroNameEnglish())
                            .from_employee_name_eng(g.getFromGroNameEnglish())
                            .to_employee_designation_bng(g.getToDesignationNameBangla())
                            .from_employee_designation_bng(g.getFromDesignationNameBangla())
                            .to_office_name_bng(g.getToOfficeNameBangla())
                            .from_office_name_bng(g.getFromOfficeNameBangla())
                            .to_employee_unit_name_bng(g.getToOfficeUnitNameBangla())
                            .from_employee_unit_name_bng(g.getFromOfficeUnitNameBangla())
                            .from_employee_username(g.getFromGroUsername())
                            .from_employee_signature(null)
                            .created_at(String.valueOf(new Date() {{ String[] p=g.getCreatedAtEng().split("/"); int d=Integer.parseInt(p[0]),m=Integer.parseInt(p[1])-1,y=Integer.parseInt(p[2]); Calendar c=Calendar.getInstance(TimeZone.getTimeZone("UTC")); c.set(y,m,d,6,45,2); c.set(Calendar.MILLISECOND,0); setTime(c.getTimeInMillis()); } @Override public String toString(){ return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'").format(this); }}))
                            .updated_at(null)
                            .created_by(null)
                            .modified_by(null)
                            .status(null)
                            .deadline_date(null)
                            .current_status(null)
                            .is_seen(null)
                            .assigned_role(g.getAssignedRole())
                            .complain_movement_attachment(g.getFiles())
                            .build()

            );
        }


        Map<String, Object> response = new HashMap<>();
        response.put("data", forwardingDTOList);
        response.put("status", "success");

        return response;
    }

    @GetMapping("/api/grievance/details")
    public Map<String, Object> getGrievanceDetails(
            Authentication authentication,
            @RequestParam("complaint_id") Long complaintId
    ) throws ParseException {

        return mobileGrievanceService.getComplaintDetailsById(complaintId);
    }

    @GetMapping("/api/grievance-track")
    public MobileResponse getGrievanceByTrackingNumber(
            @RequestParam("tracking_number") String trx
    ){
        List<MobileGrievanceResponseDTO> grievanceList = mobileGrievanceService.findGrievancesByTrackingNumber(trx);

        if (grievanceList == null || grievanceList.isEmpty()){
            return MobileResponse.builder()
                    .status("empty")
                    .data(null)
                    .build();
        }
        return MobileResponse.builder()
                .status("success")
                .data(grievanceList.get(0))
                .build();
    }


    @RequestMapping(value = "/api/grievance/list/to-employee", method = RequestMethod.GET)
    public Map<String, Object> getToEmployeeGrievances(Authentication authentication,
                                                       @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {

        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);

        return mobileGrievanceService.findGrievances(userInformation, pageable, ListViewType.NORMAL_INBOX);
    }

    @RequestMapping(value = "/api/grievance/list/from-employee", method = RequestMethod.GET)
    public Map<String, Object> getFromEmployeeGrievances(Authentication authentication,
                                                     @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {

        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);

        return mobileGrievanceService.findGrievances(userInformation, pageable, ListViewType.NORMAL_OUTBOX);
    }

    @RequestMapping(value = "/api/grievance/list/closed_grievances", method = RequestMethod.GET)
    public Map<String, Object> getResolvedGrievances(Authentication authentication,
                                                         @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {

        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);

        return mobileGrievanceService.findGrievances(userInformation, pageable, ListViewType.NORMAL_CLOSED);
    }

    @RequestMapping(value = "/api/grievance/list/forwarded_to_other_office", method = RequestMethod.GET)
    public Map<String, Object> getForwardedGrievances(Authentication authentication,
                                                     @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {

        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);

        return mobileGrievanceService.findGrievances(userInformation, pageable, ListViewType.NORMAL_FORWARDED);
    }

    @RequestMapping(value = "/api/grievance/list/expired_grievances", method = RequestMethod.GET)
    public Map<String, Object> getExpiredGrievances(Authentication authentication,
                                                      @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {

        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);

        return mobileGrievanceService.findGrievances(userInformation, pageable, ListViewType.NORMAL_EXPIRED);
    }

    @RequestMapping(value = "/api/grievance/list/cc", method = RequestMethod.GET)
    public Map<String, Object> getCC(Authentication authentication,
                                                    @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {

        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);

        return mobileGrievanceService.findGrievances(userInformation, pageable, ListViewType.NORMAL_CC);
    }

    @RequestMapping(value = "/api/grievance/list/incoming-appeal", method = RequestMethod.GET)
    public Map<String, Object> getIncomingAppeals(Authentication authentication,
                                                  @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {

        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);

        return mobileGrievanceService.findGrievances(userInformation, pageable, ListViewType.APPEAL_INBOX);
    }
    @RequestMapping(value = "/api/grievance/list/closed-appeal", method = RequestMethod.GET)
    public Map<String, Object> getClosedAppeals(Authentication authentication,
                                                  @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {

        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);

        return mobileGrievanceService.findGrievances(userInformation, pageable, ListViewType.APPEAL_CLOSED);
    }
    @RequestMapping(value = "/api/grievance/list/sent-appeal", method = RequestMethod.GET)
    public Map<String, Object> getSentAppeal(Authentication authentication,
                                                  @PageableDefault(value = Integer.MAX_VALUE) Pageable pageable) {

        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);

        return mobileGrievanceService.findGrievances(userInformation, pageable, ListViewType.APPEAL_OUTBOX);
    }

    @GetMapping("/api/administrative-grievance/departmental-action-officers")
    public Map<String,Object> getMovementForComplainant(
            Authentication authentication,
            @RequestParam(value = "complaint_id") Long complaint_id,
            @RequestParam(value = "action", required = false) String action
    ) throws ParseException {
        if (complaint_id == null){
            Map<String, Object> response = new HashMap<>();
            response.put("data", "Complaint could not be found");
            response.put("status", "error");

            return response;
        }
        List<GrievanceForwardingEmployeeRecordsDTO> grievanceList = grievanceForwardingService.getAllComplaintMovementHistoryByGrievance(complaint_id, authentication);

        if (!(action == null || action.isEmpty())){
            grievanceList = grievanceList.stream().filter(item -> Objects.equals(item.getAction(), action)).collect(Collectors.toList());
        }

        List<MobileGrievanceForwardingDTO> forwardingDTOList = new ArrayList<>();//

        for (GrievanceForwardingEmployeeRecordsDTO g : grievanceList){
            forwardingDTOList.add(
                    MobileGrievanceForwardingDTO.builder()
                            .id(g.getId())
                            .complaint_id(Math.toIntExact(complaint_id))
                            .note(g.getComment())
                            .action(g.getAction())
                            .to_employee_record_id(Math.toIntExact(g.getTo_employee_record_id()))
                            .from_employee_record_id(Math.toIntExact(g.getFrom_employee_record_id()))
                            .to_office_unit_organogram_id(Math.toIntExact(g.getTo_office_unit_organogram_id()))
                            .from_office_unit_organogram_id(Math.toIntExact(g.getFrom_office_unit_organogram_id()))
                            .to_office_id(null)
                            .from_office_id(null)
                            .to_office_unit_id(null)
                            .from_office_unit_id(null)
                            .is_current(null)
                            .is_cc(g.getIsCC() ? 1 : 0)
                            .is_committee_head(g.getIsCommitteeHead() ? 1 : 0)
                            .is_committee_member(g.getIsCommitteeMember() ? 1 : 0)
                            .to_employee_name_bng(g.getToGroNameBangla())
                            .from_employee_name_bng(g.getFromGroNameBangla())
                            .to_employee_name_eng(g.getToGroNameEnglish())
                            .from_employee_name_eng(g.getFromGroNameEnglish())
                            .to_employee_designation_bng(g.getToDesignationNameBangla())
                            .from_employee_designation_bng(g.getFromDesignationNameBangla())
                            .to_office_name_bng(g.getToOfficeNameBangla())
                            .from_office_name_bng(g.getFromOfficeNameBangla())
                            .to_employee_unit_name_bng(g.getToOfficeUnitNameBangla())
                            .from_employee_unit_name_bng(g.getFromOfficeUnitNameBangla())
                            .from_employee_username(g.getFromGroUsername())
                            .from_employee_signature(null)
                            .created_at(String.valueOf(new Date() {{ String[] p=g.getCreatedAtEng().split("/"); int d=Integer.parseInt(p[0]),m=Integer.parseInt(p[1])-1,y=Integer.parseInt(p[2]); Calendar c=Calendar.getInstance(TimeZone.getTimeZone("UTC")); c.set(y,m,d,6,45,2); c.set(Calendar.MILLISECOND,0); setTime(c.getTimeInMillis()); } @Override public String toString(){ return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'").format(this); }}))
                            .updated_at(null)
                            .created_by(null)
                            .modified_by(null)
                            .status(null)
                            .deadline_date(null)
                            .current_status(null)
                            .is_seen(null)
                            .assigned_role(g.getAssignedRole())
                            .complain_movement_attachment(g.getFiles())
                            .build()

            );
        }


        Map<String, Object> response = new HashMap<>();
        response.put("data", forwardingDTOList);
        response.put("status", "success");

        return response;
    }

    @GetMapping("/api/grievance/gro-info")
    public Map<String,Object> getGROInfo(
            Authentication authentication,
            @RequestParam("complaint_id") Long complaint_id
    ){
        UserInformation auth = Utility.extractUserInformationFromAuthentication(authentication);

        Grievance grievance = grievanceService.findGrievanceById(complaint_id);
        OfficesGRO officesGRO = this.officesGroService.findOfficesGroByOfficeId(grievance.getOfficeId());
        List<GrievanceForwarding> grievanceForwardings = this.grievanceForwardingDAO.getAllRelatedComplaintMovements(complaint_id,
                officesGRO.getOfficeId(),
                new ArrayList<Long>() {{
                    add(officesGRO.getGroOfficeUnitOrganogramId());
                }},
                "%APPEAL%");

        GrievanceForwarding gro = grievanceForwardings.get(0);
        EmployeeRecord employeeRecord = employeeRecordDAO.findEmployeeRecordById(gro.getToEmployeeRecordId());
        OfficeUnit officeUnit = officeUnitDAO.findById(gro.getToOfficeUnitId());

        Map<String, Object> response = new HashMap<>();
        Map<String,Object> data = new HashMap<>();
        data.put("unit", officeUnit.getOffice().getId());
        data.put("employeeRecord", gro.getToEmployeeRecordId());
        data.put("joiningDate", null);
        data.put("office", gro.getToOfficeId());
        data.put("id", gro.getId());
        data.put("designation", gro.getToEmployeeDesignationBangla());
        data.put("organogram", gro.getToOfficeUnitOrganogramId());
        data.put("lastDate", null);
        data.put("status", null);
        data.put("officeHead", null);
        data.put("officeNameEn", gro.getToOfficeNameBangla());
        data.put("officeNameBn", gro.getToOfficeNameBangla());
        data.put("unitNameEn", officeUnit.getUnitNameEnglish());
        data.put("unitNameBn", officeUnit.getUnitNameBangla());
        data.put("officeMinistryId", officeUnit.getOfficeMinistry().getId());
        data.put("ministryNameBng", officeUnit.getOfficeMinistry().getNameBangla());
        data.put("ministryNameEng", officeUnit.getOfficeMinistry().getNameEnglish());
        data.put("ministryShortName", officeUnit.getOfficeMinistry().getNameEnglishShort());
        data.put("ministryReferenceCode", officeUnit.getOfficeMinistry().getReferenceCode());
        data.put("refOriginUnitOrganogramId", null);
        data.put("is_office_admin", auth.getIsOfficeAdmin());
        data.put("is_unit_admin", null);
        data.put("is_office_head", null);
        data.put("is_unit_head", null);
        data.put("name_bng", employeeRecord.getNameBangla());
        data.put("name", employeeRecord.getNameEnglish());
        data.put("mobile", employeeRecord.getPersonalMobile());
        data.put("email", employeeRecord.getPersonalEmail());
        data.put("username", auth.getUsername());

        response.put("data", data);
        response.put("status", "success");

        return response;
    }


}

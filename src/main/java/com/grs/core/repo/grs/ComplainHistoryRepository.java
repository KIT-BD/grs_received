package com.grs.core.repo.grs;

import com.grs.core.domain.grs.ComplainHistory;
import com.grs.core.domain.grs.DashboardData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplainHistoryRepository extends JpaRepository<ComplainHistory, Long> {

    @Query("SELECT c FROM ComplainHistory c " +
            "WHERE c.id IN (" +
            "   SELECT MAX(c2.id) FROM ComplainHistory c2 " +
            "   WHERE c2.officeId = :officeId " +
            "   AND c2.complainId NOT IN (" +
            "       SELECT c3.complainId FROM ComplainHistory c3 " +
            "       WHERE c3.currentStatus IN ('APPEAL', 'APPEAL_CLOSED', 'CELL_APPEAL')" +
            "   ) " +
            "   GROUP BY c2.complainId" +
            ") " +
            "ORDER BY c.createdAt DESC")
    Page<ComplainHistory> findGrievanceRegisterGrievances(@Param("officeId") Long officeId, Pageable pageable);

    @Query("SELECT c FROM ComplainHistory c " +
            "WHERE c.officeId       = ?1 " +
            "  AND c.trackingNumber = ?2 " +
            "  AND c.currentStatus NOT IN ('APPEAL', 'APPEAL_CLOSED', 'CELL_APPEAL')")
    Page<ComplainHistory> findGrievanceRegisterGrievancesByTrackingNumber(Long officeId, String trackingNumber, Pageable pageable);

    @Query(value = "SELECT * FROM complain_history " +
                    "WHERE current_status IN ('NEW', 'RETAKE') " +
                    "AND created_at < DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 0 MONTH), '%Y-%m-01 00:00:00') " +
                    "AND closed_at IS NULL " +
                    "AND DATEDIFF(DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 0 MONTH), '%Y-%m-01 00:00:00'), created_at) > 30 " +
                    "AND office_id = :officeId",
            nativeQuery = true)
    List<ComplainHistory> getTimeExpiredGrievancesByOfficeId(@Param("officeId") Long officeId);

    @Query("SELECT c FROM ComplainHistory c " +
            "WHERE c.id IN (" +
            "   SELECT MAX(c2.id) FROM ComplainHistory c2 " +
            "   WHERE c2.officeId = :officeId " +
            "   AND c2.currentStatus IN ('APPEAL', 'APPEAL_CLOSED', 'CELL_APPEAL') " +
            "   GROUP BY c2.complainId" +
            ") " +
            "ORDER BY c.createdAt DESC")
    Page<ComplainHistory> getPageableDashboardDataAppealRegister(@Param("officeId") Long officeId, Pageable pageable);
    
    @Query(value =
            "SELECT * FROM complain_history " +
                    "WHERE id IN (" +
                    "SELECT MAX(id) FROM complain_history " +
                    "WHERE office_id = :officeId AND current_status IN ('CLOSED') " +
                    "GROUP BY complain_id) ORDER BY created_at DESC",
            nativeQuery = true
    )
    List<ComplainHistory> getAllResolutions(@Param("officeId") Long officeId);

    @Query(value = "SELECT * FROM complain_history " +
            "WHERE current_status IN ('APPEAL', 'CELL_APPEAL') " +
            "AND created_at < DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 0 MONTH), '%Y-%m-01 00:00:00') " +
            "AND closed_at IS NULL " +
            "AND DATEDIFF(DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 0 MONTH), '%Y-%m-01 00:00:00'), created_at) > 30 " +
            "AND office_id = :officeId",
            nativeQuery = true)
    List<ComplainHistory> getTimeExpiredAppealsByOfficeId(@Param("officeId") Long officeId);

    @Query(value =
            "SELECT * FROM complain_history " +
                    "WHERE id IN (" +
                    "SELECT MAX(id) FROM complain_history " +
                    "WHERE office_id = :officeId AND current_status IN ('APPEAL_CLOSED') " +
                    "GROUP BY complain_id) ORDER BY created_at DESC",
            nativeQuery = true)
    List<ComplainHistory> getResolvedAppealsOfCurrentMonthByOfficeId(@Param("officeId") Long officeId);
}

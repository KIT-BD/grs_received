package com.grs.core.repo.grs;

import com.grs.core.domain.grs.ComplainHistory;
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
            "   AND c2.currentStatus NOT IN ('APPEAL', 'APPEAL_CLOSED', 'CELL_APPEAL') " +
            "   GROUP BY c2.complainId" +
            ") " +
            "ORDER BY c.createdAt DESC")
    Page<ComplainHistory> findGrievanceRegisterGrievances(@Param("officeId") Long officeId, Pageable pageable);

    @Query("SELECT c FROM ComplainHistory c " +
            "WHERE c.officeId       = ?1 " +
            "  AND c.trackingNumber = ?2 " +
            "  AND c.currentStatus NOT IN ('APPEAL', 'APPEAL_CLOSED', 'CELL_APPEAL')")
    Page<ComplainHistory> findGrievanceRegisterGrievancesByTrackingNumber(Long officeId, String trackingNumber, Pageable pageable);

    @Query(value =
            "SELECT * FROM complain_history " +
                    "WHERE id IN (" +
                    "SELECT MAX(id) FROM complain_history " +
                    "WHERE office_id = 2287 AND current_status IN ('CLOSED') " +
                    "GROUP BY complain_id) ORDER BY created_at DESC",
            nativeQuery = true
    )
    List<ComplainHistory> getAllResolutions();
}

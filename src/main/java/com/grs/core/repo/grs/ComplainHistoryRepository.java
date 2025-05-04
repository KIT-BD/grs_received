package com.grs.core.repo.grs;

import com.grs.core.domain.grs.ComplainHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplainHistoryRepository extends JpaRepository<ComplainHistory, Long> {

    @Query("SELECT c FROM ComplainHistory c " +
            "WHERE c.officeId = :officeId AND c.currentStatus NOT IN ('APPEAL', 'APPEAL_CLOSED', 'CELL_APPEAL')")
    Page<ComplainHistory> findGrievanceRegisterGrievances(@Param("officeId") Long officeId, Pageable pageable);
}

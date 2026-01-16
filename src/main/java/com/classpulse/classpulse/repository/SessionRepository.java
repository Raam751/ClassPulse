package com.classpulse.classpulse.repository;

import com.classpulse.classpulse.entity.Session;
import com.classpulse.classpulse.entity.SessionStatus;
import com.classpulse.classpulse.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    Optional<Session> findByCode(String code);

    List<Session> findByCreatedBy(User createdBy);

    List<Session> findByStatus(SessionStatus status);

    boolean existsByCode(String code);

    // Filtering queries
    Page<Session> findByStatus(SessionStatus status, Pageable pageable);

    Page<Session> findByCreatedById(Long teacherId, Pageable pageable);

    Page<Session> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Combined filters
    @Query("SELECT s FROM Session s WHERE " +
            "(:status IS NULL OR s.status = :status) AND " +
            "(:teacherId IS NULL OR s.createdBy.id = :teacherId) AND " +
            "(:startDate IS NULL OR s.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR s.createdAt <= :endDate)")
    Page<Session> findWithFilters(
            @Param("status") SessionStatus status,
            @Param("teacherId") Long teacherId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}

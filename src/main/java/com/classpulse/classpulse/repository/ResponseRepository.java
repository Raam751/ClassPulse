package com.classpulse.classpulse.repository;

import com.classpulse.classpulse.entity.Question;
import com.classpulse.classpulse.entity.Response;
import com.classpulse.classpulse.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResponseRepository extends JpaRepository<Response, Long> {

    List<Response> findByQuestion(Question question);

    List<Response> findByQuestionId(Long questionId);

    // Paginated version
    Page<Response> findByQuestionId(Long questionId, Pageable pageable);

    List<Response> findByUser(User user);

    Optional<Response> findByQuestionAndUser(Question question, User user);

    boolean existsByQuestionAndUser(Question question, User user);
}

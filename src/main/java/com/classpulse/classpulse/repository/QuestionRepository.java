package com.classpulse.classpulse.repository;

import com.classpulse.classpulse.entity.Question;
import com.classpulse.classpulse.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findBySession(Session session);

    List<Question> findBySessionId(Long sessionId);
}

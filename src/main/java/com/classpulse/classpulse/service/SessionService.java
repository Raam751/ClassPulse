package com.classpulse.classpulse.service;

import com.classpulse.classpulse.entity.Session;
import com.classpulse.classpulse.entity.SessionStatus;
import com.classpulse.classpulse.entity.User;
import com.classpulse.classpulse.exception.BadRequestException;
import com.classpulse.classpulse.exception.ResourceNotFoundException;
import com.classpulse.classpulse.repository.SessionRepository;
import com.classpulse.classpulse.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
@Transactional
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    public SessionService(SessionRepository sessionRepository, UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    // Paginated version
    @Transactional(readOnly = true)
    public Page<Session> getAllSessions(Pageable pageable) {
        return sessionRepository.findAll(pageable);
    }

    // Non-paginated version (for backward compatibility)
    @Transactional(readOnly = true)
    public List<Session> getAllSessions() {
        return sessionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Session getSessionById(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session", id));
    }

    @Transactional(readOnly = true)
    public Session getSessionByCode(String code) {
        return sessionRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Session", "code", code));
    }

    @Transactional(readOnly = true)
    public List<Session> getSessionsByTeacher(Long teacherId) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", teacherId));
        return sessionRepository.findByCreatedBy(teacher);
    }

    @Transactional(readOnly = true)
    public List<Session> getActiveSessions() {
        return sessionRepository.findByStatus(SessionStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public Page<Session> filterSessions(SessionStatus status, Long teacherId,
            java.time.LocalDateTime startDate, java.time.LocalDateTime endDate,
            Pageable pageable) {
        return sessionRepository.findWithFilters(status, teacherId, startDate, endDate, pageable);
    }

    public Session createSession(String title, Long teacherId) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", teacherId));

        Session session = new Session();
        session.setTitle(title);
        session.setCode(generateUniqueCode());
        session.setStatus(SessionStatus.CREATED);
        session.setCreatedBy(teacher);

        return sessionRepository.save(session);
    }

    public Session startSession(Long id) {
        Session session = getSessionById(id);
        if (session.getStatus() != SessionStatus.CREATED) {
            throw new BadRequestException(
                    "Session can only be started from CREATED status. Current status: " + session.getStatus());
        }
        session.setStatus(SessionStatus.ACTIVE);
        return sessionRepository.save(session);
    }

    public Session endSession(Long id) {
        Session session = getSessionById(id);
        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new BadRequestException(
                    "Session can only be ended from ACTIVE status. Current status: " + session.getStatus());
        }
        session.setStatus(SessionStatus.ENDED);
        return sessionRepository.save(session);
    }

    public void deleteSession(Long id) {
        if (!sessionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Session", id);
        }
        sessionRepository.deleteById(id);
    }

    private String generateUniqueCode() {
        Random random = new Random();
        String code;
        do {
            code = String.format("%06d", random.nextInt(1000000));
        } while (sessionRepository.existsByCode(code));
        return code;
    }
}

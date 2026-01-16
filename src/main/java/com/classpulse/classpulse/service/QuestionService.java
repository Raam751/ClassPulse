package com.classpulse.classpulse.service;

import com.classpulse.classpulse.entity.Question;
import com.classpulse.classpulse.entity.QuestionType;
import com.classpulse.classpulse.entity.Session;
import com.classpulse.classpulse.entity.SessionStatus;
import com.classpulse.classpulse.exception.BadRequestException;
import com.classpulse.classpulse.exception.ResourceNotFoundException;
import com.classpulse.classpulse.repository.QuestionRepository;
import com.classpulse.classpulse.repository.SessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final SessionRepository sessionRepository;

    public QuestionService(QuestionRepository questionRepository, SessionRepository sessionRepository) {
        this.questionRepository = questionRepository;
        this.sessionRepository = sessionRepository;
    }

    @Transactional(readOnly = true)
    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Question getQuestionById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question", id));
    }

    @Transactional(readOnly = true)
    public List<Question> getQuestionsBySession(Long sessionId) {
        return questionRepository.findBySessionId(sessionId);
    }

    public Question createQuestion(Long sessionId, String text, QuestionType type, String optionsJson) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session", sessionId));

        if (session.getStatus() == SessionStatus.ENDED) {
            throw new BadRequestException("Cannot add questions to an ended session");
        }

        Question question = new Question();
        question.setSession(session);
        question.setText(text);
        question.setType(type);
        question.setOptionsJson(optionsJson);

        return questionRepository.save(question);
    }

    public Question updateQuestion(Long id, String text, QuestionType type, String optionsJson) {
        Question question = getQuestionById(id);
        question.setText(text);
        question.setType(type);
        question.setOptionsJson(optionsJson);
        return questionRepository.save(question);
    }

    public void deleteQuestion(Long id) {
        if (!questionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Question", id);
        }
        questionRepository.deleteById(id);
    }
}

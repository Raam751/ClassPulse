package com.classpulse.classpulse.service;

import com.classpulse.classpulse.entity.Question;
import com.classpulse.classpulse.entity.Response;
import com.classpulse.classpulse.entity.SessionStatus;
import com.classpulse.classpulse.entity.User;
import com.classpulse.classpulse.exception.BadRequestException;
import com.classpulse.classpulse.exception.DuplicateResourceException;
import com.classpulse.classpulse.exception.ResourceNotFoundException;
import com.classpulse.classpulse.repository.QuestionRepository;
import com.classpulse.classpulse.repository.ResponseRepository;
import com.classpulse.classpulse.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ResponseService {

    private final ResponseRepository responseRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    public ResponseService(ResponseRepository responseRepository,
            QuestionRepository questionRepository,
            UserRepository userRepository) {
        this.responseRepository = responseRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Response> getAllResponses() {
        return responseRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Response getResponseById(Long id) {
        return responseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Response", id));
    }

    // Paginated version
    @Transactional(readOnly = true)
    public Page<Response> getResponsesByQuestion(Long questionId, Pageable pageable) {
        return responseRepository.findByQuestionId(questionId, pageable);
    }

    // Non-paginated version (backward compatibility)
    @Transactional(readOnly = true)
    public List<Response> getResponsesByQuestion(Long questionId) {
        return responseRepository.findByQuestionId(questionId);
    }

    @Transactional(readOnly = true)
    public List<Response> getResponsesByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return responseRepository.findByUser(user);
    }

    public Response submitResponse(Long questionId, Long userId, String answer) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question", questionId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (question.getSession().getStatus() != SessionStatus.ACTIVE) {
            throw new BadRequestException("Can only respond to questions in active sessions. Session status: "
                    + question.getSession().getStatus());
        }

        if (responseRepository.existsByQuestionAndUser(question, user)) {
            throw new DuplicateResourceException("User has already responded to this question");
        }

        Response response = new Response();
        response.setQuestion(question);
        response.setUser(user);
        response.setAnswer(answer);

        return responseRepository.save(response);
    }

    public Response updateResponse(Long id, String answer) {
        Response response = getResponseById(id);

        if (response.getQuestion().getSession().getStatus() != SessionStatus.ACTIVE) {
            throw new BadRequestException("Can only update responses in active sessions");
        }

        response.setAnswer(answer);
        return responseRepository.save(response);
    }

    public void deleteResponse(Long id) {
        if (!responseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Response", id);
        }
        responseRepository.deleteById(id);
    }
}

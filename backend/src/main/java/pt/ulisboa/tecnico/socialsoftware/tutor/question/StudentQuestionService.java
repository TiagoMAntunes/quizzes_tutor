package pt.ulisboa.tecnico.socialsoftware.tutor.question;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Service
public class StudentQuestionService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public StudentQuestionDto createStudentQuestion(int courseId, QuestionDto questionDto, int studentId){
        User student = userRepository.findById(studentId).orElseThrow(() -> new TutorException(ACCESS_DENIED, studentId));
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new TutorException(COURSE_NOT_FOUND, courseId));
        if (questionDto.getKey() == null) {
            int maxQuestionNumber = questionRepository.getMaxQuestionNumber() != null ?
                    questionRepository.getMaxQuestionNumber() : 0;
            questionDto.setKey(maxQuestionNumber + 1);
        }
        StudentQuestion studentQuestion = new StudentQuestion(course, questionDto, student);
        studentQuestion.setCreationDate(LocalDateTime.now());
        this.entityManager.persist(studentQuestion);

        return new StudentQuestionDto(studentQuestion);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void studentQuestionApproveReject(Integer questionId, StudentQuestion.QuestionStatus status, String explanation, User teacher, CourseExecution execution) {
        if (teacher.getRole() != User.Role.TEACHER) {
            throw new TutorException(ACCESS_DENIED);
        }

        StudentQuestion question = studentQuestionRepository.findById(questionId).orElseThrow(() -> new TutorException(QUESTION_NOT_FOUND, questionId));

        if(!teacher.getCourseExecutions().contains(execution)){
            throw new TutorException(ACCESS_DENIED);
        }

        switch (status) {
            case REJECTED:
                question.setQuestionStatus(status);
                question.setRejectionExplanation(explanation);
                break;
            case APPROVED:
                question.setQuestionStatus(status);
                break;
        }
    }

    public List<StudentQuestion> getStudentQuestions(User student){

        List<StudentQuestion> list = studentQuestionRepository.findAll().stream()
                .filter(studentQuestion -> studentQuestion.getUser() == student)
                .collect(Collectors.toList());

        if(list.size() == 0){
            throw new TutorException(NO_QUESTION_SUBMITTED);
        }else{
            return list;
        }
    }

}

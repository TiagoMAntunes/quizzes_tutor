package pt.ulisboa.tecnico.socialsoftware.tutor.question;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Service
public class StudentQuestionService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentQuestionRepository studentQuestionRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private CourseExecutionRepository courseExecutionRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public StudentQuestionDto createStudentQuestion(int courseId, QuestionDto questionDto, int studentId){
        User student = userRepository.findById(studentId).orElseThrow(() -> new TutorException(ACCESS_DENIED, studentId));
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new TutorException(COURSE_NOT_FOUND, courseId));
        checkRoleStudent(student);
        checkEnrolledCourseExecution(student, course);
        StudentQuestion studentQuestion = new StudentQuestion(course, questionDto, student);
        studentQuestion.setCreationDate(LocalDateTime.now());
        this.entityManager.persist(studentQuestion);

        return new StudentQuestionDto(studentQuestion);
    }

    @Retryable(
    value = { SQLException.class },
    backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void studentQuestionApproveReject(int questionId, StudentQuestion.QuestionStatus status) {
        StudentQuestion question = studentQuestionRepository.findById(questionId).orElseThrow(() -> new TutorException(QUESTION_NOT_FOUND, questionId));
        question.setQuestionStatus(status);

        switch (status) {
            case APPROVED:
            case PENDING:
                question.setRejectionExplanation(" ");
                break;
            case REJECTED:
                break;
        }
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void studentQuestionApproveToAvailable(int questionId) {
        StudentQuestion question = studentQuestionRepository.findById(questionId).orElseThrow(() -> new TutorException(QUESTION_NOT_FOUND, questionId));

        if(question.getQuestionStatus().equals(StudentQuestion.QuestionStatus.APPROVED)) {
            question.setStatus(Question.Status.AVAILABLE);
        }
        else{
            throw new TutorException(ErrorMessage.CANT_MAKE_QUESTION_AVAILABLE);
        }
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void setStudentQuestionExplanation(int questionId, String explanation) {
        StudentQuestion question = studentQuestionRepository.findById(questionId).orElseThrow(() -> new TutorException(QUESTION_NOT_FOUND, questionId));
        checkStatusToAddExplanation(explanation, questionId);
        question.setRejectionExplanation(explanation);
    }


    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<StudentQuestionDto> getStudentQuestions(int courseId, int studentId) {
        User student = userRepository.findById(studentId).orElseThrow(() -> new TutorException(ACCESS_DENIED, studentId));

        return studentQuestionRepository.findAll().stream()
                .filter(studentQuestion -> studentQuestion.getUser() == student && studentQuestion.getCourse().getId() == courseId)
                .map(StudentQuestionDto::new)
                .collect(Collectors.toList());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<StudentQuestionDto> getStudentQuestionsByCourses(Set<CourseExecution> courses, int currentCourseId) {

        return studentQuestionRepository.findAll().stream()
                .filter(studentQuestion -> equalsCourses(studentQuestion.getCourse().getId(), courses, currentCourseId))
                .map(StudentQuestionDto::new)
                .collect(Collectors.toList());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public boolean equalsCourses(int id, Set<CourseExecution> courses, int currentCourseId) {
        return courses.stream().
                anyMatch(course -> course.getCourse().getId() == id && id == currentCourseId);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public int  findNumberStudentQuestionsSubmitted(int studentId, int courseId) {
        User user = userRepository.findById(studentId).orElseThrow(() -> new TutorException(ACCESS_DENIED, studentId));
        checkRoleStudent(user);
        Integer count = studentQuestionRepository.findNumberStudentQuestionsSubmitted(user.getId(), courseId);
        return count;
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public int  findNumberStudentQuestionsApproved(int studentId, int courseId) {
        User user = userRepository.findById(studentId).orElseThrow(() -> new TutorException(ACCESS_DENIED, studentId));
        checkRoleStudent(user);
        Integer count = studentQuestionRepository.findNumberStudentQuestionsApproved(user.getId(), courseId);
        return count;
    }

    private void checkEnrolledCourseExecution(User student, Course course) {
        List<CourseExecution> list = student.getCourseExecutions().stream().filter(
                courseExecution -> courseExecution.getCourse() == course).collect(Collectors.toList());
        if(list.isEmpty()) {
            throw  new TutorException(ACCESS_DENIED);
        }
    }

    private void checkRoleStudent(User student) {
        if(student.getRole() != User.Role.STUDENT){
            throw new TutorException(ACCESS_DENIED);
        }
    }

    private void checkStatusToAddExplanation(String explanation, int questionId) {
        StudentQuestion question = studentQuestionRepository.findById(questionId).orElseThrow(() -> new TutorException(QUESTION_NOT_FOUND, questionId));
        if(question.getQuestionStatus() != StudentQuestion.QuestionStatus.REJECTED && !explanation.equals("No explanation")){
            throw new TutorException(CANT_ADD_EXPLANATION);
        }
    }
}

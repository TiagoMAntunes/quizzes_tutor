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
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
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
        checkQuestionKey(questionDto);
        StudentQuestion studentQuestion = new StudentQuestion(course, questionDto, student);
        studentQuestion.setCreationDate(LocalDateTime.now());
        this.entityManager.persist(studentQuestion);

        return new StudentQuestionDto(studentQuestion);
    }

    @Retryable(
    value = { SQLException.class },
    backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void studentQuestionApproveReject(int questionId, StudentQuestion.QuestionStatus status, String explanation, int teacherId, int courseExecutionId) {
        User teacher = userRepository.findById(teacherId).orElseThrow(() -> new TutorException(ACCESS_DENIED, teacherId));
        CourseExecution execution = courseExecutionRepository.findById(courseExecutionId).orElseThrow(() -> new TutorException(COURSE_EXECUTION_NOT_FOUND, courseExecutionId));
        checkUserRole(teacher);
        StudentQuestion question = studentQuestionRepository.findById(questionId).orElseThrow(() -> new TutorException(QUESTION_NOT_FOUND, questionId));
        checkTeacherCourse(teacher, execution);
        checkStatusToAddExplanation(explanation, status);

        switch (status) {
            case REJECTED:
                question.setQuestionStatus(status);
                question.setRejectionExplanation(explanation);
                break;
            case APPROVED:
            case PENDING:
                question.setQuestionStatus(status);
                break;
        }
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<StudentQuestion> getStudentQuestions(int studentId) {
        User student = userRepository.findById(studentId).orElseThrow(() -> new TutorException(ACCESS_DENIED, studentId));


        List<StudentQuestion> list = studentQuestionRepository.findAll().stream()
                .filter(studentQuestion -> studentQuestion.getUser() == student)
                .collect(Collectors.toList());

        if(list.isEmpty()){
            throw new TutorException(NO_QUESTION_SUBMITTED);
        }else{
            return list;
        }
    }

    private void checkStatusToAddExplanation(String explanation, StudentQuestion.QuestionStatus status) {
        if(explanation != null && status != StudentQuestion.QuestionStatus.REJECTED) {
            throw new TutorException(CANT_ADD_EXPLANATION);
        }
    }

    private void checkQuestionKey(QuestionDto questionDto) {
        if (questionDto.getKey() == null) {
            int maxQuestionNumber = questionRepository.getMaxQuestionNumber() != null ?
                    questionRepository.getMaxQuestionNumber() : 0;
            questionDto.setKey(maxQuestionNumber + 1);
        }
    }

    private void checkTeacherCourse(User teacher, CourseExecution execution) {
        if(!teacher.getCourseExecutions().contains(execution)){
            throw new TutorException(ACCESS_DENIED);
        }
    }

    private void checkUserRole(User teacher) {
        if (teacher.getRole() != User.Role.TEACHER) {
            throw new TutorException(ACCESS_DENIED);
        }
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
}

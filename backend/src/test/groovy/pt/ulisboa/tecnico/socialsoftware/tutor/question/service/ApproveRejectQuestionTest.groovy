package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentQuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import spock.lang.Specification

@DataJpaTest
class ApproveRejectQuestionTest extends Specification {
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String QUESTION_TITLE = "question title"
    public static final String QUESTION_CONTENT = "question content"
    public static final String OPTION_CONTENT = "optionId content"
    public static final String EXPLANATION = "explanation"

    @Autowired
    StudentQuestionService studentQuestionService

    @Autowired
    UserRepository userRepository

    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    StudentQuestionRepository studentQuestionRepository

    def course;
    def teacher;
    def student;
    def questionDto;
    def result
    def courseExecution;

    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)
        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        teacher = new User("User1", "teacher", 1, User.Role.TEACHER)
        userRepository.save(teacher)
        teacher.addCourse(courseExecution)
        student = new User("User2", "student", 2, User.Role.STUDENT)
        userRepository.save(student)
        student.addCourse(courseExecution)

        questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_TITLE)
        questionDto.setContent(QUESTION_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())

        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        questionDto.setOptions(options)
        studentQuestionService.createStudentQuestion(course.getId(), questionDto, student.getId())
    }

    def "a question is approved by the Teacher"() {
        given: "a student question"
        result = studentQuestionRepository.findAll().get(0)

        when:
        studentQuestionService.studentQuestionApproveReject(result.getId(), StudentQuestion.QuestionStatus.APPROVED, null, teacher.getId(), courseExecution.getId())

        then: "the question status will be: approved"
        studentQuestionRepository.count() == 1L
        def result = studentQuestionRepository.findAll().get(0)
        result.getId() != null
        result.getQuestionStatus() == StudentQuestion.QuestionStatus.APPROVED
    }


    def "a question is rejected by the Teacher"() {
        given: "a student question"
        result = studentQuestionRepository.findAll().get(0)

        when:
        studentQuestionService.studentQuestionApproveReject(result.getId(), StudentQuestion.QuestionStatus.REJECTED, null, teacher.getId(), courseExecution.getId())

        then: "the question status will be: rejected"
        studentQuestionRepository.count() == 1L
        def result = studentQuestionRepository.findAll().get(0)
        result.getId() != null
        result.getQuestionStatus() == StudentQuestion.QuestionStatus.REJECTED
    }

    def "an explanation is added for a rejected question"() {
        given: "a rejected question"
        result = studentQuestionRepository.findAll().get(0)
        studentQuestionService.studentQuestionApproveReject(result.getId(), StudentQuestion.QuestionStatus.REJECTED, null, teacher.getId(), courseExecution.getId())

        when:
        result.setRejectionExplanation(EXPLANATION)

        then: "the explanation will have been added"
        studentQuestionRepository.count() == 1L
        result.getId() != null
        result.getQuestionStatus() == StudentQuestion.QuestionStatus.REJECTED
        result.getRejectionExplanation() == EXPLANATION
    }

    def "an explanation is added for an approved question"() {
        given: "a question"
        result = studentQuestionRepository.findAll().get(0)

        when:
        studentQuestionService.studentQuestionApproveReject(result.getId(), StudentQuestion.QuestionStatus.APPROVED, EXPLANATION, teacher.getId(), courseExecution.getId())

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.CANT_ADD_EXPLANATION
        result.getQuestionStatus() == StudentQuestion.QuestionStatus.PENDING
        result.getRejectionExplanation() == null
    }

    def "an explanation is added for a question that has not been aproved or rejected"() {
        given: "a pending question"
        result = studentQuestionRepository.findAll().get(0)

        when:
        studentQuestionService.studentQuestionApproveReject(result.getId(), StudentQuestion.QuestionStatus.PENDING, EXPLANATION, teacher.getId(), courseExecution.getId())

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.CANT_ADD_EXPLANATION
        result.getRejectionExplanation() == null
    }

    def "a student tries to approve a question"() {
        given: "a pending question"
        result = studentQuestionRepository.findAll().get(0)

        when:
        studentQuestionService.studentQuestionApproveReject(result.getId(), StudentQuestion.QuestionStatus.APPROVED, null, student.getId(), courseExecution.getId())

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.ACCESS_DENIED
        result.getQuestionStatus() == StudentQuestion.QuestionStatus.PENDING
    }

    @TestConfiguration
    static class StudentQuestionServiceImplTestContextConfiguration {

        @Bean
        StudentQuestionService studentQuestionService() {
            return new StudentQuestionService()
        }
    }
}

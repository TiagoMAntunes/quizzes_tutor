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
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentQuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

@DataJpaTest
class ApproveBecomesAvailableTest extends Specification{
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String QUESTION_TITLE = "question title"
    public static final String QUESTION_CONTENT = "question content"
    public static final String OPTION_CONTENT = "optionId content"

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
    def student;
    def questionDto;
    def result
    def courseExecution;

    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)
        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

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

    def "a question is approved and then it is made available"(){
        given: "an approved student question"
        result = studentQuestionRepository.findAll().get(0)
        result.setStatus(Question.Status.DISABLED)
        studentQuestionService.studentQuestionApproveReject(result.getId(), StudentQuestion.QuestionStatus.APPROVED)

        when:
        studentQuestionService.studentQuestionApproveToAvailable(result.getId())

        then: "the question is made available"
        studentQuestionRepository.count() == 1L
        def result = studentQuestionRepository.findAll().get(0)
        result.getId() != null
        result.getStatus() == Question.Status.AVAILABLE
    }

    def "a question is rejected and then it is made available"(){
        given: "a rejected student question"
        result = studentQuestionRepository.findAll().get(0)
        result.setStatus(Question.Status.DISABLED)
        studentQuestionService.studentQuestionApproveReject(result.getId(), StudentQuestion.QuestionStatus.REJECTED)

        when:
        studentQuestionService.studentQuestionApproveToAvailable(result.getId())

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.CANT_MAKE_QUESTION_AVAILABLE
        studentQuestionRepository.count() == 1L
        result.getStatus() != Question.Status.AVAILABLE
    }

    def "a question is in the pending status and then it is made available"(){
        given: "a pending student question"
        result = studentQuestionRepository.findAll().get(0)
        result.setStatus(Question.Status.DISABLED)
        studentQuestionService.studentQuestionApproveReject(result.getId(), StudentQuestion.QuestionStatus.PENDING)

        when:
        studentQuestionService.studentQuestionApproveToAvailable(result.getId())

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.CANT_MAKE_QUESTION_AVAILABLE
        studentQuestionRepository.count() == 1L
        result.getStatus() != Question.Status.AVAILABLE
    }

    @TestConfiguration
    static class StudentQuestionServiceImplTestContextConfiguration {

        @Bean
        StudentQuestionService studentQuestionService() {
            return new StudentQuestionService()
        }
    }
}


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
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

import java.time.LocalDateTime

@DataJpaTest
class StudentDashboardQuestionSubmittedTest extends Specification {
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String QUESTION_TITLE = 'question title'
    public static final String QUESTION_CONTENT = 'question content'
    public static final String OPTION_CONTENT = "optionId content"
    public static final String USER_NAME = "afonso"
    public static final String USER_USERNAME = "nof"
    public static final String USER_NAME2 = "pedro"
    public static final String USER_USERNAME2 = "lamegow"
    public static final String TEACHER_NAME = "joana"
    public static final String TEACHER_USERNAME = "jojo"


    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    StudentQuestionRepository studentQuestionRepository

    @Autowired
    UserRepository userRepository

    @Autowired
    StudentQuestionService studentQuestionService

    def course
    def courseExecution

    def student
    def student2
    def teacher

    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        student = new User(USER_NAME, USER_USERNAME, 1, User.Role.STUDENT)
        userRepository.save(student)

        student2 = new User(USER_NAME2, USER_USERNAME2, 2, User.Role.STUDENT)
        userRepository.save(student2)

        courseExecution.addUser(student)
        student.addCourse(courseExecution)

        teacher = new User(TEACHER_NAME, TEACHER_USERNAME, 3, User.Role.TEACHER)
        userRepository.save(teacher)
    }

    def "find the number of questions submitted greater than zero"() {
        given:"A question"
        def questionDto = new QuestionDto()
        questionDto.setTitle(QUESTION_TITLE)
        questionDto.setContent(QUESTION_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        and: 'a optionId'
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        questionDto.setOptions(options)
        def studentQuestion = new StudentQuestion(course, questionDto, student)
        studentQuestionRepository.save(studentQuestion)
        def studentQuestion2 = new StudentQuestion(course, questionDto, student2)
        studentQuestionRepository.save(studentQuestion2)
        when:
        def result = studentQuestionService.findNumberStudentQuestionsSubmitted(student.getId());

        then:
        result == 1
    }

    def "find the number of questions submitted equals zero"() {
        when:
        def result = studentQuestionService.findNumberStudentQuestionsSubmitted(student2.getId());

        then:
        result == 0

    }

    def "teacher try to find the number of questions submitted"() {
        given:

        when:
        studentQuestionService.findNumberStudentQuestionsSubmitted(teacher.getId());
        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.ACCESS_DENIED
    }

    @TestConfiguration
    static class StudentQuestionServiceImplTestContextConfiguration {

        @Bean
        StudentQuestionService studentQuestionService() {
            return new StudentQuestionService()
        }
    }
}

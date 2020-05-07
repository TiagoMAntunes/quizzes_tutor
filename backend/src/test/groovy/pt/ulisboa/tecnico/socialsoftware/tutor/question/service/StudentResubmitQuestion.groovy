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
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.OptionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

@DataJpaTest
class StudentResubmitQuestion extends Specification {
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String QUESTION_TITLE = 'question title'
    public static final String QUESTION_CONTENT = 'question content'
    public static final String OPTION_CONTENT = "optionId content"
    public static final String QUESTION_TITLE_CHANGED = 'changed'
    public static final String QUESTION_CONTENT_CHANGED = 'changed'
    public static final String OPTION_CONTENT_CHANGED1 = "changed1"
    public static final String USER_NAME = "pedro"
    public static final String USER_USERNAME = "lamegow"

    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    StudentQuestionRepository studentQuestionRepository

    @Autowired
    UserRepository userRepository

    @Autowired
    OptionRepository optionRepository

    @Autowired
    StudentQuestionService studentQuestionService

    def course
    def courseExecution
    def student
    def studentQuestion
    def optionOK
    def questionDto
    def options
    def optionDto

    def setup() {

        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        student = new User(USER_NAME, USER_USERNAME, 1, User.Role.STUDENT)
        userRepository.save(student)

        courseExecution.addUser(student)
        student.addCourse(courseExecution)

        studentQuestion = new StudentQuestion()
        studentQuestion.setUser(student)
        studentQuestion.setTitle(QUESTION_TITLE)
        studentQuestion.setContent(QUESTION_CONTENT)
        and: 'two options'
        optionOK = new Option()
        optionOK.setContent(OPTION_CONTENT)
        optionOK.setCorrect(true)
        optionOK.setSequence(0)
        optionOK.setQuestion(studentQuestion)
        optionRepository.save(optionOK)
        studentQuestionRepository.save(studentQuestion)

        questionDto = new QuestionDto(studentQuestion)
        questionDto.setTitle(QUESTION_TITLE_CHANGED)
        questionDto.setContent(QUESTION_CONTENT_CHANGED)
        options = new ArrayList<OptionDto>()
        optionDto = new OptionDto(optionOK)
        optionDto.setContent(OPTION_CONTENT_CHANGED1)
        optionDto.setCorrect(true)
        options.add(optionDto)
        questionDto.setOptions(options)
    }

    def "student resubmit a pending question"() {
        when: studentQuestionService.resubmitRejectedStudentQuestion(studentQuestion.getId(), questionDto)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.QUESTION_NOT_REJECTED
        studentQuestionRepository.count() == 1L

    }

    def "student resubmit a rejected question"() {
        given:
        def studentQuestionChanged = studentQuestionRepository.findAll().get(0)
        studentQuestionChanged.setQuestionStatus(StudentQuestion.QuestionStatus.APPROVED)

        when: studentQuestionService.resubmitRejectedStudentQuestion(studentQuestion.getId(), questionDto)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.QUESTION_NOT_REJECTED
        studentQuestionRepository.count() == 1L
    }

    def "student resubmit a approved question"() {
        given:
        def studentQuestionChanged = studentQuestionRepository.findAll().get(0)
        studentQuestionChanged.setQuestionStatus(StudentQuestion.QuestionStatus.REJECTED)

        when: studentQuestionService.resubmitRejectedStudentQuestion(studentQuestion.getId(), questionDto)

        then:"the student question is changed"
        studentQuestionRepository.count() == 1L
        def result = studentQuestionRepository.findAll().get(0)
        result.getId() != null
        result.getTitle() == QUESTION_TITLE_CHANGED
        result.getContent() == QUESTION_CONTENT_CHANGED
        result.getImage() == null
        result.getQuestionStatus() == StudentQuestion.QuestionStatus.PENDING
        result.getOptions().size() == 1
        def resOption1 = result.getOptions().get(0)
        resOption1.getContent() == OPTION_CONTENT_CHANGED1
        resOption1.getCorrect()

    }

    @TestConfiguration
    static class StudentQuestionServiceImplTestContextConfiguration {

        @Bean
        StudentQuestionService studentQuestionService() {
            return new StudentQuestionService()
        }
    }

}

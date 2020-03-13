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
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ImageDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification
import spock.lang.Unroll

import javax.persistence.EntityManager

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*

@DataJpaTest
class CreateStudentQuestionTest extends Specification {
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String QUESTION_TITLE = 'question title'
    public static final String QUESTION_CONTENT = 'question content'
    public static final String OPTION_CONTENT = "optionId content"
    public static final String URL = 'URL'
    public static final String EMPTY = "    "
    public static final String BLANK = ""
    public static final String USER_NAME = "pedro"
    public static final String USER_USERNAME = "lamegow"
    public static final String USER_NAME2 = "tiago"
    public static final String USER_USERNAME2 = "riju"
    public static final String TEACHER_NAME = "maria"
    public static final String TEACHER_USERNAME = "appolle"

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

    @Autowired
    TopicRepository topicRepository

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

        courseExecution.addUser(student)
        student.addCourse(courseExecution)

        student2 = new User(USER_NAME2, USER_USERNAME2, 2, User.Role.STUDENT)
        userRepository.save(student2)

        teacher = new User(TEACHER_NAME, TEACHER_USERNAME, 3, User.Role.TEACHER)
        userRepository.save(teacher)

    }

    def "create a question with no image and one option"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
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

        when:
        studentQuestionService.createStudentQuestion(course.getId(), questionDto, student.getId())

        then: "the correct question is inside the repository"
        studentQuestionRepository.count() == 1L
        def result = studentQuestionRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
        result.getStatus() == Question.Status.AVAILABLE
        result.getTitle() == QUESTION_TITLE
        result.getContent() == QUESTION_CONTENT
        result.getImage() == null
        result.getOptions().size() == 1
        result.getCourse().getName() == COURSE_NAME
        course.getQuestions().contains(result)
        def resOption = result.getOptions().get(0)
        resOption.getContent() == OPTION_CONTENT
        resOption.getCorrect()

    }

    def "not a student"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
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

        when:
        studentQuestionService.createStudentQuestion(course.getId(), questionDto, teacher.getId())

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.ACCESS_DENIED
        studentQuestionRepository.count() == 0L

    }

    def "a student isn't in a course"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
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

        when:
        studentQuestionService.createStudentQuestion(course.getId(), questionDto, student2.getId())

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.ACCESS_DENIED
        studentQuestionRepository.count() == 0L
    }


    @Unroll("invalid arguments:  #Title | #Content | #Option || errorMessage")
    def "invalid input values"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(Title)
        questionDto.setContent(Content)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        and: 'a optionId'
        def optionDto = new OptionDto()
        optionDto.setContent(Option)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        questionDto.setOptions(options)

        when:
        studentQuestionService.createStudentQuestion(course.getId(), questionDto, student.getId())

        then: "a StudentQuestion Exception"
        def error = thrown(TutorException)
        error.errorMessage == errorMessage
        studentQuestionRepository.count() == 0L

        where:
        Title             | Content            | Option             || errorMessage
        BLANK             | QUESTION_CONTENT   | OPTION_CONTENT     || QUESTION_MISSING_DATA
        EMPTY             | QUESTION_CONTENT   | OPTION_CONTENT     || QUESTION_MISSING_DATA
        QUESTION_TITLE    | BLANK              | OPTION_CONTENT     || QUESTION_MISSING_DATA
        QUESTION_TITLE    | EMPTY              | OPTION_CONTENT     || QUESTION_MISSING_DATA
        QUESTION_TITLE    | QUESTION_CONTENT   | BLANK              || QUESTION_MISSING_DATA
        QUESTION_TITLE    | QUESTION_CONTENT   | EMPTY              || QUESTION_MISSING_DATA
    }


    def "create a question with image and two options"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_TITLE)
        questionDto.setContent(QUESTION_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())

        and: 'an image'
        def image = new ImageDto()
        image.setUrl(URL)
        image.setWidth(20)
        questionDto.setImage(image)
        and: 'two options'
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        optionDto = new OptionDto()
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(false)
        options.add(optionDto)
        questionDto.setOptions(options)

        when:
        studentQuestionService.createStudentQuestion(course.getId(), questionDto, student.getId())

        then: "the correct question is inside the repository"
        studentQuestionRepository.count() == 1L
        def result = studentQuestionRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
        result.getStatus() == Question.Status.AVAILABLE
        result.getTitle() == QUESTION_TITLE
        result.getContent() == QUESTION_CONTENT
        result.getImage().getId() != null
        result.getImage().getUrl() == URL
        result.getImage().getWidth() == 20
        result.getOptions().size() == 2
    }

    def "create two questions"() {
        given: "a questionDto"
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

        when: 'are created two questions'
        studentQuestionService.createStudentQuestion(course.getId(), questionDto, student.getId())
        questionDto.setKey(null)
        studentQuestionService.createStudentQuestion(course.getId(), questionDto, student.getId())

        then: "the two questions are created with the correct numbers"
        studentQuestionRepository.count() == 2L
        def resultOne = studentQuestionRepository.findAll().get(0)
        def resultTwo = studentQuestionRepository.findAll().get(1)
        resultOne.getKey() + resultTwo.getKey() == 3
    }

    @TestConfiguration
    static class StudentQuestionServiceImplTestContextConfiguration {

        @Bean
        StudentQuestionService studentQuestionService() {
            return new StudentQuestionService()
        }
    }
}
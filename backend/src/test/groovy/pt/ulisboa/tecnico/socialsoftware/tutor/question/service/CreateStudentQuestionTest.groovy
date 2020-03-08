package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentQuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import spock.lang.Specification

@DataJpaTest
class StudentCreateSubmitQuestionTest extends Specification {

    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "SA"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String EMPTY = ""
    public static final String BLANK = "   "
    public static final String QUESTION_TITLE = "question title"
    public static final String QUESTION_CONTENT = "question content"
    public static final String OPTION_CONTENT = "optionId content"
    public static final String URL = "URL"
    public static final String USER_NAME = "Pedro"
    public static final String USER_USERNAME = "lamegow"

    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    QuestionRepository questionRepository


    def studentQuestionService

    def setup() {
        studentQuestionService = new StudentQuestionService()
    }

    def "the course and topic exist and title and body of text is valid and the user is a student without Image"() {
        given: "a course"
        def course = new Course(COURSE_NAME, Course.Type.TECNICO)
        and: "a courseDto"
        def courseDto = new CourseDto()
        courseDto.setName(COURSE_NAME)
        courseDto.setAcronym(ACRONYM)
        courseDto.setAcademicTerm(ACADEMIC_TERM)
        and: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_TITLE)
        questionDto.setContent(QUESTION_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        and: "a optionId"
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        questionDto.setOptions(options)
        and: "a user"
        def user = new User()
        /*user.setKey(1)
        user.setName() = USER_USERNAME
        user.setUsername() = USER_USERNAME
        user.setRole() = STUDENT*/

        when:
        studentQuestionService.createStudentQuestion(course, questionDto, user)

        then: "the returned data are correct"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
        result.getStatus() == Question.Status.AVAILABLE
        result.getTitle() == QUESTION_TITLE
        result.getContent() == QUESTION_CONTENT
        result.getImage() == null
        result.getOptions().size() == 1
        result.getCourse().getName() == COURSE_NAME
        course.getQuestions().contains(result)
        /*result.getUserName() == USER_NAME
        result.getUserUsername() == USER_USERNAME
        result.getUserRole() == STUDENT*/
        def resOption = result.getOptions().get(0)
        resOption.getContent() == OPTION_CONTENT
        resOption.getCorrect()
    }

    def "the course and topic exist and title and body of text is valid and the user is a student with Image"() {  //falta acrescentar a parte da imagem
        given: "a course"
        def course = new Course(COURSE_NAME, Course.Type.TECNICO)
        and: "a courseDto"
        def courseDto = new CourseDto()
        courseDto.setName(COURSE_NAME)
        courseDto.setAcronym(ACRONYM)
        courseDto.setAcademicTerm(ACADEMIC_TERM)
        and: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_TITLE)
        questionDto.setContent(QUESTION_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        and: "a optionId"
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        questionDto.setOptions(options)
        and: "a user"
        def user = new User()
        /*user.setKey(1)
        user.setName() = USER_USERNAME
        user.setUsername() = USER_USERNAME
        user.setRole() = STUDENT*/

        when:
        studentQuestionService.createStudentQuestion(course, questionDto, user)

        then: "the returned data are correct"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
        result.getStatus() == Question.Status.AVAILABLE
        result.getTitle() == QUESTION_TITLE
        result.getContent() == QUESTION_CONTENT
        result.getImage() == null
        result.getOptions().size() == 1
        result.getCourse().getName() == COURSE_NAME
        course.getQuestions().contains(result)
        /*result.getUserName() == USER_NAME
        result.getUserUsername() == USER_USERNAME
        result.getUserRole() == STUDENT*/
        def resOption = result.getOptions().get(0)
        resOption.getContent() == OPTION_CONTENT
        resOption.getCorrect()
    }

    def "not a student"() {
        given: "a course"
        def course = new Course(COURSE_NAME, Course.Type.TECNICO)
        and: "a courseDto"
        def courseDto = new CourseDto()
        courseDto.setName(COURSE_NAME)
        courseDto.setAcronym(ACRONYM)
        courseDto.setAcademicTerm(ACADEMIC_TERM)
        and: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_TITLE)
        questionDto.setContent(QUESTION_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        and: "a optionId"
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        questionDto.setOptions(options)
        and: "a user"
        def user = new User()
        /*user.setKey(1)
        user.setName() = USER_USERNAME
        user.setUsername() = USER_USERNAME
        user.setRole() = TEACHER*/

        when:
        studentQuestionService.createStudentQuestion(course, questionDto, user)

        then:
        thrown(TutorException)
    }

    def "title already exists"() {
        given: "a course"
        def course = new Course(COURSE_NAME, Course.Type.TECNICO)
        and: "a courseDto"
        def courseDto = new CourseDto()
        courseDto.setName(COURSE_NAME)
        courseDto.setAcronym(ACRONYM)
        courseDto.setAcademicTerm(ACADEMIC_TERM)
        and: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_TITLE)
        questionDto.setContent(QUESTION_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        and: "a optionId"
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        questionDto.setOptions(options)
        and: "a user"
        def user = new User()
        /*user.setKey(1)
        user.setName() = USER_USERNAME
        user.setUsername() = USER_USERNAME
        user.setRole() = STUDENT*/


        when:
        studentQuestionService.createStudentQuestion(course, questionDto, user)

        then:
        thrown(TutorException)
    }

    def "title is blank"() {
        given: "a course"
        def course = new Course(COURSE_NAME, Course.Type.TECNICO)
        and: "a courseDto"
        def courseDto = new CourseDto()
        courseDto.setName(COURSE_NAME)
        courseDto.setAcronym(ACRONYM)
        courseDto.setAcademicTerm(ACADEMIC_TERM)
        and: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(BLANK)
        questionDto.setContent(QUESTION_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        and: "a optionId"
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        questionDto.setOptions(options)
        and: "a user"
        def user = new User()
        /*user.setKey(1)
        user.setName() = USER_USERNAME
        user.setUsername() = USER_USERNAME
        user.setRole() = STUDENT*/


        when:
        studentQuestionService.createStudentQuestion(course, questionDto, user)

        then:
        thrown(TutorException)
    }

    def "title is empty"() {
        given: "a course"
        def course = new Course(COURSE_NAME, Course.Type.TECNICO)
        and: "a courseDto"
        def courseDto = new CourseDto()
        courseDto.setName(COURSE_NAME)
        courseDto.setAcronym(ACRONYM)
        courseDto.setAcademicTerm(ACADEMIC_TERM)
        and: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(EMPTY)
        questionDto.setContent(QUESTION_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        and: "a optionId"
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        questionDto.setOptions(options)
        and: "a user"
        def user = new User()
        /*user.setKey(1)
        user.setName() = USER_USERNAME
        user.setUsername() = USER_USERNAME
        user.setRole() = STUDENT*/

        when:
        studentQuestionService.createStudentQuestion(course, questionDto, user)

        then:
        thrown(TutorException)
    }

    def "content is blank"() {
        given: "a course"
        def course = new Course(COURSE_NAME, Course.Type.TECNICO)
        and: "a courseDto"
        def courseDto = new CourseDto()
        courseDto.setName(COURSE_NAME)
        courseDto.setAcronym(ACRONYM)
        courseDto.setAcademicTerm(ACADEMIC_TERM)
        and: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_TITLE)
        questionDto.setContent(BLANK)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        and: "a optionId"
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        questionDto.setOptions(options)
        and: "a user"
        def user = new User()
        /*user.setKey(1)
        user.setName() = USER_USERNAME
        user.setUsername() = USER_USERNAME
        user.setRole() = STUDENT*/

        when:
        studentQuestionService.createStudentQuestion(course, questionDto, user)

        then:
        thrown(TutorException)
    }

    def "content is empty"() {
        given: "a course"
        def course = new Course(COURSE_NAME, Course.Type.TECNICO)
        and: "a courseDto"
        def courseDto = new CourseDto()
        courseDto.setName(COURSE_NAME)
        courseDto.setAcronym(ACRONYM)
        courseDto.setAcademicTerm(ACADEMIC_TERM)
        and: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_TITLE)
        questionDto.setContent(EMPTY)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        and: "a optionId"
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        questionDto.setOptions(options)
        and: "a user"
        def user = new User()
        /*user.setKey(1)
        user.setName() = USER_USERNAME
        user.setUsername() = USER_USERNAME
        user.setRole() = STUDENT*/

        when:
        studentQuestionService.createStudentQuestion(course, questionDto, user)

        then:
        thrown(TutorException)
    }

    def "course is empty"() {
        given: "a course"
        def course = new Course(EMPTY, Course.Type.TECNICO)
        and: "a courseDto"
        def courseDto = new CourseDto()
        courseDto.setName(EMPTY)
        courseDto.setAcronym(ACRONYM)
        courseDto.setAcademicTerm(ACADEMIC_TERM)
        and: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_TITLE)
        questionDto.setContent(QUESTION_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        and: "a optionId"
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        questionDto.setOptions(options)
        and: "a user"
        def user = new User()
        /*user.setKey(1)
        user.setName() = USER_USERNAME
        user.setUsername() = USER_USERNAME
        user.setRole() = STUDENT*/

        when:
        studentQuestionService.createStudentQuestion(course, questionDto, user)

        then:
        thrown(TutorException)
    }

    def "course is blank"() {
        given: "a course"
        def course = new Course(BLANK, Course.Type.TECNICO)
        and: "a courseDto"
        def courseDto = new CourseDto()
        courseDto.setName(BLANK)
        courseDto.setAcronym(ACRONYM)
        courseDto.setAcademicTerm(ACADEMIC_TERM)
        and: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_TITLE)
        questionDto.setContent(QUESTION_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        and: "a optionId"
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        questionDto.setOptions(options)
        and: "a user"
        def user = new User()
        /*user.setKey(1)
        user.setName() = USER_USERNAME
        user.setUsername() = USER_USERNAME
        user.setRole() = STUDENT*/

        when:
        studentQuestionService.createStudentQuestion(course, questionDto, user)

        then:
        thrown(TutorException)
    }

    def "course doesnt exist"() {
        given: "a course"
        def course = new Course(COURSE_NAME, Course.Type.TECNICO)
        and: "a courseDto"
        def courseDto = new CourseDto()
        courseDto.setName(COURSE_NAME)
        courseDto.setAcronym(ACRONYM)
        courseDto.setAcademicTerm(ACADEMIC_TERM)
        and: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_TITLE)
        questionDto.setContent(QUESTION_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        and: "a optionId"
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        questionDto.setOptions(options)
        and: "a user"
        def user = new User()
        /*user.setKey(1)
        user.setName() = USER_USERNAME
        user.setUsername() = USER_USERNAME
        user.setRole() = STUDENT*/

        when:
        studentQuestionService.createStudentQuestion(course, questionDto, user)

        then:
        thrown(TutorException)
    }

    def "topic is empty"() {
        given: "a course"
        def course = new Course(COURSE_NAME, Course.Type.TECNICO)
        and: "a courseDto"
        def courseDto = new CourseDto()
        courseDto.setName(COURSE_NAME)
        courseDto.setAcronym(ACRONYM)
        courseDto.setAcademicTerm(ACADEMIC_TERM)
        and: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_TITLE)
        questionDto.setContent(QUESTION_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        and: "a optionId"
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        questionDto.setOptions(options)
        and: "a user"
        def user = new User()
        /*user.setKey(1)
        user.setName() = USER_USERNAME
        user.setUsername() = USER_USERNAME
        user.setRole() = STUDENT*/

        when:
        studentQuestionService.createStudentQuestion(course, questionDto, user)

        then:
        thrown(TutorException)
    }

    def "topic is blank"() {
        given: "a course"
        def course = new Course(COURSE_NAME, Course.Type.TECNICO)
        and: "a courseDto"
        def courseDto = new CourseDto()
        courseDto.setName(COURSE_NAME)
        courseDto.setAcronym(ACRONYM)
        courseDto.setAcademicTerm(ACADEMIC_TERM)
        and: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_TITLE)
        questionDto.setContent(QUESTION_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        and: "a optionId"
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        questionDto.setOptions(options)
        and: "a user"
        def user = new User()
        /*user.setKey(1)
        user.setName() = USER_USERNAME
        user.setUsername() = USER_USERNAME
        user.setRole() = STUDENT*/

        when:
        studentQuestionService.createStudentQuestion(course, questionDto, user)

        then:
        thrown(TutorException)
    }

    def "topic doesnt exist"() {
        given: "a course"
        def course = new Course(COURSE_NAME, Course.Type.TECNICO)
        and: "a courseDto"
        def courseDto = new CourseDto()
        courseDto.setName(COURSE_NAME)
        courseDto.setAcronym(ACRONYM)
        courseDto.setAcademicTerm(ACADEMIC_TERM)
        and: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_TITLE)
        questionDto.setContent(QUESTION_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        and: "a optionId"
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        questionDto.setOptions(options)
        and: "a user"
        def user = new User()
        /*user.setKey(1)
        user.setName() = USER_USERNAME
        user.setUsername() = USER_USERNAME
        user.setRole() = STUDENT*/

        when:
        studentQuestionService.createStudentQuestion(course, questionDto, user)

        then:
        thrown(TutorException)
    }
}
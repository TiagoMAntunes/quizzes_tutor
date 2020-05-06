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
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.OptionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto
import spock.lang.Specification


@DataJpaTest
class UpdateStudentQuestionTest extends Specification{
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String QUESTION_TITLE = "question title"
    public static final String QUESTION_CONTENT = "question content"
    public static final String OPTION_CONTENT = "optionId content"
    public static final String NEW_QUESTION_TITLE = 'new question title'
    public static final String NEW_QUESTION_CONTENT = 'new question content'
    public static final String NEW_OPTION_CONTENT = "new optionId content"

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

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    OptionRepository optionRepository

    def course;
    def student;
    def questionDto;
    def studentQuestion;
    def userDto;
    def courseExecution;
    def optionOK;
    def optionKO;

    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)
        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        student = new User("User2", "student", 2, User.Role.STUDENT)
        userRepository.save(student)
        userDto = new UserDto(student);
        student.addCourse(courseExecution)

        studentQuestion = new StudentQuestion()
        studentQuestion.setKey(1)
        studentQuestion.setTitle(QUESTION_TITLE)
        studentQuestion.setContent(QUESTION_CONTENT)
        studentQuestion.setStatus(Question.Status.AVAILABLE)
        studentQuestion.setNumberOfAnswers(2)
        studentQuestion.setNumberOfCorrect(1)
        studentQuestion.setUser(student)

        questionDto = new QuestionDto(studentQuestion)
        questionDto.setTitle(QUESTION_TITLE)
        questionDto.setContent(QUESTION_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())

        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        questionDto.setOptions(options)

        optionOK = new Option()
        optionOK.setContent(OPTION_CONTENT)
        optionOK.setCorrect(true)
        optionOK.setSequence(0)
        System.out.println(questionDto.getId())
        optionOK.setQuestion(studentQuestion)
        optionRepository.save(optionOK)
        optionKO = new Option()
        optionKO.setContent(OPTION_CONTENT)
        optionKO.setCorrect(false)
        optionKO.setSequence(1)
        optionKO.setQuestion(studentQuestion)
        optionRepository.save(optionKO)
        questionRepository.save(studentQuestion)
    }

    def "update a question when it is not available"(){
        given: "a student question"
        studentQuestion.setStatus(Question.Status.DISABLED)
        def studentQuestionDto = new StudentQuestionDto(studentQuestion)
        studentQuestionDto.setTitle(NEW_QUESTION_TITLE)
        studentQuestionDto.setContent(NEW_QUESTION_CONTENT)
        and: '2 changed options'
        def options = new ArrayList<OptionDto>()
        def optionDto = new OptionDto(optionOK)
        optionDto.setContent(NEW_OPTION_CONTENT)
        optionDto.setCorrect(false)
        options.add(optionDto)
        optionDto = new OptionDto(optionKO)
        optionDto.setCorrect(true)
        options.add(optionDto)
        studentQuestionDto.setOptions(options)

        when:
        studentQuestionService.updateStudentQuestion(studentQuestionDto.getId(), studentQuestionDto)

        then: "the question is changed"
        studentQuestionRepository.count() == 1L
        def result = studentQuestionRepository.findAll().get(0)
        result.getId() == studentQuestionDto.getId()
        result.getTitle() == NEW_QUESTION_TITLE
        result.getContent() == NEW_QUESTION_CONTENT
        and: 'are not changed'
        result.getStatus() == Question.Status.DISABLED
        result.getNumberOfAnswers() == 2
        result.getNumberOfCorrect() == 1
        result.getDifficulty() == 50
        and: 'an option is changed'
        result.getOptions().size() == 2
        def resOptionOne = result.getOptions().stream().filter({option -> option.getId() == optionOK.getId()}).findAny().orElse(null)
        resOptionOne.getContent() == NEW_OPTION_CONTENT
        !resOptionOne.getCorrect()
        def resOptionTwo = result.getOptions().stream().filter({option -> option.getId() == optionKO.getId()}).findAny().orElse(null)
        resOptionTwo.getContent() == OPTION_CONTENT
        resOptionTwo.getCorrect()
    }

    def "update a question when it is already available"(){
        given: "a student question"
        studentQuestion.setStatus(Question.Status.AVAILABLE)
        def studentQuestionDto = new StudentQuestionDto(studentQuestion)
        studentQuestionDto.setTitle(NEW_QUESTION_TITLE)
        studentQuestionDto.setContent(NEW_QUESTION_CONTENT)
        and: '2 changed options'
        def options = new ArrayList<OptionDto>()
        def optionDto = new OptionDto(optionOK)
        optionDto.setContent(NEW_OPTION_CONTENT)
        optionDto.setCorrect(false)
        options.add(optionDto)
        optionDto = new OptionDto(optionKO)
        optionDto.setCorrect(true)
        options.add(optionDto)
        studentQuestionDto.setOptions(options)

        when:
        studentQuestionService.updateStudentQuestion(studentQuestionDto.getId(), studentQuestionDto)

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.CANNOT_CHANGE_ANSWERED_QUESTION

    }

    def "update a question with missing data"(){
        given: "a question"
        studentQuestion.setStatus(Question.Status.DISABLED)
        def studentQuestionDto = new StudentQuestionDto(studentQuestion)
        studentQuestionDto.setTitle('     ')

        when:
        studentQuestionService.updateStudentQuestion(studentQuestionDto.getId(), studentQuestionDto)

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.INVALID_TITLE_FOR_QUESTION
    }

    @TestConfiguration
    static class StudentQuestionServiceImplTestContextConfiguration {

        @Bean
        StudentQuestionService studentQuestionService() {
            return new StudentQuestionService()
        }
    }
}
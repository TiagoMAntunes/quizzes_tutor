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
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
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
    QuestionService questionService

    @AutoWired
    UserRepository userRepository

    @AutoWired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    QuestionRepository questionRepository

    public enum Role {STUDENT, TEACHER, ADMIN, DEMO_ADMIN}

    def setup() {
        def user = new User()
        user.setRole(Role.TEACHER)
        userRepository.save(user)

        def course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)
    }

    def "a question is approved by the Teacher"(){
        given: "a StudentquestionDto"
        def StudentquestionDto = new StudentquestionDto()
        
        StudentquestionDto.setTitle(QUESTION_TITLE)
        StudentquestionDto.setContent(QUESTION_CONTENT)
        StudentquestionDto.setStatus(StudentQuestion.Status.AVAILABLE.name())
        
        and: "a optionDto"
        def optionDto = new OptionDto()
        
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        StudentquestionDto.setOptions(options)
        questionService.createQuestion(course.getId(), StudentquestionDto)

        when:
        user.approveQuestion(StudentquestionDto) //has to be defined

        then: "the question status will be: approved"
        questionRepository.count() = 1L 
        def result = questionRepository.findAll().get(0)
        result.getId() != null
        result.getStatus() == APPROVED
    }
    

    def "a question is rejected by the Teacher"(){
        given: "a StudentquestionDto"
        def StudentquestionDto = new StudentquestionDto()
        
        StudentquestionDto.setTitle(QUESTION_TITLE)
        StudentquestionDto.setContent(QUESTION_CONTENT)
        StudentquestionDto.setStatus(StudentQuestion.Status.AVAILABLE.name())
        
        and: "an optionDto"
        def optionDto = new OptionDto()
        
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        StudentquestionDto.setOptions(options)
        questionService.createQuestion(course.getId(), StudentquestionDto)

        when:
        user.rejectQuestion(StudentquestionDto) //has to be defined

        then: "the question status will be: rejected"
        questionRepository.count() = 1L 
        def result = questionRepository.findAll().get(0)
        result.getId() != null
        result.getStatus() == REJECTED
    }

    def "an explanation is added for a rejected question"(){
        given: "a StudentquestionDto"
        def StudentquestionDto = new StudentquestionDto()
        
        StudentquestionDto.setTitle(QUESTION_TITLE)
        StudentquestionDto.setContent(QUESTION_CONTENT)
        StudentquestionDto.setStatus(StudentQuestion.Status.AVAILABLE.name())
        
        and: "an optionDto"
        def optionDto = new OptionDto()
        
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        StudentquestionDto.setOptions(options)
        questionService.createQuestion(course.getId(), StudentquestionDto)
        
        and: "a rejected question"
        user.rejectQuestion(StudentquestionDto) //has to be defined

        when:
        StudentquestionDto.addExplanation(user, EXPLANATION)

        then: "the explanation will have been added"
        questionRepository.count() = 1L 
        def result = questionRepository.findAll().get(0)
        result.getId() != null
        result.getStatus() == REJECTED
        result.getExplanation() == EXPLANATION
    }

    def "an explanation is added for an approved question"(){
        given: "a StudentquestionDto"
        def StudentquestionDto = new StudentquestionDto()
        
        StudentquestionDto.setTitle(QUESTION_TITLE)
        StudentquestionDto.setContent(QUESTION_CONTENT)
        StudentquestionDto.setStatus(StudentQuestion.Status.AVAILABLE.name())
        
        and: "an optionDto"
        def optionDto = new OptionDto()
        
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        StudentquestionDto.setOptions(options)
        questionService.createQuestion(course.getId(), StudentquestionDto)
        
        and: "an approved question"
        user.approveQuestion(StudentquestionDto) //has to be defined

        when:
        StudentquestionDto.addExplanation(user, EXPLANATION)

        then: "an exception is thrown"
        def exception = thrown(QuestionException)
        exception.getErrorMessage() == ErrorMessage.CANT_ADD_EXPLANATION
        questionRepository.count() = 1L 
    }

    def "an explanation is added for a question that has not been aproved or rejected"(){
        given: "a StudentquestionDto"
        def StudentquestionDto = new StudentquestionDto()
        
        StudentquestionDto.setTitle(QUESTION_TITLE)
        StudentquestionDto.setContent(QUESTION_CONTENT)
        StudentquestionDto.setStatus(StudentQuestion.Status.AVAILABLE.name())
        
        and: "an optionDto"
        def optionDto = new OptionDto()
        
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        StudentquestionDto.setOptions(options)
        questionService.createQuestion(course.getId(), StudentquestionDto)

        when:
        StudentquestionDto.addExplanation(user, EXPLANATION)

        then: "an exception is thrown"
        def exception = thrown(QuestionException)
        exception.getErrorMessage() == ErrorMessage.CANT_ADD_EXPLANATION
        questionRepository.count() = 1L 
        expect: false;
    }

    @TestConfiguration
    static class QuestionServiceImplTestContextConfiguration {

        @Bean
        QuestionService questionService() {
            return new QuestionService()
        }
    }
}

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
class getQuestionsStatusTest extends Specification {
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
        user.setRole(Role.STUDENT)
        userRepository.save(user)

        def teacher = new User()
        teacher.setRole(Role.TEACHER)
        teacherRepository.save(teacher)
    }

    def "no questions submitted"(){
        given: "a student that hasn't submitted questions"
        //checkar se pode ficar sem nada

        when:
        def result = user.getStudentQuestions()

        then: "an exception is thrown"
        result.size() == 0
        def exception = thrown(QuestionException) // isto nao existe ainda
        exception.getErrorMessage() == ErrorMessage.NO_QUESTION_SUBMITTED
    }

    def "get status of question that is waiting for approval"(){
        given: "a student that has submitted a question"
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
        def result = user.getStudentQuestions()

        then: 
        result.size() == 1
        result.get(0).getStatus() == AVAILABLE
        result.get(0).getId() != null
        result.get(0).getCreator() == user.getId()
    }

    def "get status of an approved question"(){
        given: "a student that has submitted a question"
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
        teacher.approveQuestion(StudentquestionDto) //has to be defined
        
        when:
        def result = user.getStudentQuestions()

        then: 
        result.size() == 1
        result.get(0).getStatus() == AVAILABLE
        result.get(0).getId() != null
        result.get(0).getCreator() == user.getId()
    }

    def "get status of a rejected question with no explanation"(){
        given: "a student that has submitted a question"
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
        teacher.rejectQuestion(StudentquestionDto) //has to be defined
        
        when:
        def result = user.getStudentQuestions()

        then: 
        result.size() == 1
        result.get(0).getStatus() == REJECTED
        result.get(0).getId() != null
        result.get(0).getCreator() == user.getId()
        result.get(0).getExplanation() == null
    }

    def "get status of a rejected question with explanation"(){
        given: "a student that has submitted a question"
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
        teacher.rejectQuestion(StudentquestionDto) //has to be defined
        
        when:
        def result = user.getStudentQuestions()

        then: 
        result.size() == 1
        result.get(0).getStatus() == REJECTED
        result.get(0).getId() != null
        result.get(0).getCreator() == user.getId()
        result.get(0).getExplanation() == EXPLANATION
    }

    def "get status of two submitted questions: one approved question and one rejected with no explanation"(){
        def StudentquestionDto = new StudentquestionDto()
        StudentquestionDto.setTitle(QUESTION_TITLE)
        StudentquestionDto.setContent(QUESTION_CONTENT)
        StudentquestionDto.setStatus(StudentQuestion.Status.AVAILABLE.name())
        def StudentquestionDto2 = new StudentquestionDto()
        StudentquestionDto2.setTitle(QUESTION_TITLE)
        StudentquestionDto2.setContent(QUESTION_CONTENT)
        StudentquestionDto2.setStatus(StudentQuestion.Status.AVAILABLE.name())

        and: "an optionDto"
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        StudentquestionDto.setOptions(options)
        StudentquestionDto2.setOptions(options)
        questionService.createQuestion(course.getId(), StudentquestionDto)
        questionService.createQuestion(course.getId(), StudentquestionDto2)
        teacher.approveQuestion(StudentquestionDto) //has to be defined
        teacher.rejectQuestion(StudentquestionDto2)

        when:
        def result = user.getStudentQuestions()

        then:
        result.size() == 2
        result.get(0).getStatus() == ACCEPTED
        result.get(0).getId() != null
        result.get(0).getCreator() == user.getId()
        result.get(1).getStatus() == REJECTED
        result.get(1).getId() != null
        result.get(1).getCreator() == user.getId()
        result.get(1).getExplanation() == EXPLANATION
    }
    @TestConfiguration
    static class QuestionServiceImplTestContextConfiguration {

        @Bean
        QuestionService questionService() {
            return new QuestionService()
        }
    }
}
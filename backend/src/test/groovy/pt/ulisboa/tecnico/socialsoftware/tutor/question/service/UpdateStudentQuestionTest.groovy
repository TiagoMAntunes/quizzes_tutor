package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentQuestionService
import spock.lang.Specification

@DataJpaTest
class UpdateStudentQuestionTest extends Specification{

    def "update a question when it is not available"(){
        //the question is updated
        expect: false
    }

    def "update a question when it is already available"(){
        //the correct option and the question answers are updated
        expect: false
    }

    def "update a question with missing data"(){
        //an exception is thrown
        expect: false
    }

    def "update question with two options true"(){
        //an exception is thrown
        expect: false
    }
    
    @TestConfiguration
    static class StudentQuestionServiceImplTestContextConfiguration {

        @Bean
        StudentQuestionService studentQuestionService() {
            return new StudentQuestionService()
        }
    }
}
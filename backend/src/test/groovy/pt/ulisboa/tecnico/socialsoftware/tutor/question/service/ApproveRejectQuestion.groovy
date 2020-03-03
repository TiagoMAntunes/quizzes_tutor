package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

@DataJpaTest
class getQuestionStatus extends Specification {

    def "a question is approved"(){
        //status of the question should be correct
        expect: false
    }

    def "a question is rejected"(){
        //status of the question should be correct
        expect: false
    }

    def "an explanation is added for a rejected question"(){
        //verify that the explanation was added for the question
        expect: false
    }

    def "an explanation is added for an approved question"(){
        //an exception is thrown
        expect: false
    }

    def "an explanation is added for a question that has not been aproved or rejected"(){
        //an exception is thrown
        expect: false;
    }
}

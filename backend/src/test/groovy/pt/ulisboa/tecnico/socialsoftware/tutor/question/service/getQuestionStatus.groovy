package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

@DataJpaTest
class CreateQuestionStudent extends Specification {

    def "get status of approved question"(){
        //should return a list with the status, as well as the teacher that approved it
        //values should be correct
        expect: false
    }

    def "get status of rejected question with no explanation"(){
        //should return a list with the status, as well as the teacher that rejected it
        //values should be correct
        expect: false
    }

    def "get status of rejected question with explanation"(){
        //should return a list with the status, the teacher that rejected it and the explanation
        //values should be correct
        expect: false
    }

    def "get status of question waiting for approval"(){
        //should return a list with the status
        //value should be correct
        expect: false
    }
}
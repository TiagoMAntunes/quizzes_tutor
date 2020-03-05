package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification


@DataJpaTest
class TournamentSignUpTest extends Specification {

    def "tournament is already closed"(){
        //throws exception
        expect: false
    }

    def "tournament doesnt exist"(){
        //throws exception
        expect: false
    }

    def "tournament is open and student hasnt signed up for it yet"(){
        //should be able to sign up
        expect: false
    }

    def "tournament is open and student has already signed up for it"(){
        //throws exception
        expect: false
    }
}
package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification


@DataJpaTest
class TournamentPrivacyTest extends Specification {

    def setup() {
    }


    def "User sets tournament privacy to private"(){
        expect: false
    }


    def "User sets tournament privacy to public"(){
        expect: false
    }

    def "Get course tournaments information with 1 private student"() {
        expect: false
    }

    def "Get course tournaments information with 1 public student"() {
        expect: false
    }

    def "Get course tournaments information with multiple students"() {
        expect: false
    }

    def "Get course tournaments information without students"() {
        expect: false
    }


}

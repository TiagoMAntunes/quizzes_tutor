package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification


@DataJpaTest
class GetOpenTournaments extends Specification {

    def "no open tournaments"(){
        //should return an empty list
        expect: false
    }

    def "one open tournament"(){
        //should return a list with the one open tournament
        //start and end time should be correct, as well as included topics and creator name
        expect: false
    }

    def "3 open tournaments"(){
        //should return a list with 3 tournaments, ordered by ?
        //start and end time should be correct, as well as included topics and creator name
        expect: false
    }
}
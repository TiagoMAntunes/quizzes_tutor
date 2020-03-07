package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import spock.lang.Specification


@DataJpaTest
class GetOpenTournamentsTest extends Specification {
    static final USERNAME = 'username'

    public static final Date START_DATE_ONE = new Date()
    public static final Date END_DATE_ONE = new Date()
    public static final User CREATOR_ONE = new User('name', USERNAME, 1, User.Role.STUDENT)
    public static final List<Topic> TOPICS_ONE = new ArrayList<>();

    def "no open tournaments"(){
        when:
        def tournamentService = new TournamentService()
        def tournaments = tournamentService.getOpenTournaments()

        then:
        tournaments.size() == 0
    }

    def "one open tournament"(){
        given:"an open tournament"
        //TODO make tournament and add it to repository

        when:
        def tournamentService = new TournamentService()
        def tournaments = tournamentService.getOpenTournaments()

        then:
        tournaments.size() == 1
        tournaments.get(0).start == START_DATE_ONE
        tournaments.get(0).end == END_DATE_ONE
        tournaments.get(0).creator == CREATOR_ONE

        def topics = tournaments.get(0).topics
        for(int i=0; i<topics.size(); i++){
            topics.get(i).equals(TOPICS_ONE.get(i))
        }


    }

    def "3 open tournaments"(){
        //should return a list with 3 tournaments, ordered by ?
        //start and end time should be correct, as well as included topics and creator name
        expect: false
    }
}
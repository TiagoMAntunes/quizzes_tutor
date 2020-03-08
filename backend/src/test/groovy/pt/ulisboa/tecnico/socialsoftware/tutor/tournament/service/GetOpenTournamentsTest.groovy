package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@DataJpaTest
class GetOpenTournamentsTest extends Specification {
    public static final int NUMBER_QUESTIONS = 5
    public static final String TOPIC_NAME = "Main_Topic"
    public static final String COURSE_NAME = "Software Architecture"
    public static final String DIFF_COURSE_NAME = "Object Oriented Programming"

    @Autowired
    TournamentRepository tournamentRepository

    @Autowired
    TournamentService tournamentService

    @Autowired
    UserRepository userRepository

    @Autowired
    TopicRepository topicRepository

    @Autowired
    CourseRepository courseRepository

    def formatter
    def NOW_TIME
    def FIVE_DAYS_EARLIER
    def THREE_DAYS_EARLIER
    def TWO_DAYS_EARLIER
    def FIVE_DAYS_LATER
    def THREE_DAYS_LATER
    def TOPIC_LIST
    def CREATOR_ID
    def COURSE
    def COURSE_ID
    def DIFF_COURSE
    def DIFF_COURSE_ID
    def notOpenTournament
    def openTournament1
    def openTournament2
    def openTournament3
    def diffCourseTournament

    def setup() {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        NOW_TIME = LocalDateTime.now().format(formatter)
        FIVE_DAYS_LATER = LocalDateTime.now().plusDays(5).format(formatter)
        THREE_DAYS_LATER = LocalDateTime.now().plusDays(3).format(formatter)
        FIVE_DAYS_EARLIER = LocalDateTime.now().minusDays(5).format(formatter)
        THREE_DAYS_EARLIER = LocalDateTime.now().minusDays(3).format(formatter)
        TWO_DAYS_EARLIER = LocalDateTime.now().minusDays(2).format(formatter)

        //Creates a user
        def user = new User()
        user.setKey(1)
        userRepository.save(user)

        //Creates a course
        def course1 = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course1)

        def diffCourse = new Course(DIFF_COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(diffCourse)


        //Creates a topic
        def topic = new Topic()
        topic.setName(TOPIC_NAME)
        topic.setCourse(course1)
        course1.addTopic(topic)
        topic = topicRepository.save(topic)

        //Creates a topicdto list with 1 element
        def topicList = new ArrayList<TopicDto>()
        topicList.add(new TopicDto(topic))

        TOPIC_LIST = topicList

        //Creates an open tournament
        def userId = userRepository.findAll().get(0).getId()
        CREATOR_ID = userId

        def course2 = courseRepository.findAll().get(0)
        COURSE = course2
        COURSE_ID = course2.getId()

        def tournamentDto = new TournamentDto()
        tournamentDto.setStartTime(THREE_DAYS_EARLIER)
        tournamentDto.setFinishTime(THREE_DAYS_LATER)
        tournamentDto.setCreatorId(CREATOR_ID)
        tournamentDto.setTopics(TOPIC_LIST)
        tournamentDto.setNumberOfQuestions(NUMBER_QUESTIONS)
        tournamentDto.setCourse(COURSE)

        openTournament1 = tournamentDto

        //Creates a not yet open tournament
        def tournamentDto2 = new TournamentDto()
        tournamentDto2.setStartTime(THREE_DAYS_LATER)
        tournamentDto2.setFinishTime(FIVE_DAYS_LATER)
        tournamentDto2.setCreatorId(CREATOR_ID)
        tournamentDto2.setTopics(TOPIC_LIST)
        tournamentDto2.setNumberOfQuestions(NUMBER_QUESTIONS)
        tournamentDto2.setCourse(COURSE)

        notOpenTournament = tournamentDto2

        //Creates an open tournament for a different course
        def course3 = courseRepository.findAll().get(1)
        DIFF_COURSE = course3
        DIFF_COURSE_ID = course3.getId()

        def tournamentDto3 = new TournamentDto()
        tournamentDto3.setStartTime(THREE_DAYS_LATER)
        tournamentDto3.setFinishTime(FIVE_DAYS_LATER)
        tournamentDto3.setCreatorId(CREATOR_ID)
        tournamentDto3.setTopics(TOPIC_LIST)
        tournamentDto3.setNumberOfQuestions(NUMBER_QUESTIONS)
        tournamentDto3.setCourse(DIFF_COURSE)

        diffCourseTournament = tournamentDto3

        //Creates 2 more open tournaments for this course
        def tournamentDto4 = new TournamentDto()
        tournamentDto4.setStartTime(FIVE_DAYS_EARLIER)
        tournamentDto4.setFinishTime(THREE_DAYS_LATER)
        tournamentDto4.setCreatorId(CREATOR_ID)
        tournamentDto4.setTopics(TOPIC_LIST)
        tournamentDto4.setNumberOfQuestions(NUMBER_QUESTIONS)
        tournamentDto4.setCourse(COURSE)

        openTournament2 = tournamentDto4

        def tournamentDto5 = new TournamentDto()
        tournamentDto5.setStartTime(TWO_DAYS_EARLIER)
        tournamentDto5.setFinishTime(THREE_DAYS_LATER)
        tournamentDto5.setCreatorId(CREATOR_ID)
        tournamentDto5.setTopics(TOPIC_LIST)
        tournamentDto5.setNumberOfQuestions(NUMBER_QUESTIONS)
        tournamentDto5.setCourse(COURSE)

        openTournament3 = tournamentDto5
    }

    def "no open tournaments"(){
        given:"a tournament that is not yet open"
        tournamentService.createTournament(notOpenTournament)

        when:
        def tournaments = tournamentService.getOpenTournaments(COURSE)

        then:
        tournaments.size() == 0
    }

    def "no open tournaments for this course"(){
        given:"a tournament that is open, but belongs to a different course"
        tournamentService.createTournament(diffCourseTournament)

        when:
        def tournaments = tournamentService.getOpenTournaments(COURSE)

        then:
        tournaments.size() == 0
    }

    def "one open tournament"(){
        given:"an open tournament for this course"
        tournamentService.createTournament(openTournament1)

        when:
        def tournaments = tournamentService.getOpenTournaments(COURSE)

        then:
        tournaments.size() == 1
        tournaments.get(0).getStartTime() == THREE_DAYS_EARLIER
        tournaments.get(0).getFinishTime() == THREE_DAYS_LATER
        tournaments.get(0).getCreatorId() == CREATOR_ID
        tournaments.get(0).getNumberOfQuestions() == NUMBER_QUESTIONS
        tournaments.get(0).getCourse().getId() == COURSE_ID


        def topics = tournaments.get(0).getTopics()
        for(int i=0; i<topics.size(); i++){
            topics.get(i) == (TOPIC_LIST.get(i))
        }

    }

    def "3 open tournaments"(){
        given:"3 open tournaments for this course"
        tournamentService.createTournament(openTournament1)
        tournamentService.createTournament(openTournament2)
        tournamentService.createTournament(openTournament3)

        when:
        def tournaments = tournamentService.getOpenTournaments(COURSE)

        then:"returns the 3 tournaments, sorted by start date (most recent to least)"
        tournaments.size() == 3

        tournaments.get(0).getStartTime() == TWO_DAYS_EARLIER
        tournaments.get(0).getFinishTime() == THREE_DAYS_LATER
        tournaments.get(0).getCreatorId() == CREATOR_ID
        tournaments.get(0).getNumberOfQuestions() == NUMBER_QUESTIONS
        tournaments.get(0).getCourse().getId() == COURSE_ID

        tournaments.get(1).getStartTime() == THREE_DAYS_EARLIER
        tournaments.get(1).getFinishTime() == THREE_DAYS_LATER
        tournaments.get(1).getCreatorId() == CREATOR_ID
        tournaments.get(1).getNumberOfQuestions() == NUMBER_QUESTIONS
        tournaments.get(1).getCourse().getId() == COURSE_ID

        tournaments.get(2).getStartTime() == FIVE_DAYS_EARLIER
        tournaments.get(2).getFinishTime() == THREE_DAYS_LATER
        tournaments.get(2).getCreatorId() == CREATOR_ID
        tournaments.get(2).getNumberOfQuestions() == NUMBER_QUESTIONS
        tournaments.get(2).getCourse().getId() == COURSE_ID

        for(int i=0; i<3; i++) {
            def topics = tournaments.get(i).getTopics()
            for (int j = 0; j < topics.size(); j++) {
                topics.get(j) == (TOPIC_LIST.get(j))
            }
        }

    }

    @TestConfiguration
    static class TournamentServiceImplTestContextConfiguration {

        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }
    }
}
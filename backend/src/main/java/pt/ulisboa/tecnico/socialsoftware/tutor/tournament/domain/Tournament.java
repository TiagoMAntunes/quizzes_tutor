package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Entity
@Table(
        name = "tournaments",
        indexes = {
                @Index(name = "tournaments_indx_0", columnList = "key")
})
public class Tournament {

    @Transient
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;

    @Column(unique=true, nullable = false)
    private Integer key;

    @Column(name = "start_time")
    private LocalDateTime startTime = null;

    @Column(name = "finish_time")
    private LocalDateTime finishTime = null;

    @ManyToOne
    private User creator;

    @Column(name = "number_of_questions")
    private Integer numberOfQuestions;

    @ManyToOne
    private CourseExecution courseExecution;

    @ManyToMany(cascade = CascadeType.ALL, mappedBy = "tournaments", fetch=FetchType.EAGER)
    private List<Topic> topics = new ArrayList<>();

    @ManyToMany(cascade = CascadeType.ALL, mappedBy = "tournaments", fetch=FetchType.EAGER)
    private Set<User> signedUp = new HashSet<>();

    public Tournament(){}

    public Tournament(TournamentDto tournamentDto, List<Topic> topic, User creator) {
        this.id = tournamentDto.getId();
        this.key = tournamentDto.getKey();
        this.startTime = LocalDateTime.parse(tournamentDto.getStartTime(), formatter);
        this.finishTime = LocalDateTime.parse(tournamentDto.getFinishTime(), formatter);
        this.creator = creator;
        this.numberOfQuestions = tournamentDto.getNumberOfQuestions();
        this.topics = topic;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) { this.id = id; }

    public int getKey() {
        return key;
    }

    public void setKey(int key) { this.key = key; }

    public LocalDateTime getStartTime() { return startTime; }

    public void setStartTime(LocalDateTime time) { startTime = time; }

    public LocalDateTime getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(LocalDateTime time) { finishTime = time; }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User user) { creator = user; }

    public List<Topic> getTopics() {
        return topics;
    }

    public void setTopics(List<Topic> l) { topics = l; }

    public int getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public CourseExecution getCourseExecution() { 
        return courseExecution;
    }

    public void setCourseExecution(CourseExecution c) {
        courseExecution = c;
    }

    public boolean hasSignedUp(User user){
        return signedUp.contains(user);
    }

    public void signUp(User user){
        signedUp.add(user);
    }
}
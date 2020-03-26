package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tournaments")
public class Tournament {

    @Transient
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;

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

    @ManyToMany(cascade = CascadeType.ALL, mappedBy = "signedUpTournaments", fetch=FetchType.EAGER)
    private Set<User> signedUp = new HashSet<>();

    public Tournament(){}

    public Tournament(TournamentDto tournamentDto, List<Topic> topic, User creator) {
        this.id = tournamentDto.getId();
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

    public void cancel() {
        Set<Topic> topicsSet = new HashSet<>(getTopics());
        topicsSet.forEach(topic -> topic.getTournaments().remove(this));

        topics.clear();

        signedUp.forEach(user -> user.getSignedUpTournaments().remove(this));
        signedUp.clear();

        creator.getCreatedTournaments().remove(this);
        creator = null;

        courseExecution.getTournaments().remove(this);
        courseExecution = null;
    }

    public boolean hasSignedUp(User user){
        return signedUp.contains(user);
    }

    public void signUp(User user){
        signedUp.add(user);
    }

    public Set<User> getSignedUp() { return signedUp; }

    public int getSignedUpNumber(){ return signedUp.size();}
}
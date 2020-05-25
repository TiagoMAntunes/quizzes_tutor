package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;

import javax.persistence.*;
import java.time.LocalDateTime;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.INVALID_TITLE_FOR_TOURNAMENT;

@Entity
@Table(name = "tournaments")
public class
Tournament {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "start_time")
    private LocalDateTime startTime = null;

    @Column(name = "finish_time")
    private LocalDateTime finishTime = null;

    @Column(nullable = false)
    private String title = "Title";

    @ManyToOne
    private User creator;

    @Column(name = "number_of_questions")
    private Integer numberOfQuestions;

    @ManyToOne
    private CourseExecution courseExecution;

    @ManyToMany(cascade = CascadeType.ALL, mappedBy = "tournaments", fetch=FetchType.EAGER)
    private Set<Topic> topics = new HashSet<>();

    @ManyToMany(cascade = CascadeType.ALL, mappedBy = "signedUpTournaments", fetch=FetchType.EAGER)
    private Set<User> signedUp = new HashSet<>();

    @OneToOne
    private Quiz quiz;

    @ManyToMany(cascade = CascadeType.ALL, mappedBy = "bannedStudents", fetch=FetchType.EAGER)
    private Set<User> banned = new HashSet<>();

    public Tournament(){}

    public Tournament(TournamentDto tournamentDto, Set<Topic> topic, User creator) {
        this.id = tournamentDto.getId();
        this.startTime = DateHandler.toLocalDateTime(tournamentDto.getStartTime());
        this.finishTime = DateHandler.toLocalDateTime(tournamentDto.getFinishTime());
        this.creator = creator;
        this.numberOfQuestions = tournamentDto.getNumberOfQuestions();
        this.topics = new HashSet<>(topic);
        setTitle(tournamentDto.getTitle());
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null || title.isBlank())
            throw new TutorException(INVALID_TITLE_FOR_TOURNAMENT);

        this.title = title;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User user) { creator = user; }

    public Set<Topic> getTopics() {
        return topics;
    }

    public void setTopics(Collection<Topic> l) { topics = new HashSet<>(l); }

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

        if (quiz != null) quiz.remove();
        quiz = null;
    }

    public boolean hasSignedUp(User user){
        return signedUp.contains(user);
    }

    public boolean hasSignedUpWithId(Integer id){
        return signedUp.stream().anyMatch(user -> user.getId().equals(id));
    }

    public void signUp(User user){
        signedUp.add(user);
    }

    public Set<User> getSignedUp() { return signedUp; }

    public int getSignedUpNumber(){ return signedUp.size();}

    public Quiz getQuiz() {
        return this.quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public boolean hasQuiz() { return this.quiz != null; }

    public void setNumberOfQuestions(int size) {
        this.numberOfQuestions = size;
    }

    public void ban(User user){
        banned.add(user);
    }

    public Set<User> banned() { return banned; }
}
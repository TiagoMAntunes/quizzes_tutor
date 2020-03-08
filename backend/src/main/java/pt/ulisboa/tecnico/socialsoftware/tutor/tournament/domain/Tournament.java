package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain;


import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto;

import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "creator_id")
    private Integer creatorId;

    @Column(name = "number_of_questions")
    private Integer numberOfQuestions;

    @ManyToOne
    private Course course;

    @ManyToMany(cascade = CascadeType.ALL, mappedBy = "tournament", fetch=FetchType.EAGER)
    private List<Topic> topics = new ArrayList<>();

    public Tournament(TournamentDto tournamentDto, List<Topic> topic) {
        this.id = tournamentDto.getId();
        this.key = tournamentDto.getKey();
        this.startTime = LocalDateTime.parse(tournamentDto.getStartTime(), formatter);
        this.finishTime = LocalDateTime.parse(tournamentDto.getFinishTime(), formatter);
        this.creatorId = tournamentDto.getCreatorId();
        this.numberOfQuestions = tournamentDto.getNumberOfQuestions();
        this.course = tournamentDto.getCourse();
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

    public int getCreatorID() {
        return creatorId;
    }

    public void setCreatorId(int id) { creatorId = id; }

    public List<Topic> getTopics() {
        return topics;
    }

    public void setTopics(List<Topic> l) { topics = l; }

    public int getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public Course getCourse() { return course; }

    public void setCourse(Course course) { this.course = course; }
}
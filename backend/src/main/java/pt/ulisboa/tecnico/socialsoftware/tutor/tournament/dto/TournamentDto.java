package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto;

import org.springframework.data.annotation.Transient;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class TournamentDto implements Serializable {

    private Integer id;
    private String startTime = null;
    private String finishTime = null;
    private Integer numberOfQuestions;
    private Set<TopicDto> topics;

    @Transient
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public TournamentDto(){}

    public TournamentDto(Tournament tournament) {
        this.id = tournament.getId();
        this.numberOfQuestions = tournament.getNumberOfQuestions();

        if (tournament.getStartTime() != null)
            this.startTime = tournament.getStartTime().format(formatter);
        if (tournament.getFinishTime() != null)
            this.finishTime= tournament.getFinishTime().format(formatter);

        this.topics = tournament.getTopics().stream().map(TopicDto::new).collect(Collectors.toSet());
    }

    public void setId(int id) { this.id = id; }

    public Integer getId() { return id; }

    public void setStartTime(String time) { startTime = time; }

    public String getStartTime() { return startTime; }

    public void setFinishTime(String time) { finishTime = time; }

    public String getFinishTime() { return finishTime; }

    public void setNumberOfQuestions(int p) { numberOfQuestions = p; }

    public int getNumberOfQuestions() { return numberOfQuestions; }

    public void setTopics(Collection<TopicDto> p) { topics = (p == null? null: new HashSet<>(p)); }

    public Set<TopicDto> getTopics() { return topics; }
}

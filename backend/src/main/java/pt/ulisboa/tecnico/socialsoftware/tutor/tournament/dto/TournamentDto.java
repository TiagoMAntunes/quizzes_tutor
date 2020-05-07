package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament;

import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class TournamentDto implements Serializable {

    private Integer id;
    private String title;
    private Boolean hasSignedUp;
    private Boolean isCreator;
    private String startTime = null;
    private String finishTime = null;
    private Integer numberOfQuestions;
    private Integer numberOfParticipants;
    private Set<TopicDto> topics;

    public TournamentDto(){}

    public TournamentDto(Tournament tournament) {
        this.id = tournament.getId();
        this.title = tournament.getTitle();
        this.numberOfQuestions = tournament.getNumberOfQuestions();
        this.numberOfParticipants = tournament.getSignedUpNumber();

        if (tournament.getStartTime() != null)
            this.startTime = DateHandler.toISOString(tournament.getStartTime());
        if (tournament.getFinishTime() != null)
            this.finishTime= DateHandler.toISOString(tournament.getFinishTime());

        this.topics = tournament.getTopics().stream().map(TopicDto::new).collect(Collectors.toSet());
    }

    public TournamentDto(Tournament tournament, Integer userId) {
        this(tournament);
        this.hasSignedUp = tournament.hasSignedUpWithId(userId);
        this.isCreator = tournament.getCreator().getId().equals(userId);
        this.numberOfParticipants = tournament.getSignedUpNumber();
    }

    public void setId(int id) { this.id = id; }

    public Integer getId() { return id; }

    public void setTitle(String title) { this.title = title; }

    public String getTitle() { return title; }

    public void setIsCreator(Boolean creator) { isCreator = creator; }

    public Boolean getIsCreator() { return isCreator; }

    public void setHasSignedUp(Boolean signedUp) { hasSignedUp = signedUp; }

    public Boolean getHasSignedUp() { return hasSignedUp; }

    public void setStartTime(String time) { startTime = time; }

    public String getStartTime() { return startTime; }

    public void setFinishTime(String time) { finishTime = time; }

    public String getFinishTime() { return finishTime; }

    public void setNumberOfQuestions(int p) { numberOfQuestions = p; }

    public int getNumberOfQuestions() { return numberOfQuestions; }

    public void setTopics(Collection<TopicDto> p) { topics = (p == null? null: new HashSet<>(p)); }

    public Set<TopicDto> getTopics() { return topics; }

    public void setNumberOfParticipants(int p) { numberOfParticipants = p; }

    public Integer getNumberOfParticipants() { return numberOfParticipants; }
}

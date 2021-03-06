package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto;

import java.io.Serializable;
import java.util.*;

public class TournamentScoreboardDto implements Serializable {
    private List<TournamentScoreDto> scores = new ArrayList<>();
    private float averageScore = 0;
    private Integer numberOfParticipants = 0;
    private Integer numberOfQuestions = 0;
    private String tournamentTitle;

    public TournamentScoreboardDto(){
        //empty
    }

    public List<TournamentScoreDto> getScores() {
        return scores;
    }

    public void setScores(List<TournamentScoreDto> scores) {
        this.scores = scores;
    }

    public float getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(float averageScore) {
        this.averageScore = averageScore;
    }

    public Integer getNumberOfQuestions() { return this.numberOfQuestions; }

    public void setNumberOfQuestions(Integer numberOfQuestions) { this.numberOfQuestions = numberOfQuestions; }

    public void setNumberOfParticipants(Integer numberOfParticipants) { this.numberOfParticipants = numberOfParticipants; }

    public Integer getNumberOfParticipants() { return this.numberOfParticipants; }

    public String getTournamentTitle() { return this.tournamentTitle; }

    public void setTournamentTitle(String tournamentTitle) { this.tournamentTitle = tournamentTitle; }


}

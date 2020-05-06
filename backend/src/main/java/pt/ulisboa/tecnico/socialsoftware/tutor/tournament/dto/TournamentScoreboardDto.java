package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TournamentScoreboardDto implements Serializable {
    List<Integer> scores = new ArrayList<>();
    float averageScore = 0;

    public TournamentScoreboardDto(){}

    public List<Integer> getScores() {
        return scores;
    }

    public void setScores(List<Integer> scores) {
        this.scores = scores;
    }

    public float getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(float averageScore) {
        this.averageScore = averageScore;
    }
}

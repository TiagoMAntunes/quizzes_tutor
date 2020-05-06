package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard;

import java.io.Serializable;

public class TournamentDashboardDto implements Serializable {
    private Integer createdTournaments = 0;
    private Integer participatedTournamentsNumber = 0;
    private Integer notYetParticipatedTournamentsNumber = 0;
    private float averageTournamentScore = 0;

    public Integer getCreatedTournaments() {
        return createdTournaments;
    }

    public void setCreatedTournaments(Integer createdTournaments) {
        this.createdTournaments = createdTournaments;
    }

    public Integer getParticipatedTournamentsNumber() {
        return participatedTournamentsNumber;
    }

    public void setParticipatedTournamentsNumber(Integer participatedTournamentsNumber) {
        this.participatedTournamentsNumber = participatedTournamentsNumber;
    }

    public Integer getNotYetParticipatedTournamentsNumber() {
        return notYetParticipatedTournamentsNumber;
    }

    public void setNotYetParticipatedTournamentsNumber(Integer notYetParticipatedTournamentsNumber) {
        this.notYetParticipatedTournamentsNumber = notYetParticipatedTournamentsNumber;
    }

    public float getAverageTournamentScore() {
        return averageTournamentScore;
    }

    public void setAverageTournamentScore(float averageTournamentScore) {
        this.averageTournamentScore = averageTournamentScore;
    }
}

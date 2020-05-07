package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard;

public class DashboardDto {
    private Integer numberQuestionsSubmitted = 0;
    private Integer numberQuestionsApproved = 0;
    private Integer createdTournaments = 0;
    private Integer participatedTournamentsNumber = 0;
    private Integer notYetParticipatedTournamentsNumber = 0;
    private float averageTournamentScore = 0;

    public DashboardDto() { }

    public int getNumberQuestionsSubmitted() {
        return numberQuestionsSubmitted;
    }

    public int getNumberQuestionsApproved() {
        return numberQuestionsApproved;
    }

    public void setNumberQuestionsSubmitted(int questionsSubmitted) {
        this.numberQuestionsSubmitted = questionsSubmitted;
    }

    public void setNumberQuestionsApproved(int questionsApproved) {
        this.numberQuestionsApproved = questionsApproved;
    }

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

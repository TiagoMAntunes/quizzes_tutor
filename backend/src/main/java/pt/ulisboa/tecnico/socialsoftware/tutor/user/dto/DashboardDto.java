package pt.ulisboa.tecnico.socialsoftware.tutor.user.dto;

public class DashboardDto {
    private int numberQuestionsSubmitted;
    private int numberQuestionsApproved;

    public DashboardDto() { }

    public DashboardDto( int submitted, int approved ) {
        numberQuestionsSubmitted = submitted;
        numberQuestionsApproved = approved;
    }

    public int getQuestionsSubmitted() {
        return numberQuestionsSubmitted;
    }

    public int getQuestionsApproved() {
        return numberQuestionsApproved;
    }

    public void setQuestionsSubmitted(int questionsSubmitted) {
        this.numberQuestionsSubmitted = questionsSubmitted;
    }

    public void setQuestionsApproved(int questionsApproved) {
        this.numberQuestionsApproved = questionsApproved;
    }


}

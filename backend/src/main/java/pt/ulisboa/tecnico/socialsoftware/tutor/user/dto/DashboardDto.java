package pt.ulisboa.tecnico.socialsoftware.tutor.user.dto;

public class DashboardDto {
    private int numberQuestionsSubmitted;
    private int numberQuestionsApproved;

    public DashboardDto() { }

    public DashboardDto( int submitted, int approved ) {
        numberQuestionsSubmitted = submitted;
        numberQuestionsApproved = approved;
    }

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


}

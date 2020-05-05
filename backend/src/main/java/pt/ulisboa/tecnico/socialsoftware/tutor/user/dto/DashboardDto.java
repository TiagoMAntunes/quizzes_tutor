package pt.ulisboa.tecnico.socialsoftware.tutor.user.dto;

public class DashboardDto {
    private int questionsSubmitted;
    private int questionsApproved;

    public DashboardDto() { }

    public DashboardDto( int submitted, int approved ) {
        questionsSubmitted = submitted;
        questionsApproved = approved;
    }

    public int getQuestionsSubmitted() {
        return questionsSubmitted;
    }

    public int getQuestionsApproved() {
        return questionsApproved;
    }

    public void setQuestionsSubmitted(int questionsSubmitted) {
        this.questionsSubmitted = questionsSubmitted;
    }

    public void setQuestionsApproved(int questionsApproved) {
        this.questionsApproved = questionsApproved;
    }


}

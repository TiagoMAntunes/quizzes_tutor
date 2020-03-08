package pt.ulisboa.tecnico.socialsoftware.tutor.tournament;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import java.util.Date;
import java.util.List;

public class Tournament {
    private Date start;
    private Date end;
    private User creator;
    private List<Topic> topics;

    public Tournament(){}

    public Tournament(Date s, Date e, User u, List<Topic> t){
        this.start = s;
        this.end = e;
        this.creator = u;
        this.topics = t;
    }
}

package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain;


import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.TopicConjunction;

import javax.persistence.*;

@Entity
@Table(
        name = "tournaments",
        indexes = {
                @Index(name = "tournaments_indx_0", columnList = "key")
})
public class Tournament {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;

    @Column(unique=true, nullable = false)
    private Integer key;

    public int getId() {
        return -1;
    }

    public int getKey() {
        return -1;
    }

    public Object getStartTime() {
        return null;
    }

    public Object getFinishTime() {
        return null;
    }

    public int getCreatorID() {
        return -1;
    }

    public Object getTopics() {
        return null;
    }

    public int getNumberOfQuestions() {
        return -1;
    }

    public TopicConjunction getTopicConjunction() {
        return null;
    }
}
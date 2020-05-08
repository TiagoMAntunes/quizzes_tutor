package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto;

import java.io.Serializable;

public class TournamentScoreDto implements Serializable {
    private String name;
    private Integer score;

    public TournamentScoreDto(){}

    public TournamentScoreDto(String n, Integer s){
        name = n;
        score = s;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public void setScore(Integer score){
        this.score = score;
    }

    public Integer getScore(){
        return this.score;
    }
}

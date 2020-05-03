package pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz;

import java.io.Serializable;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class StatementQuizDto implements Serializable {
    private Integer id;
    private Integer quizAnswerId;
    private String title;
    private boolean oneWay;
    private String availableDate;
    private String conclusionDate;
    private Long timeToAvailability;
    private Long timeToSubmission;
    private Long timeToResults;
    private List<StatementQuestionDto> questions = new ArrayList<>();
    private List<StatementAnswerDto> answers = new ArrayList<>();

    public StatementQuizDto() {}

    public StatementQuizDto(QuizAnswer quizAnswer) {
        this.id = quizAnswer.getQuiz().getId();
        this.quizAnswerId = quizAnswer.getId();
        this.title = quizAnswer.getQuiz().getTitle();
        this.oneWay = quizAnswer.getQuiz().isOneWay();
        this.availableDate = DateHandler.toISOString(quizAnswer.getQuiz().getAvailableDate());
        this.conclusionDate = DateHandler.toISOString(quizAnswer.getQuiz().getConclusionDate());

        if (quizAnswer.getQuiz().getConclusionDate() != null && quizAnswer.getQuiz().getType().equals(Quiz.QuizType.IN_CLASS)) {
            this.timeToSubmission = ChronoUnit.MILLIS.between(DateHandler.now(), quizAnswer.getQuiz().getConclusionDate());
        }

        if (quizAnswer.getQuiz().getResultsDate() != null && quizAnswer.getQuiz().getType().equals(Quiz.QuizType.IN_CLASS)) {
            this.timeToResults = ChronoUnit.MILLIS.between(DateHandler.now(), quizAnswer.getQuiz().getResultsDate());
        }

        this.questions = quizAnswer.getQuestionAnswers().stream()
                .map(StatementQuestionDto::new)
                .sorted(Comparator.comparing(StatementQuestionDto::getSequence))
                .collect(Collectors.toList());

        this.answers = quizAnswer.getQuestionAnswers().stream()
                .map(StatementAnswerDto::new)
                .sorted(Comparator.comparing(StatementAnswerDto::getSequence))
                .collect(Collectors.toList());
    }

    public Integer getId() {
        return id;
    }

    public Integer getQuizAnswerId() {
        return quizAnswerId;
    }

    public void setQuizAnswerId(Integer quizAnswerId) {
        this.quizAnswerId = quizAnswerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isOneWay() {
        return oneWay;
    }

    public void setOneWay(boolean oneWay) {
        this.oneWay = oneWay;
    }

    public String getAvailableDate() {
        return availableDate;
    }

    public void setAvailableDate(String availableDate) {
        this.availableDate = availableDate;
    }

    public String getConclusionDate() {
        return conclusionDate;
    }

    public void setConclusionDate(String conclusionDate) {
        this.conclusionDate = conclusionDate;
    }

    public Long getTimeToAvailability() {
        return timeToAvailability;
    }

    public void setTimeToAvailability(Long timeToAvailability) {
        this.timeToAvailability = timeToAvailability;
    }

    public Long getTimeToSubmission() {
        return timeToSubmission;
    }

    public void setTimeToSubmission(Long timeToSubmission) {
        this.timeToSubmission = timeToSubmission;
    }

    public Long getTimeToResults() {
        return timeToResults;
    }

    public void setTimeToResults(Long timeToResults) {
        this.timeToResults = timeToResults;
    }

    public List<StatementQuestionDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<StatementQuestionDto> questions) {
        this.questions = questions;
    }

    public List<StatementAnswerDto> getAnswers() {
        return answers;
    }

    public void setAnswers(List<StatementAnswerDto> answers) {
        this.answers = answers;
    }

    @Override
    public String toString() {
        return "StatementQuizDto{" +
                "id=" + id +
                ", quizAnswerId=" + quizAnswerId +
                ", title='" + title + '\'' +
                ", oneWay=" + oneWay +
                ", availableDate='" + availableDate + '\'' +
                ", conclusionDate='" + conclusionDate + '\'' +
                ", timeToAvailability=" + timeToAvailability +
                ", timeToSubmission=" + timeToSubmission +
                ", timeToResults=" + timeToResults +
                ", questions=" + questions +
                ", answers=" + answers +
                '}';
    }
}
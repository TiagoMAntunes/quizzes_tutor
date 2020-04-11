package pt.ulisboa.tecnico.socialsoftware.tutor.question.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface QuestionRepository extends JpaRepository<Question, Integer> {
    @Query(value = "select q from Question q where q.course.id = :courseId and type(q) = Question")
    List<Question> findQuestions(int courseId);

    @Query(value = "SELECT q FROM Question q WHERE q.course.id = :courseId AND q.status = 'AVAILABLE' and type(q) = Question ")
    List<Question> findAvailableQuestions(int courseId);

    @Query(value = "SELECT count(q) FROM Question q WHERE q.course.id = :courseId AND q.status = 'AVAILABLE' and type(q) = Question")
    Integer getAvailableQuestionsSize(Integer courseId);

    @Query(value = "SELECT MAX(key) FROM Question q WHERE type(q) = Question")
    Integer getMaxQuestionNumber();

    @Query(value = "SELECT q FROM Question q WHERE q.key = :key AND type(q) = Question")
    Optional<Question> findByKey(Integer key);
}
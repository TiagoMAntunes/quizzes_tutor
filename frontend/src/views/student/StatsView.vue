<template>
  <div class="container">
    <h2>Statistics</h2>
    <div v-if="stats != null" class="stats-container">
      <div class="items">
        <div class="icon-wrapper" ref="totalQuizzes">
          <animated-number :number="stats.totalQuizzes" />
        </div>
        <div class="project-name">
          <p>Total Quizzes Solved</p>
        </div>
      </div>
      <div class="items">
        <div class="icon-wrapper" ref="totalAnswers">
          <animated-number :number="stats.totalAnswers" />
        </div>
        <div class="project-name">
          <p>Total Questions Solved</p>
        </div>
      </div>
      <div class="items">
        <div class="icon-wrapper" ref="totalUniqueQuestions">
          <animated-number :number="stats.totalUniqueQuestions" />
        </div>
        <div class="project-name">
          <p>Unique Questions Solved</p>
        </div>
      </div>
      <div class="items">
        <div class="icon-wrapper" ref="correctAnswers">
          <animated-number :number="stats.correctAnswers">%</animated-number>
        </div>
        <div class="project-name">
          <p>Total Correct Answers</p>
        </div>
      </div>
      <div class="items">
        <div class="icon-wrapper" ref="improvedCorrectAnswers">
          <animated-number :number="stats.improvedCorrectAnswers"
            >%</animated-number
          >
        </div>
        <div class="project-name">
          <p>Improved Correct Questions</p>
        </div>
      </div>
      <div class="items">
        <div class="icon-wrapper" ref="percentageOfSeenQuestions">
          <animated-number
            :number="
              (stats.totalUniqueQuestions * 100) / stats.totalAvailableQuestions
            "
            >%</animated-number
          >
        </div>
        <div class="project-name">
          <p>Percentage of questions seen</p>
        </div>
      </div>
    </div>
    <h2>Questions</h2>
    <div v-if="dashboard != null" class="stats-container">
      <div class="items">
        <div class="icon-wrapper" ref="QuestionsSubmitted"  >
          <animated-number  :number="dashboard.numberQuestionsSubmitted" />
        </div>
        <div class="project-name">
          <p>Total Questions Submitted</p>
        </div>
      </div>
      <div class="items">
        <div class="icon-wrapper" ref="QuestionsApproved" >
          <animated-number    :number="dashboard.numberQuestionsApproved" />
        </div>
        <div class="project-name">
          <p>Total Questions Approved</p>
        </div>
      </div>
    </div>
    <h2>Tournaments</h2>
    <div v-if="dashboard != null" class="stats-container">
      <div class="items">
        <div class="icon-wrapper" ref="createdTournaments">
          <animated-number :number="dashboard.createdTournaments" />
        </div>
        <div class="project-name">
          <p>Created Tournaments</p>
        </div>
      </div>
      <div class="items">
        <div class="icon-wrapper" ref="participatedTournaments">
          <animated-number :number="dashboard.participatedTournamentsNumber" />
        </div>
        <div class="project-name">
          <p>Participated Tournaments</p>
        </div>
      </div>
      <div class="items">
        <div class="icon-wrapper" ref="averageScore">
          <animated-number :number="dashboard.averageTournamentScore" 
          >%</animated-number
          >
        </div>
        <div class="project-name">
          <p>Average Tournament Score</p>
        </div>
      </div>
      <div class="items">
        <div class="icon-wrapper" ref="notYetParticipated">
          <animated-number :number="dashboard.notYetParticipatedTournamentsNumber" />
        </div>
        <div class="project-name">
          <p>Joined Tournaments with Available Quizzes</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import StudentStats from '@/models/statement/StudentStats';
import RemoteServices from '@/services/RemoteServices';
import AnimatedNumber from '@/components/AnimatedNumber.vue';
import StudentDashboard from '@/models/user/StudentDashboard';

@Component({
  components: { AnimatedNumber }
})
export default class StatsView extends Vue {
  stats: StudentStats | null = null;
  dashboard: StudentDashboard | null = null;

  async created() {
    await this.$store.dispatch('loading');
    try {
      this.dashboard =  await RemoteServices.getStudentDashboard();
      this.stats = await RemoteServices.getUserStats();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }
}
</script>

<style lang="scss" scoped>
.stats-container {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  justify-content: center;
  align-items: stretch;
  align-content: center;
  height: 100%;

  .items {
    background-color: rgba(255, 255, 255, 0.75);
    color: #1976d2;
    border-radius: 5px;
    flex-basis: 25%;
    margin: 20px;
    cursor: pointer;
    transition: all 0.6s;
    padding: 0px 10px;
  }
}

.icon-wrapper,
.project-name {
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-wrapper {
  font-size: 100px;
  transform: translateY(0px);
  transition: all 0.6s;
}

.icon-wrapper {
  align-self: end;
}

.project-name {
  align-self: start;
}
.project-name p {
  font-size: 24px;
  font-weight: bold;
  letter-spacing: 2px;
  transform: translateY(0px);
  transition: all 0.5s;
}

.items:hover {
  border: 3px solid black;

  & .project-name p {
    transform: translateY(-10px);
  }
  & .icon-wrapper i {
    transform: translateY(5px);
  }
}
</style>

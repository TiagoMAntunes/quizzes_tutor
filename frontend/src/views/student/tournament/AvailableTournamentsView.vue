<template>
  <div class="container">
    <h2>Available Tournaments</h2>
    <ul>
      <li class="list-header">
        <div class="col">Start Time</div>
        <div class="col">Finish Time</div>
        <div class="col">Topics</div>
        <div class="col">Number of Questions</div>
        <div class="col last-col"></div>
      </li>
      <li
        class="list-row"
        v-for="tournament in tournaments"
        :key="tournament.id"
      >
        <div class="col">
          {{ tournament.startTime }}
        </div>
        <div class="col">
          {{ tournament.finishTime }}
        </div>
        <div class="col topics">
          <v-chip v-for="topic in tournament.topics" :key="topic.id"
            class="ma-2"
            small
          >
            {{ topic.name }}
          </v-chip>
        </div>
        <div class="col">
          {{ tournament.numberOfQuestions }}
        </div>
        <div class="col last-col">
          <i class="fas fa-chevron-circle-right"></i>
        </div>
      </li>
    </ul>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import Tournament from '@/models/tournament/Tournament';
import RemoteServices from '@/services/RemoteServices';

@Component
export default class AvailableTournamentsView extends Vue {
  tournaments: Tournament[] = [];
  search: string = '';

  async created() {
    await this.$store.dispatch('loading');
    try {
      this.tournaments = await RemoteServices.getOpenTournaments();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }
}
</script>

<style lang="scss" scoped>
.container {
  max-width: 1000px;
  margin-left: auto;
  margin-right: auto;
  padding-left: 10px;
  padding-right: 10px;

  h2 {
    font-size: 26px;
    margin: 20px 0;
    text-align: center;
    small {
      font-size: 0.5em;
    }
  }

  ul {
    overflow: hidden;
    padding: 0 5px;

    li {
      border-radius: 3px;
      padding: 15px 10px;
      display: flex;
      justify-content: space-between;
      margin-bottom: 10px;
    }

    .list-header {
      background-color: #1976d2;
      color: white;
      font-size: 14px;
      text-transform: uppercase;
      letter-spacing: 0.03em;
      text-align: center;
    }

    .col {
      font-size: 12px;
      flex-basis: 25% !important;
      margin: auto; /* Important */
      text-align: center;
    }

    .list-row {
      background-color: #ffffff;
      box-shadow: 0 0 9px 0 rgba(0, 0, 0, 0.1);
      display: flex;
    }
  }
}
</style>


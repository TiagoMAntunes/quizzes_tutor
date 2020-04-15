<template>
  <div class="container">
    <h2>Available Tournaments</h2>
    <ul>
      <li class="list-header">
        <div class="col">Start Time</div>
        <div class="col">Finish Time</div>
        <div class="col topics">Topics</div>
        <div class="col">Number of Questions</div>
        <div class="col last-col"></div>
      </li>
      <li
        class="list-row"
        v-for="tournament in sortDate(tournaments)"
        :key="tournament.id + '-tournament'"
      >
        <div class="col">
          {{ tournament.startTime }}
        </div>
        <div class="col">
          {{ tournament.finishTime }}
        </div>
        <div class="col topics">
          <v-chip v-for="topic in sortAlpha(tournament.topics)" :key="topic.id + '-topic'"
            class="ma-2 topic-chip"
            small
            outlined
          ><span>
            {{ topic.name }}
          </span>
          </v-chip>
        </div>
        <div class="col">
          {{ tournament.numberOfQuestions }}
        </div>
        <div class="col last-col">
          <!-- placeholder button -->
          <i class="fas fa-chevron-circle-right"></i>
        </div>
      </li>
    </ul>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import Tournament from '@/models/tournament/Tournament';
import Topic from '@/models/management/Topic';
import RemoteServices from '@/services/RemoteServices';
import moment from 'moment';

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

  sortAlpha(list: Topic[]){
    return list.slice().sort((a, b) => a.name.localeCompare(b.name) );
  }

  sortDate(list: Tournament[]){
    return list.slice().sort((a, b) => moment(a.startTime).isBefore(moment(b.startTime)) ? -1 : 1 );
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
    padding: 0 3px;

    li {
      border-radius: 3px;
      padding: 15px 10px;
      display: flex;
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
      font-size: 13px;
      text-align: center;
      margin: auto;
      min-width: 5%;
    }

    .list-row {
      background-color: #ffffff;
      box-shadow: 0 0 9px 0 rgba(0, 0, 0, 0.1);
      display: flex;
    }

    .topics{
      flex-grow: 2.5;
      overflow: auto;
    }

    .topic-chip{
      margin: 4px !important;
      overflow: visible;
      padding: 0px;
      font-size: 13px;
    }

    .topic-chip span{
      background-color: #e0e0e0;
      border-color: rgba(0,0,0,.12);
      color: rgba(0,0,0,.87);
      border-radius: 12px;
      padding: 2px 10px;
      margin: none;
    }
  }
}
</style>


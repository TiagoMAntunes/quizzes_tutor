<template>
  <div class="container">
    <h2>Available Tournaments</h2>
    <ul>
      <li class="list-header">
        <div class="col">Title</div>
        <div class="col">Start Time</div>
        <div class="col">Finish Time</div>
        <div class="col topics">Topics</div>
        <div class="col">Number of Questions</div>
        <div class="col">Number of Participants</div>
        <div class="col last-col"></div>
      </li>
      <li
        class="list-row"
        v-for="tournament in sortDate(tournaments)"
        :key="tournament.id + '-tournament'"
      >
        <div class="col">
          {{ tournament.title }}
        </div>
        <div class="col">
          {{ tournament.startTime }}
        </div>
        <div class="col">
          {{ tournament.finishTime }}
        </div>
        <div class="col topics">
          <v-chip
            v-for="topic in sortAlpha(tournament.topics)"
            :key="topic.id + '-topic'"
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
        <div class="col">
          {{ tournament.numberOfParticipants }}
        </div>
        <div class="col last-col">
          <v-tooltip bottom v-if="!tournament.hasSignedUp">
            <template v-slot:activator="{ on }">
              <v-icon
                small
                class="mr-2"
                v-on="on"
                @click="$emit('joinTournament', tournament.id)"
                color="dark grey"
                >fas fa-user-plus</v-icon
              >
            </template>
            <span>Sign Up for Tournament</span>
          </v-tooltip>
          <v-tooltip bottom v-if="tournament.isCreator">
            <template v-slot:activator="{ on }">
              <v-icon
                small
                class="mr-2"
                v-on="on"
                @click="$emit('cancelTournament', tournament.id)"
                color="red"
                >delete</v-icon
              >
            </template>
            <span>Cancel Tournament</span>
          </v-tooltip>
        </div>
      </li>
    </ul>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop } from 'vue-property-decorator';
import Tournament from '@/models/tournament/Tournament';
import Topic from '@/models/management/Topic';
import RemoteServices from '@/services/RemoteServices';
import moment from 'moment';

@Component
export default class TournamentList extends Vue {
  @Prop({ type: Array, required: true }) readonly tournaments!: Tournament[];

  sortAlpha(list: Topic[]) {
    return list.slice().sort((a, b) => a.name.localeCompare(b.name));
  }

  sortDate(list: Tournament[]) {
    return list
      .slice()
      .sort((a, b) =>
        moment(a.startTime).isBefore(moment(b.startTime)) ? -1 : 1
      );
  }
}
</script>

<style lang="scss">
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
    padding: 0px;

    li {
      border-radius: 3px;
      padding: 15px 5px;
      display: flex;
      margin-bottom: 10px;
    }

    .list-header {
      background-color: #1976d2;
      color: white;
      font-size: 12px;
      text-transform: uppercase;
      letter-spacing: 0.03em;
      text-align: center;
    }

    .col {
      font-size: 12px;
      text-align: center;
      margin: auto;
      min-width: 10%;
      padding: 12px 5px;
      overflow: auto;
    }

    .list-row {
      background-color: #ffffff;
      box-shadow: 0 0 9px 0 rgba(0, 0, 0, 0.1);
      display: flex;
    }

    .topics {
      flex-grow: 2.5;
      overflow: auto;
    }

    .topic-chip {
      margin: 4px !important;
      overflow: visible;
      padding: 0px;
      font-size: 13px;
    }

    .topic-chip span {
      background-color: #e0e0e0;
      border-color: rgba(0, 0, 0, 0.12);
      color: rgba(0, 0, 0, 0.87);
      border-radius: 12px;
      padding: 2px 10px;
      margin: none;
    }
  }
}
</style>

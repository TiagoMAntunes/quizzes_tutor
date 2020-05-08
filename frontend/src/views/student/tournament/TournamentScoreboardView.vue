<template>
  <div class="container">
    <h2>Tournament Scoreboards</h2>
    <ul>
      <li class="list-header">
        <div class="col">Title</div>
        <div class="col">Number of Participants</div>
        <div class="col">Average Score</div>
        <div class="col last-col"></div>
      </li>
      <li
        class="list-row"
        v-for="(scoreboard, index) in sortAlpha(scoreboards)"
        :key="index"
      >
        <div class="col">
          {{ scoreboard.tournamentTitle }}
        </div>
        <div class="col">
          {{ scoreboard.numberOfParticipants }}
        </div>
        <div class="col">
          {{ scoreboard.averageScore }}
          /
          {{ scoreboard.numberOfQuestions }}
        </div>
        <div class="col last-col">
          <v-tooltip bottom>
            <template v-slot:activator="{ on }">
              <v-icon
                small
                class="mr-2"
                v-on="on"
                color="dark grey"
                data-cy="details"
                @click="showDetailedScoreboardDialog(scoreboard)"
                >visibility</v-icon
              >
            </template>
            <span>See Details</span>
          </v-tooltip>
        </div>
      </li>
    </ul>
    <show-detailed-scoreboard-dialog
      v-if="currentScoreboard"
      :dialog="detailedDialog"
      :scoreboard="currentScoreboard"
      v-on:close-show-detailed-scoreboard-dialog="onCloseShowDetailedScoreboardDialog"
    />
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import TournamentScoreboard from '@/models/tournament/TournamentScoreboard';
import ShowDetailedScoreboardDialog from '@/views/student/tournament/ShowDetailedScoreboardDialog.vue';
import RemoteServices from '@/services/RemoteServices';
import moment from 'moment';

@Component({
  components: {
    'show-detailed-scoreboard-dialog': ShowDetailedScoreboardDialog
  }
})
export default class TournamentList extends Vue {
    scoreboards: TournamentScoreboard[] = [];
    currentScoreboard: TournamentScoreboard | null = null;
    detailedDialog: boolean = false;

    async created() {
    await this.$store.dispatch('loading');
    try {
      this.scoreboards = await RemoteServices.getTournamentScoreboards();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  sortAlpha(list: TournamentScoreboard[]) {
    return list.slice().sort((a, b) => a.tournamentTitle.localeCompare(b.tournamentTitle));
  }

  showDetailedScoreboardDialog(scoreboard: TournamentScoreboard) {
    this.currentScoreboard = scoreboard;
    this.detailedDialog = true;
  }

  onCloseShowDetailedScoreboardDialog() {
    this.detailedDialog = false;
  }

}
</script>

<style lang="scss">
.container {
  max-width: 900px !important;
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
  }
}
</style>

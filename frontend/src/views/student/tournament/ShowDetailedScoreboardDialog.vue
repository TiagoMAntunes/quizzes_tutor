<template>
  <v-dialog
    v-model="dialog"
    @keydown.esc="closeDetailedScoreboardDialog"
    max-width="75%"
  >
    <v-card>
      <v-card-title>
        <span class="headline"
          >Tournament Scoreboard - {{ scoreboard.tournamentTitle }}</span
        >
      </v-card-title>
      <v-card-text class="text-left">
        <show-scores :scores="scoreboard.scores" />
      </v-card-text>
      <footer>
        (Displaying only public scores)
      </footer>
      <v-card-actions>
        <v-spacer />
        <v-btn dark color="blue darken-1" @click="closeDetailedScoreboardDialog"
          >close</v-btn
        >
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Vue, Prop } from 'vue-property-decorator';
import TournamentScoreboard from '@/models/tournament/TournamentScoreboard';
import ShowScores from '@/views/student/tournament/ShowScores.vue';

@Component({
  components: {
    'show-scores': ShowScores
  }
})
export default class ShowDetailedScoreboardDialog extends Vue {
  @Prop({ type: TournamentScoreboard, required: true })
  readonly scoreboard!: TournamentScoreboard;
  @Prop({ type: Boolean, required: true }) readonly dialog!: boolean;

  closeDetailedScoreboardDialog() {
    this.$emit('close-show-detailed-scoreboard-dialog');
  }
}
</script>

<style lang="scss" scoped>
footer {
  font-size: 14px !important;
}

span {
  margin: auto;
}
</style>

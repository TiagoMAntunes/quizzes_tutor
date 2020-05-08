<template>
  <tournament-list
    @joinTournament="joinTournament"
    @cancelTournament="cancelTournament"
    :tournaments="tournaments"
  />
</template>

<script lang="ts">
import moment from 'moment';
import { Component, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import Tournament from '@/models/tournament/Tournament';
import Topic from '@/models/management/Topic';
import TournamentList from '@/views/student/tournament/TournamentList.vue';

@Component({
  components: {
    TournamentList
  }
})
export default class AvailableTournamentsView extends Vue {
  tournaments: Tournament[] = [];
  updatedTournament: Tournament | undefined;

  async created() {
    await this.$store.dispatch('loading');
    try {
      this.tournaments = await RemoteServices.getOpenTournaments();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  async cancelTournament(tournamentId: number) {
    if (confirm('Are you sure you want to cancel this tournament?')) {
      try {
        await RemoteServices.cancelTournament(tournamentId);
        this.tournaments = this.tournaments.filter(
          tournament => tournament.id !== tournamentId
        );
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }
  }

  async joinTournament(tournamentId: number) {
    try {
      this.updatedTournament = await RemoteServices.joinTournament(
        tournamentId
      );
      this.tournaments = this.tournaments.filter(
        tournament => tournament.id !== tournamentId
      );
      this.tournaments.unshift(this.updatedTournament);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }
}
</script>

<style lang="scss" scoped></style>

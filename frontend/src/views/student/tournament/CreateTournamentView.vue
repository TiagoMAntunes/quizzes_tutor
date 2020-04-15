<template>
  <div class="container">
    <h2>Create Tournament</h2>
    <v-container class="create-form">
      <v-row justify="center">
        <v-col>
          <v-select
            chips
            deletable-chips
            multiple
            :items="availableTopics"
            item-text="name"
            label="Select the tournament topics"
            v-model="tournamentManager.topics"
            return-object
            persistent-hint
            dropzone
          ></v-select>
        </v-col>
      </v-row>
      <v-row justify="center">
        <v-col>
          <v-datetime-picker
            label="Start time"
            format="yyyy-MM-dd HH:mm"
            date-format="yyyy-MM-dd"
            time-format="HH:mm"
            v-model="tournamentManager.startTime"
          ></v-datetime-picker>
        </v-col>
        <v-col>
          <v-datetime-picker
            label="Finish time"
            format="yyyy-MM-dd HH:mm"
            date-format="yyyy-MM-dd"
            time-format="HH:mm"
            v-model="tournamentManager.finishTime"
          ></v-datetime-picker>
        </v-col>
      </v-row>
      <v-container>
        <p>Number of Questions</p>
        <v-btn-toggle mandatory class="button-group" v-model="tournamentManager.numberOfQuestions">
          <v-btn text data-cy="select5Questions" value="5">5</v-btn>
          <v-btn text data-cy="select10Questions" value="10">10</v-btn>
          <v-btn text data-cy="select20Questions" value="20">20</v-btn>
        </v-btn-toggle>
      </v-container>
      <v-container>
        <v-btn color="primary" @click="createTournament"
          >Create Tournament</v-btn
        >
      </v-container>
    </v-container>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import Topic from '../../../models/management/Topic';
import RemoteServices from '../../../services/RemoteServices';
import TournamentSkeleton from '@/models/tournament/TournamentSkeleton';

@Component
export default class CreateTournamentView extends Vue {
  availableTopics: Topic[] = [];
  selected: Topic[] = [];
  tournamentManager: TournamentSkeleton = new TournamentSkeleton();

  async created() {
    await this.$store.dispatch('loading');
    this.tournamentManager.reset();
    try {
      this.availableTopics = await RemoteServices.getTopics();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  async createTournament() {
    this.tournamentManager
      .createTournament()
      .then(() => {
        this.$router.push({ name: 'home' }); //TODO change to list tournaments when finished
      })
      .catch(err => {
        this.$store.dispatch('error', err);
      });
  }
}
</script>
<style lang="scss" scoped>
.create-form {
  width: 80% !important;
  background-color: white;
  border-width: 10px;
  border-style: solid;
  border-color: #818181;
}
</style>

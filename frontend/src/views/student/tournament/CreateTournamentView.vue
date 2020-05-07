<template>
  <div class="container">
    <h2>Create Tournament</h2>
    <v-container class="create-form">
      <v-row justify="center">
        <v-text-field
          class="title"
          v-model="tournamentManager.title"
          label="*Title"
          data-cy="title"
        />
      </v-row>
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
          <VueCtkDateTimePicker
            label="Start time"
            format="YYYY-MM-DDTHH:mm:ssZ"
            id="startTimer"
            v-model="tournamentManager.startTime"
            data-cy="startTimePicker"
          ></VueCtkDateTimePicker>
        </v-col>
        <v-col>
          <VueCtkDateTimePicker
            label="Finish time"
            format="YYYY-MM-DDTHH:mm:ssZ"
            id="finishTime"
            v-model="tournamentManager.finishTime"
            data-cy="finishTimePicker"
          ></VueCtkDateTimePicker>
        </v-col>
      </v-row>
      <v-container>
        <p>Number of Questions</p>
        <v-btn-toggle
          mandatory
          class="button-group"
          v-model="tournamentManager.numberOfQuestions"
        >
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
        this.$router.push({ name: 'available-tournament' });
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
.title {
  margin: 10px;
}
</style>

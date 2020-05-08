<template>
    <div class="container">
        <h2>Settings</h2>
        <v-container class="settings">
            <v-row justify="center" align="center" data-cy="tournament">
                <v-col>
                    Tournament Privacy
                </v-col>
                <v-col>
                    <v-btn-toggle
                    mandatory
                    class="button-group" 
                    v-model="tournamentPrivacy"
                    >
                        <v-btn width="50%" value="false" data-cy="public" @click="setTournamentPrivacy(false)">Public</v-btn>
                        <v-btn width="50%" value="true" data-cy="private" @click="setTournamentPrivacy(true)">Private</v-btn>
                    </v-btn-toggle>
                </v-col>
            </v-row>
            <v-row justify="center" align="center" data-cy="question">
                <v-col>
                    Question Privacy
                </v-col>
                <v-col>
                    <v-btn-toggle
                            mandatory
                            class="button-group"
                            v-model="questionPrivacy"
                    >
                        <v-btn width="50%" value="false" data-cy="public" @click="setQuestionPrivacy(false)">Public</v-btn>
                        <v-btn width="50%" value="true" data-cy="private" @click="setQuestionPrivacy(true)">Private</v-btn>
                    </v-btn-toggle>
                </v-col>
            </v-row>
            <!-- <v-divider></v-divider> This is good for when extra buttons are added-->
            
        </v-container>
    </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import RemoteServices from '../../services/RemoteServices';

@Component
export default class SettingsView extends Vue {

  tournamentPrivacy : String = "false";
  questionPrivacy : String = "false";


  async created() {
    await this.$store.dispatch('loading');
    
    try {
        this.tournamentPrivacy = String(await RemoteServices.getTournamentPrivacy());
        this.questionPrivacy = String(await RemoteServices.getQuestionPrivacy());

    } catch (error) {
        await this.$store.dispatch('error', error)
    }
    
    await this.$store.dispatch('clearLoading');
  }

  async setTournamentPrivacy(arg : boolean) {
      try {
        await RemoteServices.setTournamentPrivacy(arg);
      } catch (error) {
          await this.$store.dispatch('error', error)
      }
  }
   async setQuestionPrivacy(arg : boolean) {
        try {
            await RemoteServices.setQuestionPrivacy(arg);
        } catch (error) {
            await this.$store.dispatch('error', error)
        }
    }

}
</script>

<style lang="scss" scoped>
.settings {
  width: 50% !important;
  background-color: white;
  border-width: 10px;
  border-style: solid;
  border-color: #818181;
}
.title {
  margin: 10px;
}
</style>

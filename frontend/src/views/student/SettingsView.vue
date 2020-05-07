<template>
    <div class="container">
        <h2>Settings</h2>
        <v-container class="settings">
            <v-row justify="center" align="center">
                <v-col>
                    Tournament Privacy
                </v-col>
                <v-col>
                    <v-btn-toggle
                    mandatory
                    class="button-group" 
                    v-model="tournamentPrivacy"
                    >
                        <v-btn width="50%" value="false" @click="setTournamentPrivacy(false)">Public</v-btn>
                        <v-btn width="50%" value="true" @click="setTournamentPrivacy(true)">Private</v-btn>
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

  async created() {
    await this.$store.dispatch('loading');
    
    try {
        this.tournamentPrivacy = String(await RemoteServices.getTournamentPrivacy());
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

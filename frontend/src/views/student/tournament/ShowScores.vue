<template>
  <v-card class="table">
    <v-data-table
      :headers="headers"
      :custom-filter="customFilter"
      :items="scores"
      :search="search"
      multi-sort
      :mobile-breakpoint="0"
      :items-per-page="15"
      :footer-props="{ itemsPerPageOptions: [15, 30, 50, 100] }"
    >
      <template v-slot:top>
        <v-card-title>
          <v-text-field
            v-model="search"
            append-icon="search"
            label="Search"
            class="mx-2"
          />
        </v-card-title>
      </template>
    </v-data-table>
  </v-card>
</template>

<script lang="ts">
import { Component, Vue, Prop } from 'vue-property-decorator';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import TournamentScore from '@/models/tournament/TournamentScore';

@Component
export default class ShowScores extends Vue {
  @Prop({ type: Array, required: true }) readonly scores!: TournamentScore[];
  search: string = '';

  headers: object = [
    { text: 'Name', value: 'name', align: 'center' },
    { text: 'Score', value: 'score', align: 'center' },
  ];

  customFilter(value: string, search: string, score: TournamentScore) {
    // noinspection SuspiciousTypeOfGuard,SuspiciousTypeOfGuard
    return (
      search != null &&
      JSON.stringify(score)
        .toLowerCase()
        .indexOf(search.toLowerCase()) !== -1
    );
  }
}
</script>

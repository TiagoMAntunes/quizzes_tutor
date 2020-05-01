<template>
  <div class="container">
    <h2>Create Question</h2>
    <v-container class="create-form">
      <v-flex xs24 sm12 md8>
           <v-text-field v-model="questionManager.title" label="Title" data-cy="Title" />
      </v-flex>
      <v-flex xs24 sm12 md12>
            <v-textarea
              outline
              rows="10"
              v-model="questionManager.content"
              label="Question"
              data-cy="Question"
            ></v-textarea>
          </v-flex>
          <v-flex
            xs24
            sm12
            md12
            v-for="index in questionManager.options.length"
            :key="index"
          >
            <v-switch
              v-model="questionManager.options[index - 1].correct"
              class="ma-4"
              label="Correct"
              :data-cy="'Correct' + index"
            />
            <v-textarea
              outline
              rows="10"
              v-model="questionManager.options[index - 1].content"
              label="Option Content"
              :data-cy="'Option' + index"
            ></v-textarea>
          </v-flex>
      <v-btn color="blue darken-1" @click="createQuestion">Save</v-btn>
    </v-container>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import RemoteServices from '../../../services/RemoteServices';
import Question from '@/models/management/Question';

@Component
export default class CreateStudentQuestionView extends Vue {
  questionManager: Question = new Question();

  async createQuestion() {
      try {
        await RemoteServices.createStudentQuestion(this.questionManager)
        .then(() => {
            this.$router.push({ name: 'questions-status' });
         });
      } catch (error) {
        // returns error "Missing information for quiz". probably a copy-paste.
        await this.$store.dispatch('error', error);
      }
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

<template>
  <v-dialog
    :value="dialog"
    @input="$emit('dialog', false)"
    @keydown.esc="$emit('dialog', false)"
    max-width="75%"
    max-height="80%"
  >
    <v-card>
      <v-card-title>
        <span class="headline">
          {{
            editQuestion && editQuestion.id === null
              ? 'New Question'
              : 'Edit Question'
          }}
        </span>
      </v-card-title>

      <v-card-text class="text-left" v-if="editQuestion">
        <v-text-field v-model="editQuestion.title" label="Title" />
        <v-textarea
          outline
          rows="10"
          v-model="editQuestion.content"
          label="Question"
        ></v-textarea>
        <div v-for="index in editQuestion.options.length" :key="index">
          <v-switch
            v-model="editQuestion.options[index - 1].correct"
            class="ma-4"
            label="Correct"
          />
          <v-textarea
            outline
            rows="10"
            v-model="editQuestion.options[index - 1].content"
            :label="`Option ${index}`"
          ></v-textarea>
        </div>
      </v-card-text>

      <v-card-actions>
        <v-spacer />
        <v-btn color="blue darken-1" @click="$emit('dialog', false)"
          >Cancel</v-btn
        >
        <v-btn color="blue darken-1" @click="saveStudentQuestion">Save</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Model, Prop, Vue, Watch } from 'vue-property-decorator';
import StudentQuestion from '@/models/management/StudentQuestion';
import RemoteServices from '@/services/RemoteServices';

@Component
export default class EditQuestionDialog extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop({ type: StudentQuestion, required: true }) readonly question!: StudentQuestion;

  editQuestion!: StudentQuestion;

  created() {
    this.updateQuestion();
  }

  @Watch('question', { immediate: true, deep: true })
  updateQuestion() {
    this.editQuestion = new StudentQuestion(this.question);
  }


  async saveStudentQuestion() {
    if (
      this.editQuestion &&
      (!this.editQuestion.title || !this.editQuestion.content)
    ) {
      await this.$store.dispatch(
        'error',
        'Question must have title and content'
      );
      return;
    }

    try {
      const result = await RemoteServices.updateStudentQuestion(this.editQuestion)

      this.$emit('save', result);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }
}
</script>

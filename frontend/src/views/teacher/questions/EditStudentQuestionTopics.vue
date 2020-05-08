<template>
  <v-form>
    <v-autocomplete
      v-model="studentQuestionTopics"
      :items="topics"
      multiple
      return-object
      item-text="name"
      item-value="name"
      @change="saveTopics"
    >
      <template v-slot:selection="data">
        <v-chip
          v-bind="data.attrs"
          :input-value="data.selected"
          close
          @click="data.select"
          @click:close="removeTopic(data.item)"
        >
          {{ data.item.name }}
        </v-chip>
      </template>
      <template v-slot:item="data">
        <v-list-item-content>
          <v-list-item-title v-html="data.item.name" />
        </v-list-item-content>
      </template>
    </v-autocomplete>
  </v-form>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator';
import Topic from '@/models/management/Topic';
import StudentQuestion from '@/models/management/StudentQuestion';
import RemoteServices from '@/services/RemoteServices';

@Component
export default class EditStudentQuestionTopics extends Vue {
  @Prop({ type: StudentQuestion, required: true })
  readonly question!: StudentQuestion;
  @Prop({ type: Array, required: true }) readonly topics!: Topic[];

  studentQuestionTopics: Topic[] = JSON.parse(
    JSON.stringify(this.question.topics)
  );

  async saveTopics() {
    if (this.question.id) {
      try {
        await RemoteServices.updateQuestionTopics(
          this.question.id,
          this.studentQuestionTopics
        );
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }

    this.$emit(
      'question-changed-topics',
      this.question.id,
      this.studentQuestionTopics
    );
  }

  removeTopic(topic: Topic) {
    this.studentQuestionTopics = this.studentQuestionTopics.filter(
      element => element.id != topic.id
    );
    this.saveTopics();
  }
}
</script>

<template>
  <v-form>
    <v-autocomplete
      :disabled="isDisabled()"
      v-model="questionTopics"
      :items="topics"
      multiple
      return-object
      item-text="name"
      item-value="name"
      @change="saveTopics"
    >
      <template v-slot:selection="data" :disabled="isDisabled()">
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
      <template v-slot:item="data" data-cy="question-topics-cy">
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
export default class ShowQuestionTopics extends Vue {
  @Prop({ type: StudentQuestion, required: true })
  readonly question!: StudentQuestion;
  @Prop({ type: Array, required: true }) readonly topics!: Topic[];

  questionTopics: Topic[] = JSON.parse(JSON.stringify(this.question.topics));

  async saveTopics() {
    if (this.question.id && this.question.questionStatus != 'APPROVED') {
      try {
        await RemoteServices.updateQuestionTopics(
          this.question.id,
          this.questionTopics
        );
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }

    this.$emit(
      'question-changed-topics',
      this.question.id,
      this.questionTopics
    );
  }

  removeTopic(topic: Topic) {
    this.questionTopics = this.questionTopics.filter(
      element => element.id != topic.id
    );
    this.saveTopics();
  }

  isDisabled() {
    if (this.question.questionStatus === 'APPROVED') {
      return true;
    } else {
      return false;
    }
  }
}
</script>

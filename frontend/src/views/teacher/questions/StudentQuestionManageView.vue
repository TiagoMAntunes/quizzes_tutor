<template>
  <v-card class="table">
    <v-data-table
      :headers="headers"
      :custom-filter="customFilter"
      :items="questions"
      :search="search"
      :explanation="explanation"
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

      <!-- This action should be in a dialog  -->
      <template v-slot:item.rejectionExplanation="{ item }">
        <v-text-field
          :disabled="isDisabled(item.id, item)"
          v-model="item.rejectionExplanation"
          name="rejectionExplanation"
          type="string"
          data-cy="Explanation"
          @change="setExplanation(item.id, item.rejectionExplanation)"
        ></v-text-field>
      </template>

      <template v-slot:item.content="{ item }">
        <p
          v-html="convertMarkDownNoFigure(item.content, null)"
          @click="showStudentQuestionDialog(item)"
      /></template>

      <template v-slot:item.difficulty="{ item }">
        <v-chip
          v-if="item.difficulty"
          :color="getDifficultyColor(item.difficulty)"
          dark
          >{{ item.difficulty + '%' }}</v-chip
        >
      </template>

      <!-- The teacher shouldn't be able to change the submitted question -->
      <template v-slot:item.topics="{ item }">
        <edit-student-question-topics
          :question="item"
          :topics="topics"
          v-on:question-changed-topics="onQuestionChangedTopics"
        />
      </template>

      <template v-slot:item.questionStatus="{ item }">
        <v-select
          data-cy="questionStatusCy"
          v-model="item.questionStatus"
          :items="statusList"
          dense
          @change="
            setStatus(item.id, item.questionStatus), isDisabled(item.id, item)
          "
          >visibility
          <template v-slot:selection="{ item }">
            <v-chip :color="getStatusColor(item)" small>
              <span>{{ item }}</span>
            </v-chip>
          </template>
        </v-select>
      </template>

      <template v-slot:item.action="{ item }">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon
              small
              class="mr-2"
              v-on="on"
              @click="showStudentQuestionDialog(item)"
              >visibility</v-icon
            >
          </template>
          <span>Show Question</span>
        </v-tooltip>
      </template>
    </v-data-table>
    <show-question-dialog
      v-if="currentQuestion"
      :dialog="questionDialog"
      :question="currentQuestion"
      v-on:close-show-question-dialog="onCloseShowStudentQuestionDialog"
    />
  </v-card>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import { convertMarkDownNoFigure } from '@/services/ConvertMarkdownService';
import StudentQuestion from '@/models/management/StudentQuestion';
import Image from '@/models/management/Image';
import Topic from '@/models/management/Topic';
import ShowStudentQuestionDialog from '@/views/student/questions/ShowStudentQuestionDialog.vue';
import EditStudentQuestionTopics from '@/views/teacher/questions/EditStudentQuestionTopics.vue';

@Component({
  components: {
    'show-question-dialog': ShowStudentQuestionDialog,
    'edit-student-question-topics': EditStudentQuestionTopics
  }
})
export default class StudentQuestionManageView extends Vue {
  questions: StudentQuestion[] = [];
  topics: Topic[] = [];
  currentQuestion: StudentQuestion | null = null;
  questionDialog: boolean = false;
  search: string = '';
  explanation: string = 'No explanation';
  statusList = ['APPROVED', 'REJECTED', 'PENDING'];

  headers: object = [
    { text: 'Title', value: 'title', align: 'center' },
    { text: 'Question', value: 'content', align: 'left' },
    {
      text: 'Topics',
      value: 'topics',
      align: 'center',
      sortable: false
    },
    { text: 'Difficulty', value: 'difficulty', align: 'center' },
    { text: 'Status', value: 'questionStatus', align: 'center' },
    { text: 'Explanation', value: 'rejectionExplanation', align: 'center' },
    {
      text: 'Creation Date',
      value: 'creationDate',
      align: 'center'
    },
    {
      text: 'Actions',
      value: 'action',
      align: 'center',
      sortable: false
    }
  ];

  async created() {
    await this.$store.dispatch('loading');
    try {
      [this.topics, this.questions] = await Promise.all([
        RemoteServices.getTopics(),
        RemoteServices.getAllStudentQuestions()
      ]);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  customFilter(value: string, search: string, question: StudentQuestion) {
    // noinspection SuspiciousTypeOfGuard,SuspiciousTypeOfGuard
    return (
      search != null &&
      JSON.stringify(question)
        .toLowerCase()
        .indexOf(search.toLowerCase()) !== -1
    );
  }

  convertMarkDownNoFigure(text: string, image: Image | null = null): string {
    return convertMarkDownNoFigure(text, image);
  }

  onQuestionChangedTopics(questionId: Number, changedTopics: Topic[]) {
    let question = this.questions.find(
      (question: StudentQuestion) => question.id == questionId
    );
    if (question) {
      question.topics = changedTopics;
    }
  }

  getDifficultyColor(difficulty: number) {
    if (difficulty < 25) return 'green';
    else if (difficulty < 50) return 'lime';
    else if (difficulty < 75) return 'orange';
    else return 'red';
  }

  getStatusColor(status: string) {
    if (status === 'REJECTED') return 'red';
    else if (status === 'PENDING') return 'orange';
    else return 'green';
  }

  async setStatus(questionId: number, status: string) {
    try {
      await RemoteServices.setStudentQuestionStatus(questionId, status);
      let question = this.questions.find(
        question => question.id === questionId
      );
      if (question) {
        question.questionStatus = status;
      }
      if (status === 'APPROVED' || status === 'PENDING') {
        await RemoteServices.setStudentQuestionExplanation(
          questionId,
          'No explanation'
        );
      }
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }

  showStudentQuestionDialog(question: StudentQuestion) {
    this.currentQuestion = question;
    this.questionDialog = true;
  }

  isDisabled(questionId: number, question: StudentQuestion) {
    return question.questionStatus !== 'REJECTED';
  }

  async setExplanation(questionId: number, explanation: string) {
    try {
      await RemoteServices.setStudentQuestionExplanation(
        questionId,
        explanation
      );
      let question = this.questions.find(
        question => question.id === questionId
      );
      if (question) {
        question.rejectionExplanation = explanation;
      }
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }

  onCloseShowStudentQuestionDialog() {
    this.questionDialog = false;
  }
}
</script>

<style lang="scss" scoped>
.question-textarea {
  text-align: left;

  .CodeMirror,
  .CodeMirror-scroll {
    min-height: 200px !important;
  }
}
.option-textarea {
  text-align: left;

  .CodeMirror,
  .CodeMirror-scroll {
    min-height: 100px !important;
  }
}
</style>

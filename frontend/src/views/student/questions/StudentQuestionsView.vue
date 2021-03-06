<template>
  <v-card class="table">
    <v-data-table
      :headers="headers"
      :custom-filter="customFilter"
      :items="questions"
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

      <template v-slot:item.content="{ item }">
        <p
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

      <template v-slot:item.questionStatus="{ item }">
        <v-chip
          v-if="item.questionStatus"
          :color="getStatusColor(item.questionStatus)"
          small
          ><span>{{ item.questionStatus }}</span></v-chip
        >
      </template>

      <template v-slot:item.topics="{ item }">
        <v-card-title :disabled="isDisabled(item)">
          <show-student-question-topics
            :question="item"
            :topics="topics"
            data-cy="topicsCy"
          />
        </v-card-title>
      </template>

      <template v-slot:item.action="{ item }">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon
              large
              data-cy="showQuestionCy"
              class="mr-2"
              v-on="on"
              @click="showStudentQuestionDialog(item)"
              >visibility</v-icon
            >
          </template>
          <span>Show Question</span>
        </v-tooltip>
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon
              large
              :disabled="isDisabledUpdate(item)"
              data-cy="updateQuestionCy"
              class="mr-2"
              v-on="on"
              @click="editQuestion(item)"
            >edit</v-icon
            >
          </template>
          <span>Update Question</span>
        </v-tooltip>
      </template>
    </v-data-table>
    <update-student-question-dialog
            v-if="currentQuestion"
            v-model="updateStudentQuestionDialog"
            :question="currentQuestion"
            v-on:save-question="onSaveQuestion"
    />
    <show-student-question-dialog
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
import StudentQuestion from '@/models/management/StudentQuestion';
import Topic from '@/models/management/Topic';
import ShowStudentQuestionDialog from '@/views/student/questions/ShowStudentQuestionDialog.vue';
import ShowStudentQuestionTopics from '@/views/student/questions/ShowStudentQuestionTopics.vue';
import Question from '@/models/management/Question';
import UpdateStudentQuestionDialog from '@/views/student/questions/UpdateStudentQuestionDialog.vue';

@Component({
  components: {
    'show-student-question-dialog': ShowStudentQuestionDialog,
    'show-student-question-topics': ShowStudentQuestionTopics,
    'update-student-question-dialog': UpdateStudentQuestionDialog
  }
})
export default class StudentQuestionsView extends Vue {
  questions: StudentQuestion[] = [];
  topics: Topic[] = [];
  currentQuestion: StudentQuestion | null = null;
  questionDialog: boolean = false;
  search: string = '';
  updateStudentQuestionDialog : boolean = false;

  headers: object = [
    {
      text: 'Actions',
      value: 'action',
      align: 'center',
      sortable: false
    },
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
    }
  ];
  @Watch('updateStudentQuestionDialog')
  closeError() {
    if (!this.updateStudentQuestionDialog) {
      this.currentQuestion = null;
    }
  }
  async created() {
    await this.$store.dispatch('loading');
    try {
      [this.topics, this.questions] = await Promise.all([
        RemoteServices.getTopics(),
        RemoteServices.getStudentQuestions()
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

  isDisabled(question: StudentQuestion) {
    return question.questionStatus == 'APPROVED';
  }

  showStudentQuestionDialog(studentQuestion: StudentQuestion) {
    this.currentQuestion = studentQuestion;
    this.questionDialog = true;
  }

  onCloseShowStudentQuestionDialog() {
    this.questionDialog = false;
  }
  isDisabledUpdate(question: StudentQuestion) {
    return question.questionStatus != 'REJECTED';
  }
  editQuestion(studentQuestion: StudentQuestion, e?: Event) {
    if (e) e.preventDefault();
    this.currentQuestion = studentQuestion;
    this.updateStudentQuestionDialog = true;
  }

  async onSaveQuestion(studentQuestion: StudentQuestion) {
    this.questions = this.questions.filter(q => q.id !== studentQuestion.id);
    this.questions.unshift(studentQuestion);
    this.updateStudentQuestionDialog = false;
    this.currentQuestion = null;
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

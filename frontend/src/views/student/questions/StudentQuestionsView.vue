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
          <show-student-question-topics :question="item" :topics="topics" data-cy="topicsCy"/>
        </v-card-title>
      </template>

      <template v-slot:item.action="{ item }">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon
              small
              data-cy="showQuestionCy"
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
import { convertMarkDownNoFigure } from '@/services/ConvertMarkdownService';
import StudentQuestion from '@/models/management/StudentQuestion';
import Image from '@/models/management/Image';
import Topic from '@/models/management/Topic';
import ShowStudentQuestionDialog from '@/views/student/questions/ShowStudentQuestionDialog.vue';
import ShowStudentQuestionTopics from '@/views/student/questions/ShowStudentQuestionTopics.vue';

@Component({
  components: {
    'show-student-question-dialog': ShowStudentQuestionDialog,
    'show-student-question-topics': ShowStudentQuestionTopics
  }
})
export default class StudentQuestionsView extends Vue {
  questions: StudentQuestion[] = [];
  topics: Topic[] = [];
  currentQuestion: StudentQuestion | null = null;
  questionDialog: boolean = false;
  search: string = '';

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

  convertMarkDownNoFigure(text: string, image: Image | null = null): string {
    return convertMarkDownNoFigure(text, image);
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
  }

  showStudentQuestionDialog(studentQuestion: StudentQuestion) {
    this.currentQuestion = studentQuestion;
    this.questionDialog = true;
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

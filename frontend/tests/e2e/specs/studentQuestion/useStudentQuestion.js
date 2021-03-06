let TITLE = 'Test Title'
let QUESTION = 'Test Question'
let OPTIONS =['Option1','Option2','Option3', 'Option4']
let TITLE2 = 'New Title'
let QUESTION2 = 'New Question'

describe('Student Questions walkthrough', () => {
  afterEach(() => {
    cy.contains('Logout').click();
  });

  it('Creates a good Question', () => {
    cy.demoStudentLogin()
    cy.createStudentQuestion(TITLE, QUESTION, OPTIONS)
  });

  it('Creates a Question without title', () => {
    cy.demoStudentLogin()
    cy.createStudentQuestion( '', QUESTION, OPTIONS)
    cy.contains('Invalid title for question')
    cy.closeErrorMessage()
  });


  it('Creates a Question without name', () => {
    cy.demoStudentLogin()
    cy.createStudentQuestion(TITLE, '', OPTIONS)
    cy.contains('Invalid content for question')
    cy.closeErrorMessage()
  });

  it('Get the questions and show a question', () => {
    cy.demoStudentLogin()
    cy.openAvailableQuestions();
    cy.showStudentQuestion(TITLE);
  });

  it('Add a topic to the question', () => {
    cy.demoStudentLogin()
    cy.openAvailableQuestions();
    cy.addTopicStudentQuestion(TITLE,'Adventure Builder');
  });

  it('Reject a student question and add an explanation', () => {
    cy.demoTeacherLogin()
    cy.openTeacherStudentQuestions();
    cy.statusQuestion(TITLE, 'REJECTED');
    cy.addExplanation(TITLE, 'not good');
  });

  it('Approve a student question', () => {
    cy.demoTeacherLogin()
    cy.openTeacherStudentQuestions();
    cy.statusQuestion(TITLE, 'APPROVED');
  });

  it('Edit a question properly', () => {
    cy.demoTeacherLogin()
    cy.openTeacherStudentQuestions();
    cy.editQuestion(TITLE2, QUESTION2, OPTIONS);
  });

  it('Make question available', () => {
    cy.demoTeacherLogin()
    cy.openTeacherStudentQuestions();
    cy.makeAvailable();
  });

  it('Make a resubmit of a rejected question', () => {
    cy.demoTeacherLogin();
    cy.openTeacherStudentQuestions();
    cy.statusQuestion(TITLE, 'REJECTED');
    cy.logout();
    cy.demoStudentLogin();
    cy.contains('Questions').click();
    cy.get('[data-cy="availableQuestions"]').click();
    cy.resubmitStudentQuestion(TITLE);
    cy.contains('Save').click();
  });
});

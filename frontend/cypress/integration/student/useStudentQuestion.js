let TITLE = 'Test Title'
let QUESTION = 'Test Question'
let OPTIONS =['Option1','Option2','Option3', 'Option4']

describe('Student Questions walkthrough', () => {

  it('Creates a good Question', () => {
    cy.demoStudentLogin()
    cy.createStudentQuestion(TITLE, QUESTION, OPTIONS)
    cy.contains('Logout').click()
  });

  /*it('Creates a Question without title', () => {
    cy.demoStudentLogin()
    cy.createStudentQuestion( '', QUESTION, OPTIONS)
    cy.contains('Missing information for quiz')
    cy.closeErrorMessage()
    cy.contains('Logout').click()
  });


  it('Creates a Question without name', () => {
    cy.demoStudentLogin()
    cy.createStudentQuestion(TITLE, '', OPTIONS)
    cy.contains('Missing information for quiz')
    cy.closeErrorMessage()
    cy.contains('Logout').click()
  });

  it('Creates a Question without options', () => {
    cy.demoStudentLogin()
    cy.createStudentQuestion(TITLE, QUESTION, [])
    cy.contains('Missing information for quiz')
    cy.closeErrorMessage()
    cy.contains('Logout').click()
  });*/

  it('Get the questions and show a question', () => {
    cy.demoStudentLogin()
    cy.openAvailableQuestions();
    cy.showStudentQuestion(TITLE);
    cy.contains('Logout').click()
  });

  it ('Add a topic to the question', () => {
    cy.demoStudentLogin()
    cy.openAvailableQuestions();
    cy.addTopicStudentQuestion(TITLE,'Adventure Builder');
    cy.contains('Logout').click()
  });

  it ('Reject a student question and add an explanation', () => {
    cy.demoTeacherLogin()
    cy.openTeacherStudentQuestions();
    cy.rejectQuestion(TITLE);
    cy.addExplanation(TITLE, 'not good');
    cy.contains('Logout').click()
  });

});

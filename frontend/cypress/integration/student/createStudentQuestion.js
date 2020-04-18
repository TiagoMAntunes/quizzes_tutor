let TITLE = 'Test Title'
let QUESTION = 'Test Question'
let OPTIONS =['Option1','Option2','Option3', 'Option4']

describe('Administration walkthrough', () => {
  beforeEach(() => {
    cy.demoStudentLogin()
  })

  afterEach(() => {
    cy.contains('Logout').click()
  })

  it('Creates a good Question', () => {
    cy.createStudentQuestion(TITLE, QUESTION, OPTIONS)

  });


  it('Creates a Question without title', () => {
    cy.createStudentQuestion( '', QUESTION, OPTIONS)
    cy.contains('Missing information for quiz')
    cy.closeErrorMessage()
  });


  it('Creates a Question without name', () => {
    cy.createStudentQuestion(TITLE, '', OPTIONS)
    cy.contains('Missing information for quiz')
    cy.closeErrorMessage()
  });

  it('Creates a Question without options', () => {
    cy.createStudentQuestion(TITLE, QUESTION, [])
    cy.contains('Missing information for quiz')
    cy.closeErrorMessage()
  });

});

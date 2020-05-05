// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add("login", (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add("drag", { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add("dismiss", { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite("visit", (originalFn, url, options) => { ... })
/// <reference types="Cypress" />
Cypress.Commands.add('demoAdminLogin', () => {
  cy.visit('/');
  cy.get('[data-cy="demoAdminLoginButton"]').click();
  cy.contains('Administration').click();
  cy.contains('Manage Courses').click();
});

Cypress.Commands.add('demoStudentLogin', () => {
  cy.visit('/');
  cy.get('[data-cy="demoStudentLoginButton"]').click();
});

Cypress.Commands.add('demoTeacherLogin', () => {
  cy.visit('/');
  cy.get('[data-cy="demoTeacherLoginButton"]').click();
});

Cypress.Commands.add('createCourseExecution', (name, acronym, academicTerm) => {
  cy.get('[data-cy="createButton"]').click();
  cy.get('[data-cy="courseExecutionNameInput"]').type(name);
  cy.get('[data-cy="courseExecutionAcronymInput"]').type(acronym);
  cy.get('[data-cy="courseExecutionAcademicTermInput"]').type(academicTerm);
  cy.get('[data-cy="saveButton"]').click();
});

Cypress.Commands.add('closeErrorMessage', (name, acronym, academicTerm) => {
  cy.contains('Error')
    .parent()
    .find('button')
    .click();
});

Cypress.Commands.add('deleteCourseExecution', acronym => {
  cy.contains(acronym)
    .parent()
    .should('have.length', 1)
    .children()
    .should('have.length', 7)
    .find('[data-cy="deleteCourse"]')
    .click();
});

Cypress.Commands.add(
  'createFromCourseExecution',
  (name, acronym, academicTerm) => {
    cy.contains(name)
      .parent()
      .should('have.length', 1)
      .children()
      .should('have.length', 7)
      .find('[data-cy="createFromCourse"]')
      .click();
    cy.get('[data-cy="courseExecutionAcronymInput"]').type(acronym);
    cy.get('[data-cy="courseExecutionAcademicTermInput"]').type(academicTerm);
    cy.get('[data-cy="saveButton"]').click();
  }
);

Cypress.Commands.add('openAvailableQuestions', () => {
  cy.contains('Questions').click();
  cy.get('[data-cy="availableQuestions"]').click();
});

Cypress.Commands.add('openTeacherStudentQuestions', () => {
  cy.contains('Management').click();
  cy.get('[data-cy="availableStudentQuestions"]').click();
});

Cypress.Commands.add('showStudentQuestion', title => {
  cy.contains(title)
    .parent()
    .should('have.length', 1)
    .children()
    .should('have.length', 8)
    .find('[data-cy="showQuestionCy"]')
    .click({ force: true });
});

Cypress.Commands.add('addTopicStudentQuestion', (title, topic) => {
  cy.contains(title)
    .parent()
    .should('have.length', 1)
    .children()
    .should('have.length', 8)
    .find(
      '[data-cy="topicsCy"] > .v-input > .v-input__control > .v-input__slot > .v-select__slot'
    )
    .click({ force: true });
  cy.contains(topic).click({ force: true });
});

Cypress.Commands.add('createStudentQuestion', (title, question, options) => {
  cy.contains('Questions').click();
  cy.contains('Create').click();
  if (title != '') cy.get('[data-cy="Title"]').type(title);
  if (question != '') cy.get('[data-cy="Question"]').type(question);
  cy.get('[data-cy="Correct1"]').click({ force: true });
  for (let option of options)
    cy.get('[data-cy="' + option + '"]').type(option + 'Test');
  cy.contains('Save').click();
});

Cypress.Commands.add(
  'createTournament',
  (topics, startDay, finishDay, numberOfQuestions) => {
    cy.contains('Select the tournament topics')
      .parent()
      .click();
    for (let topic of topics) cy.contains(topic).click(); //selects the given topics from the list

    cy.get('[data-cy="startTimePicker"')
      .click({ force: true })
      .parent()
      .parent() // Open menu
      .find('.datepicker-next')
      .click() // go to next month
      .wait(1000); // needs to wait for load

    cy.get('[data-cy="startTimePicker"')
      .parent()
      .parent()
      .find('.month-container') // select dates
      .contains(startDay)
      .click();

    cy.get('[data-cy="startTimePicker"')
      .parent()
      .parent()
      .find('.validate')
      .click();

    cy.get('[data-cy="finishTimePicker"')
      .click({ force: true })
      .parent()
      .parent() // Open menu
      .find('.datepicker-next')
      .click() // go to next month
      .wait(1000); // needs to wait for load

    cy.get('[data-cy="finishTimePicker"')
      .parent()
      .parent()
      .find('.month-container') // select dates
      .contains(finishDay)
      .click();

    cy.get('[data-cy="finishTimePicker"')
      .parent()
      .parent()
      .find('.validate')
      .click();

    cy.get('[data-cy="select' + numberOfQuestions + 'Questions"]').click({
      force: true
    });

    cy.get('button')
      .contains('Create Tournament')
      .click({ force: true });
  }
);

Cypress.Commands.add('openCreateTournament', () => {
  cy.contains('Tournaments').click();
  cy.get('[data-cy="createTournament"]').click();
});

Cypress.Commands.add('statusQuestion', (title, status) => {
  cy.get(
    ':nth-child(1) > :nth-child(5) > .v-input > .v-input__control > .v-input__slot > .v-select__slot > .v-select__selections > .v-chip'
  ).click({ force: true });
  cy.contains(status).click({ force: true });
});

Cypress.Commands.add('addExplanation', (title, explanation) => {
  cy.get(
    ':nth-child(1) > :nth-child(6) > .v-input > .v-input__control > .v-input__slot > .v-text-field__slot > [data-cy=Explanation]'
  )
    .click({ force: true })
    .clear()
    .type(explanation);
});

Cypress.Commands.add('makeAvailable', () => {
  cy.get(':nth-child(1) > .text-left > .fas').click({ force: true });
});

Cypress.Commands.add('openAvailableTournaments', () => {
  cy.contains('Tournaments').click();
  cy.get('[data-cy="availableTournaments"]').click();
});

Cypress.Commands.add('joinTournament', () => {
  cy.get(':nth-child(2) > .last-col > .fas').click();
});

Cypress.Commands.add('cancelTournament', () => {
  cy.get(':nth-child(2) > .last-col > .v-icon').click();
});

let START_DAY = 10
let FINISH_DAY = 11
let NQUESTIONS = 20;
let TOPICS = ['Adventure Builder', 'Availability', 'Graphite']

describe('Student using tournaments walkthrough', () => {
    beforeEach(() => {
        cy.demoStudentLogin()
    })

    afterEach(() => {
        cy.contains('Logout').click()
    })

    it('Create a good tournament', () => {
        cy.openCreateTournament();
        cy.createTournament(TOPICS, START_DAY, FINISH_DAY, NQUESTIONS)
    })

    it('Create tournament without topics', () => {
        cy.openCreateTournament()
        cy.createTournament([], START_DAY, FINISH_DAY, NQUESTIONS)
        cy.contains('Tournament requires at least one topic to be valid')
        cy.closeErrorMessage()
    })

    it('Create tournament with finish time before start time', () => {
        cy.openCreateTournament()
        cy.createTournament(TOPICS, FINISH_DAY, START_DAY, NQUESTIONS)
        cy.contains('Start time of a tournament must be before finish time')
        cy.closeErrorMessage()
    })
})
let TITLE = 'Tournament title'
let START_DAY = 10
let FINISH_DAY = 11
let NQUESTIONS = 20;
let TOPICS = ['Adventure Builder', 'Availability', 'Graphite']

describe('Student using tournaments walkthrough', () => {
    beforeEach(() => {
        cy.demoStudentLogin();
    })

    afterEach(() => {
        cy.contains('Logout').click();
    })

    it('Create a good tournament', () => {
        cy.openCreateTournament();
        cy.createTournament(TITLE, TOPICS, START_DAY, FINISH_DAY, NQUESTIONS);
    })

    it('Create tournament without topics', () => {
        cy.openCreateTournament();
        cy.createTournament(TITLE, [], START_DAY, FINISH_DAY, NQUESTIONS);
        cy.contains('Tournament requires at least one topic to be valid');
        cy.closeErrorMessage();
    })

    it('Create tournament with finish time before start time', () => {
        cy.openCreateTournament();
        cy.createTournament(TITLE, TOPICS, FINISH_DAY, START_DAY, NQUESTIONS)
        cy.contains('Start time of a tournament must be before finish time');
        cy.closeErrorMessage();
    })

    it('Join created tournament', () => {
        cy.openAvailableTournaments();
        cy.joinTournament();
    })

    it('Cancel created tournament', () => {
        cy.openAvailableTournaments();
        cy.cancelTournament();
    })

    it('Join tournament with 1 member already signed up and answer the quiz associated', () => {

        //Remove tournament from database
        cy.exec("PGPASSWORD=db_pass psql tutordb -U db_admin -c \"DELETE FROM topics_tournaments WHERE tournaments_id = '42069';\"");
        cy.exec("PGPASSWORD=db_pass psql tutordb -U db_admin -c \"DELETE FROM users_signed_up_tournaments WHERE signed_up_tournaments_id = '42069';\"");
        cy.exec("PGPASSWORD=db_pass psql tutordb -U db_admin -c \"DELETE FROM tournaments WHERE id = '42069';\"");

        const startTime = new Date();
        startTime.setSeconds(startTime.getSeconds() + 5);

        const finish_time = new Date(startTime);
        finish_time.setMinutes(finish_time.getSeconds() + 15);

        //Create tournament in database
        cy.exec("PGPASSWORD=db_pass psql tutordb -U db_admin -c \"INSERT INTO tournaments(id, finish_time, number_of_questions, start_time, course_execution_id, creator_id, quiz_id, title) \
        VALUES(42069, '" + finish_time.toISOString() + "', 5, '" + startTime.toISOString() + "', 11, 676, null, '" + TITLE + "'); INSERT INTO topics_tournaments(topics_id, tournaments_id) VALUES(88, 42069);\"");

        //Insert user
        cy.exec("PGPASSWORD=db_pass psql tutordb -U db_admin -c \"INSERT INTO users_signed_up_tournaments VALUES(647, 42069);\"");

        //Join tournament with one participant
        cy.openAvailableTournaments();
        cy.wait(1000); // let everything load
        cy.joinTournament();
        cy.wait(5000); // let everything load

        //Answer quiz
        cy.answerTournamentQuiz();
    })

    
    it('Tournament stats test', () => {
        cy.openStats();
        cy.wait(1000); // let everything load
    })

    it('Check detailed tournament scoreboard', () => {
        cy.exec();
        cy.wait(9000); // let tournament end
    })

})
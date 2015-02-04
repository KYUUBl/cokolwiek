import com.sun.org.apache.xpath.internal.SourceTree;
import utils.AccountType;
import utils.Pair;

import java.util.ArrayList;
import java.util.Scanner;

import static utils.Constants.*;

/**
 * Created by Kamil on 2015-02-04.
 */
public class MainClass implements AdminInterface, StudentInterface, TeacherInterface {

        private DBManager dbManager = new DBManager();
        private Scanner scanner = new Scanner(System.in);
        private User user;

        private Admin admin;
        private Student student;
        private Teacher teacher;

        public void initApplication() {
                String login;
                String password;
                System.out.println("Dziennik Elektroniczny 1.0");
                user = signIn();

                switch(user.getAccountType()) {
                        case ADMIN:
                                break;

                        case TEACHER:
                                break;

                        case STUDENT:
                                studentMain();
                                break;

                        default:
                                throw new RuntimeException("Wrong account type");
                }
        }

        public User signIn() {
                String login;
                String password;
                while(true) {
                        System.out.print("login: ");
                        login = scanner.nextLine();

                        System.out.print("hasło: ");
                        password = scanner.nextLine();

                        if(dbManager.signIn(login, password) != null) {
                                return dbManager.signIn(login, password);
                        }

                        if(login.equals(ADMIN_LOGIN) && password.equals(ADMIN_PASSWORD)) {
                                return new User(ADMIN_ID, AccountType.ADMIN);
                        }
                        System.out.println("Login lub hasło niepoprawne, proszę spróbować ponownie");
                }
        }

        public static void main (String args[]) {

                MainClass mainClass = new MainClass();
                mainClass.initApplication();
                System.out.println(mainClass.user.getAccountType());
        }

        @Override
        public void studentMain() {
                System.out.println("Zalogowano jako uczeń o numerze PESEL:" + user.getId() + "\n" +
                        "Wybierz działanie:");
                System.out.println("[1] Wyświetl oceny");
                System.out.println("[2] Wyświetl nieobecności");
                System.out.println("[3] Wyświetl uwagi");
                int order = scanner.nextInt();

                switch(order) {
                        case 1:
                                getStudentGrades();
                                break;
                        case 2:
                                getStudentAbsences();
                                break;
                        case 3:
                                getStudentNotes();
                                break;
                        default:
                                throw new RuntimeException("Wrong order");
                }
        }

        @Override
        public void AdminMain() {
                System.out.println("Zalogowano jako Admin");

        }

        @Override
        public void teacherMain() {
                System.out.println("Zalogowano jako nauczyciel o ID" + user.getId() + "\n" +
                        "Wybierz działanie:");

                System.out.println("[1] Wyświetl przedmioty");
                System.out.println("[2] Wyświetl ");
                System.out.println("[3] Wyświetl uwagi");
                int order = scanner.nextInt();

                switch(order) {
                        case 1:
                                getStudentGrades();
                                break;
                        case 2:
                                getStudentAbsences();
                                break;
                        case 3:
                                getStudentNotes();
                                break;
                        default:
                                throw new RuntimeException("Wrong order");
                }

        }

        @Override
        public void getStudentGrades() {
                ArrayList<String> subjects = new ArrayList<String>();
                dbManager.getStudentSubjects(user.getId());
                for(int i = 0; i<subjects.size(); i++) {
                        System.out.println("[" + i + "] " + subjects.get(i));
                }
                System.out.println("\n WYBIERZ PRZEDMIOT");
                int order = scanner.nextInt();
                String subject = subjects.get(order);

                ArrayList<String> grades = new ArrayList<String>();
                //dbManager.getStudentGrades(user.getId(), )
        }

        @Override
        public void getStudentAbsences() { //TRZEBA UODPORNIC NA ZJEBOW
                System.out.println("Podaj zakres, z jakiego chcesz otrzymać nieobecności");
                System.out.println("od: DD.MM.RRRR");
                String dateFrom = scanner.nextLine();
                System.out.println("do: DD.MM.RRRR");
                String dateTo = scanner.nextLine();
                ArrayList<String> absences = dbManager.getStudentAbsences(user.getId(), dateFrom, dateTo);

        }

        @Override
        public void getStudentNotes() {

        }


        @Override
        public void getTeacherSubject() {

        }

        @Override
        public void getSubjectStudents() {

        }

        @Override
        public void addStudentGrade() {

        }

        @Override
        public void addStudentNote() {

        }

        @Override
        public void addStudentAbsence() {

        }

        @Override
        public void addCompletedLesson() {

        }


}

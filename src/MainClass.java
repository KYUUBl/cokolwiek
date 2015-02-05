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

        public void initApplication() {
                String login;
                String password;
                System.out.println("Dziennik Elektroniczny 1.0");
                user = signIn();

                switch(user.getAccountType()) {
                        case ADMIN:
                                break;

                        case TEACHER:
                                teacherMain();
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
                System.out.println("Zalogowano jako uczeń o numerze PESEL:" + user.getId());
               while(true) {
                       System.out.println("Wybierz działanie:");
                       System.out.println("[0] Zakończ program");
                       System.out.println("[1] Wyświetl oceny");
                       System.out.println("[2] Wyświetl nieobecności");
                       System.out.println("[3] Wyświetl uwagi");
                       int order = scanner.nextInt();

                       switch (order) {
                               case 1:
                                       getStudentGrades();
                                       break;
                               case 2:
                                       getStudentAbsences();
                                       break;
                               case 3:
                                       getStudentNotes();
                                       break;
                               case 0:
                                       System.out.println("goodbye");
                                       return;
                               default:
                                       throw new RuntimeException("Wrong order");
                       }
               }
        }

        @Override
        public void adminMain() {
                System.out.println("Zalogowano jako Admin \n Wybierz działanie:");
                System.out.println("[1] Zarządzaj bazą");
                System.out.println("[2] Zarządzaj szkołą");
        }

        @Override
        public void manageDatabase() {
                System.out.println("Dodaj nowego użytkownika:");
                System.out.println("[1] Dodaj ucznia");
                System.out.println("[2] Dodaj nauczyciela");

                int order = scanner.nextInt();

                switch(order) {
                        case 1:
                                addUserStudent();
                                break;
                        case 2:
                                addUserTeacher();
                        default:
                                throw new RuntimeException("Wrong order in Admin Panel");
                }
        }

        @Override
        public void manageSchool() {

        }

        @Override
        public void addUserStudent() { //TODO Dokonczyc ten shit
                System.out.println("Podaj login");
                String login = scanner.next();
                System.out.println("Podaj hasło");
                String password = scanner.next();
                System.out.println("Wybierz ucznia");
//                dbManager.getStudents(); //TODO Dodaj tę metodę
        }

        @Override
        public void addUserTeacher() {
                System.out.println("Podaj login");
                String login = scanner.next();
                System.out.println("Podaj hasło");
                String password = scanner.next();
                System.out.println("Wybierz ucznia");
//                dbManager.getTeachers() //TODO Dodaj tę metodę
        }

        @Override
        public void teacherMain() {
                System.out.println("Zalogowano jako nauczyciel o ID" + user.getId() + "\n" +
                        "Wybierz działanie:");

                System.out.println("[1] Dodaj ocene");
                System.out.println("[2] Dodaj uwage");
                System.out.println("[3] Dodaj nieobecnosc");
                int order = scanner.nextInt();

                switch(order) {
                        case 1:
                                addStudentGrade();
                                break;
                        case 2:
                                addStudentNote();
                                break;
                        case 3:
                                addStudentAbsence();
                                break;
                        default:
                                throw new RuntimeException("Wrong order");
                }

        }

        @Override
        public void getStudentGrades() {
                ArrayList<Pair<Integer,String> > subjects = new ArrayList<Pair<Integer,String> >();
                subjects = dbManager.getStudentSubjects(user.getId());
                for(int i = 0; i<subjects.size(); i++) {
                        System.out.println("[" + i + "] " + subjects.get(i).getY());
                }
                System.out.println("\n WYBIERZ PRZEDMIOT");
                int order = scanner.nextInt();
                int subject = subjects.get(order).getX();

                ArrayList<String> grades = new ArrayList<String>();
                grades=dbManager.getStudentGrades(user.getId(),subject );
                for(int i = 0; i<grades.size(); i++) {
                        System.out.println(grades.get(i));
                }

        }

        @Override
        public void getStudentAbsences() { //TRZEBA UODPORNIC NA ZJEBOW
                System.out.println("Podaj zakres, z jakiego chcesz otrzymać nieobecności");
                System.out.println("od: DD.MM.RRRR");
                String dateFrom = scanner.next();
                System.out.println("do: DD.MM.RRRR");
                String dateTo = scanner.next();
                ArrayList<String> absences = dbManager.getStudentAbsences(user.getId(), dateFrom, dateTo);

            for (String s : absences) {
                System.out.println(s);
            }

        }

        @Override
        public void getStudentNotes() {
            System.out.println("Podaj zakres, z jakiego chcesz otrzymać uwagi");
            System.out.println("od: DD.MM.RRRR");
            String dateFrom = scanner.next();
            System.out.println("do: DD.MM.RRRR");
            String dateTo = scanner.next();
            ArrayList<String> notes = dbManager.getStudentNotes(user.getId(), dateFrom, dateTo);

            for (String s : notes) {
                System.out.println(s);
            }

        }


        @Override
        public void getTeacherSubject() {

        }

        @Override
        public void getSubjectStudents() {

        }

        @Override
        public void addStudentGrade() { //TODO Dokonczyc ten shit
                System.out.println("Wybierz przedmiot, z którego chcesz dodać ocenę");
                ArrayList<Pair<Integer, String>> subjects = dbManager.getTeacherSubjects(Integer.parseInt(user.getId()));
                for (int i = 0; i < subjects.size(); i++) {
                        System.out.println("[" + i + "] " + subjects.get(i).getY());
                }
                int order = scanner.nextInt();
                int subjectId = subjects.get(order).getX();

                ArrayList<Pair<String, String>> studentsBySubject = dbManager.getSubjectStudents(subjectId);
                for (int i = 0; i < studentsBySubject.size(); i++) {
                        System.out.println("[" + i + "] " + studentsBySubject.get(i).getY());
                }
                //int


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

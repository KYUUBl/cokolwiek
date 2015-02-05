import com.sun.org.apache.xpath.internal.SourceTree;
import utils.AccountType;
import utils.Pair;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Scanner;

import static utils.Constants.*;


public class MainClass implements AdminInterface, StudentInterface, TeacherInterface {

        private DBManager dbManager = new DBManager();
        private Scanner scanner = new Scanner(System.in);
        private User user;

        public static void main(String args[]) {

                MainClass mainClass = new MainClass();
                mainClass.initApplication();
                System.out.println(mainClass.user.getAccountType());
        }

        public void initApplication() {
                String login;
                String password;
                System.out.println("Dziennik Elektroniczny 1.0");
                user = signIn();

                switch(user.getAccountType()) {
                        case ADMIN:
                                adminMain();
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
                                return new User(ADMIN_ID, AccountType.ADMIN, password);
                        }
                        System.out.println("Login lub hasło niepoprawne, proszę spróbować ponownie");
                }
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
                       System.out.println("[4] Zmień hasło");
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
                               case 4:
                                       changeStudentPassword();
                               case 0:
                                       System.out.println("goodbye");
                                       System.exit(0);
                               default:
                                       throw new RuntimeException("Wrong order");
                       }
               }
        }

        @Override
        public void adminMain() {
                System.out.println("Zalogowano jako Admin \n Wybierz działanie:");
                System.out.println("[0] Zakończ program");
                System.out.println("[1] Zarządzaj bazą");
                System.out.println("[2] Zarządzaj szkołą");
                System.out.println("[3] Zmien haslo");

                int order = scanner.nextInt();

                switch (order) {
                        case 1:
                                manageDatabase();
                                break;
                        case 2:
                                manageSchool();
                                break;
                        case 3:
                                changeAdminPassword();
                                break;
                        case 0:
                                System.out.println("goodbye");
                                System.exit(0);
                        default:
                                throw new RuntimeException("Wrong order");
                }
        }

        @Override
        public void teacherMain() {
                System.out.println("Zalogowano jako nauczyciel o ID" + user.getId() + "\n" +
                        "Wybierz działanie:");

                System.out.println("[0] Zakończ program");
                System.out.println("[1] Dodaj ocene");
                System.out.println("[2] Dodaj uwage");
                System.out.println("[3] Dodaj nieobecnosc");
                System.out.println("[4] Zmien haslo");
                int order = scanner.nextInt();

                switch (order) {
                        case 1:
                                addStudentGrade();
                                break;
                        case 2:
                                addStudentNote();
                                break;
                        case 3:
                                addStudentAbsence();
                                break;
                        case 4:
                                changeTeacherPassword();
                                break;
                        case 0:
                                System.out.println("goodbye");
                                System.exit(0);

                        default:
                                throw new RuntimeException("Wrong order");
                }

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
        public void addUserStudent() {
                System.out.println("Podaj login: ");
                String login = scanner.next();
                System.out.println("Podaj hasło: ");
                String password = scanner.next();
                System.out.println("Wybierz ucznia");
                ArrayList<Pair<String, String>> students = dbManager.getStudentsWithoutUser();
                for (int i = 0; i < students.size(); i++) {
                        System.out.println("[" + i + "] " + students.get(i).getY());
                }
                int order = scanner.nextInt();
                String studentId = students.get(order).getX();
                dbManager.addStudentUser(login, password, studentId);
                System.out.println("Dodano użytkownika:");
                System.out.println("login: " + login);
                System.out.println("haslo: " + password);
                System.out.println("Dla ucznia: " + students.get(order).getY());
        }

        @Override
        public void addUserTeacher() {
                System.out.println("Podaj login: ");
                String login = scanner.next();
                System.out.println("Podaj hasło: ");
                String password = scanner.next();
                System.out.println("Wybierz nauczyciela");
                ArrayList<Pair<Integer, String>> students = dbManager.getTeachersWithoutUser();
                for (int i = 0; i < students.size(); i++) {
                        System.out.println("[" + i + "] " + students.get(i).getY());
                }
                int order = scanner.nextInt();
                Integer teacherId = students.get(order).getX();
                dbManager.addStudentUser(login, password, teacherId.toString());
                System.out.println("Dodano użytkownika:");
                System.out.println("login: " + login);
                System.out.println("haslo: " + password);
                System.out.println("Dla nauczyciela: " + students.get(order).getY());
        }

        @Override
        public void changeAdminPassword() { //NOT WORKING YET
                String oldPassword;
                do {
                        System.out.print("Podaj stare hasło: ");
                        oldPassword = scanner.next();
                } while (oldPassword.equals(ADMIN_PASSWORD));

                System.out.print("Podaj nowe hasło: ");
                String newPassword = scanner.next();
                try {
                        Class Constants = Class.forName("utils.Constants");
                        try {
                                Field AdminPassword = Constants.getDeclaredField(ADMIN_PASSWORD);
                                AdminPassword.set(this, newPassword);
                                System.out.println(ADMIN_PASSWORD);
                        } catch (NoSuchFieldException e) {
                                e.printStackTrace();
                        } catch (IllegalAccessException e) {
                                e.printStackTrace();
                        }
                } catch (ClassNotFoundException e) {
                        e.printStackTrace();
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
        public void changeStudentPassword() {
                while (true) {
                        System.out.println("Podaj stare haslo: ");
                        String oldPassword = scanner.next();
                        System.out.println("login: " + user.getId());
                        System.out.println("haslo: " + oldPassword);

                        if (user.getPassword().equals(oldPassword)) {
                                break;
                        } else {
                                System.out.println("Podane hasło jest niepoprawne, spróbuj ponownie");
                        }
                }
                String newPassword, newPassword1;
                do {
                        System.out.println("Podaj nowe hasło: ");
                        newPassword = scanner.next();

                        System.out.println("Zatwierdz nowe hasło: ");
                        newPassword1 = scanner.next();
                } while (!newPassword.equals(newPassword1) && newPassword != null);

                dbManager.changeStudentPassword(user.getId(), newPassword);
                user.setPassword(newPassword);
                System.out.println("Hasło zostało zmienione");
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

        @Override
        public void changeTeacherPassword() {

        }


}

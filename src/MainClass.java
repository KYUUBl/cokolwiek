import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import utils.AccountType;
import utils.Pair;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;

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

                switch (user.getAccountType()) {
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
                //editXML();
                String login;
                String password;
                while (true) {
                        System.out.print("login: ");
                        login = scanner.nextLine();

                        System.out.print("hasło: ");
                        password = scanner.nextLine();

                        if (dbManager.signIn(login, password) != null) {
                                return dbManager.signIn(login, password);
                        }

                        if (login.equals(ADMIN_LOGIN) && password.equals(ADMIN_PASSWORD)) {
                                return new User(ADMIN_ID, AccountType.ADMIN, password);
                        }
                        System.out.println("Login lub hasło niepoprawne, proszę spróbować ponownie");
                }
        }

        public void logout(AccountType accountType) { //TODO To zdecydowanie nie dziala
                System.out.println("Czy na pewno chcesz się wylogować?");
                System.out.println("[1] TAK");
                System.out.println("[0] NIE");
                int order = scanner.nextInt();
                switch (order) {
                        case 0:
                                switch (accountType) {
                                        case ADMIN:
                                                adminMain();
                                                break;
                                        case STUDENT:
                                                studentMain();
                                                break;
                                        case TEACHER:
                                                teacherMain();
                                                break;
                                        default:
                                                throw new RuntimeException("Wrong Order");
                                }
                                break;
                        case 1:
                                user.setId(null);
                                user.setPassword(null);
                                user.setAccountType(null);
                                initApplication();
                }
        }

        public boolean checkDate(String date) {
                if (!Pattern.matches("\\d{2}[.]\\d{2}[.]\\d{4}", date)) return false;
                int dd = Integer.parseInt(date.substring(0, 2));
                int mm = Integer.parseInt(date.substring(3, 5));
                int rrrr = Integer.parseInt(date.substring(6, 10));
                try {
                        LocalDate.of(rrrr, mm, dd);
                } catch (DateTimeException e) {
                        return false;
                }
                return true;
        }

        public boolean checkOrderValidity(int from, int to, int order) {
                if (order >= from && order <= to) {
                        return true;
                }
                return false;
        }

        @Override
        public void editXML() { //TODO Implement this shit

                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = null;
                try {
                        documentBuilder = documentBuilderFactory.newDocumentBuilder();
                } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                }
                try {
                        InputStream fis = getClass().getResourceAsStream("adminPassword.xml");
                        Document document = documentBuilder.parse(fis);
                        document.setNodeValue("ala");


                        Node password = document.getFirstChild();
                        System.out.println("Stare: " + password.getNodeValue());
                        password.setNodeValue("ala");

                        System.out.println("Nowe:" + password.getNodeValue());
                } catch (SAXException e) {
                        e.printStackTrace();
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }


// ------------------------------------STUDENT VIEW METHODS ------------------------------------ \\

        @Override
        public void studentMain() {
                System.out.println("Zalogowano jako uczeń o numerze PESEL:" + user.getId());
                while (true) {
                        System.out.println("Wybierz działanie:");

                        System.out.println("[-1] Wyloguj");
                        System.out.println("[0] Zakończ program");
                        System.out.println("[1] Wyświetl oceny");
                        System.out.println("[2] Wyświetl nieobecności");
                        System.out.println("[3] Wyświetl uwagi");
                        System.out.println("[4] Wyświetl plan lekcji");
                        System.out.println("[5] Zmień hasło");
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
                                        getStudentSchedule();
                                        break;
                                case 5:
                                        changeStudentPassword();
                                        break;
                                case 0:
                                        System.out.println("goodbye");
                                        System.exit(0);
                                        break;
                                case -1:
                                        //logout(user.getAccountType());
                                        break;
                                default:
                                        System.out.println("Niepoprawne polecenie,\nWybierz wartość z zakresu");
                                        break;
                        }
                }
        }

        @Override
        public void getStudentGrades() {
                ArrayList<Pair<Integer, String>> subjects = new ArrayList<Pair<Integer, String>>();
                subjects = dbManager.getStudentSubjects(user.getId());
                boolean isOrderCorrect = false;
                int order = -1;
                while (!isOrderCorrect) {
                        for (int i = 0; i < subjects.size(); i++) {
                                System.out.println("[" + i + "] " + subjects.get(i).getY());
                        }
                        System.out.println("\n WYBIERZ PRZEDMIOT");
                        order = scanner.nextInt();
                        isOrderCorrect = checkOrderValidity(0, subjects.size() - 1, order);
                        if (!isOrderCorrect) {
                                System.out.println("Niepoprawny wybór");
                        }
                }
                assert (order != -1); //DODAM SOBIE ASSERTA
                int subject = subjects.get(order).getX();

                ArrayList<String> grades = new ArrayList<String>();
                grades = dbManager.getStudentGrades(user.getId(), subject);
                for (int i = 0; i < grades.size(); i++) {
                        System.out.println(grades.get(i));
                }
                studentMain();
        }

        @Override
        public void getStudentSchedule() {
                ArrayList<ArrayList<String>> shedule = dbManager.getLessonShedule(user.getId());
                System.out.printf("%-4s %-20s %-20s %-20s %-20s %s", " ", "PONIEDZIALEK", "WTOREK", "SRODA", "CZWARTEK", "PIATEK\n");
                for (int j = 0; j < 10; j++) {
                        System.out.printf("%-4d %-20s %-20s %-20s %-20s %s", j, shedule.get(0).get(j), shedule.get(1).get(j), shedule.get(2).get(j), shedule.get(3).get(j), shedule.get(4).get(j) + "\n");
                }
                System.out.println("Naciśnij enter, by kontynować");
                studentMain();
        }

        @Override //TODO DOPISAC SPRAWDZANIE POPRAWNOSCI
        public void getStudentAbsences() {
                System.out.println("Podaj zakres, z jakiego chcesz otrzymać nieobecności");
                System.out.println("od: DD.MM.RRRR");
                String dateFrom = scanner.next();
                System.out.println("do: DD.MM.RRRR");
                String dateTo = scanner.next();
                ArrayList<String> absences = dbManager.getStudentAbsences(user.getId(), dateFrom, dateTo);

                for (String s : absences) {
                        System.out.println(s);
                }
                studentMain();
        }

        @Override //TODO DOPISAC SPRAWDZANIE POPRAWNOSCI
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
                studentMain();
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
                studentMain();
        }

// ------------------------------------ END ------------------------------------ \\
// ------------------------------------ ADMIN VIEW METHODS ------------------------------------ \\

        @Override
        public void adminMain() {
                while (true) {
                        System.out.println("Zalogowano jako Admin \n Wybierz działanie:");
                        System.out.println("[-1] Wyloguj");
                        System.out.println("[0] Zakończ program");
                        System.out.println("[1] Zarządzaj bazą");
                        System.out.println("[2] Zarządzaj szkołą");
                        System.out.println("[3] Zmien haslo");

                        int order = scanner.nextInt();

                        switch (order) {
                                case -1:
                                        //logout(user.getAccountType());
                                        break;
                                case 0:
                                        System.out.println("goodbye");
                                        System.exit(0);
                                        break;
                                case 1:
                                        manageDatabase();
                                        break;
                                case 2:
                                        manageSchool();
                                        break;
                                case 3:
                                        changeAdminPassword();
                                        break;

                                default:
                                        System.out.println("Niepoprawne polecenie,\n Wybierz wartość z zakresu");
                                        break;
                        }
                }
        }

        @Override
        public void manageDatabase() {
                while (true) {
                        System.out.println("Dodaj nowego użytkownika:");
                        System.out.println("[1] Dodaj ucznia");
                        System.out.println("[2] Dodaj nauczyciela");

                        int order = scanner.nextInt();

                        switch (order) {
                                case 1:
                                        addUserStudent();
                                        break;
                                case 2:
                                        addUserTeacher();
                                        break;
                                default:
                                        System.out.println("Niepoprawne polecenie,\n Wybierz wartość z zakresu");
                                        break;
                        }
                }
        }

        @Override
        public void manageSchool() {
                while (true) {
                        System.out.println("Wybierz działanie:");
                        System.out.println("[0] Wycofaj do głównego menu");
                        System.out.println("[1] Dodaj ucznia");
                        System.out.println("[2] Deaktywuj ucznia");
                        System.out.println("[3] Dodaj nauczyciela");
                        System.out.println("[4] Dodaj przedmiot");
                        System.out.println("[5] Zakończ rok szkolny");

                        int order = scanner.nextInt();

                        switch (order) {
                                case 0:
                                        adminMain();
                                        break;
                                case 1:
                                        addStudent();
                                        break;
                                case 2:
                                        deactivateStudent();
                                        break;
                                case 3:
                                        addTeacher();
                                        break;
                                case 4:
                                        addSubject();
                                        break;
                                case 5:
                                        endSchoolYear();
                                        break;
                                default:
                                        System.out.println("Niepoprawne polecenie,\n Wybierz wartość z zakresu");
                                        break;

                        }
                }
        }

        @Override
        public void addUserStudent() {
                System.out.println("Podaj login: ");
                String login = scanner.next();
                System.out.println("Podaj hasło: ");
                String password = scanner.next();
                System.out.println("Wybierz ucznia");

                boolean isOrderCorrect = false;
                int order = -1;
                ArrayList<Pair<String, String>> students = dbManager.getStudentsWithoutUser();
                while (!isOrderCorrect) {
                        for (int i = 0; i < students.size(); i++) {
                                System.out.println("[" + i + "] " + students.get(i).getY());
                        }
                        order = scanner.nextInt();
                        isOrderCorrect = checkOrderValidity(order, 0, students.size() - 1);
                        if (!isOrderCorrect) {
                                System.out.println("Niepoprawny wybór");
                        }
                }
                String studentId = students.get(order).getX();
                dbManager.addStudentUser(login, password, studentId);
                System.out.println("Dodano użytkownika:");
                System.out.println("login: " + login);
                System.out.println("haslo: " + password);
                System.out.println("Dla ucznia: " + students.get(order).getY());
                manageDatabase();
        }

        @Override
        public void addUserTeacher() {
                System.out.println("Podaj login: ");
                String login = scanner.next();
                System.out.println("Podaj hasło: ");
                String password = scanner.next();
                System.out.println("Wybierz nauczyciela");

                boolean isOrderCorrect = false;
                int order = -1;
                ArrayList<Pair<Integer, String>> teachers = dbManager.getTeachersWithoutUser();
                while (!isOrderCorrect) {
                        for (int i = 0; i < teachers.size(); i++) {
                                System.out.println("[" + i + "] " + teachers.get(i).getY());
                        }
                        order = scanner.nextInt();
                        isOrderCorrect = checkOrderValidity(order, 0, teachers.size() - 1);
                        if (!isOrderCorrect) {
                                System.out.println("Niepoprawny wybór");
                        }
                }
                Integer teacherId = teachers.get(order).getX();
                dbManager.addTeacherUser(login, password, teacherId);
                System.out.println("Dodano użytkownika:");
                System.out.println("login: " + login);
                System.out.println("haslo: " + password);
                System.out.println("Dla nauczyciela: " + teachers.get(order).getY());
                manageDatabase();
        }

        @Override
        public void addStudent() { //TODO finish this
                System.out.print("Podaj imie: ");
                String name = scanner.next();
                System.out.print("Podaj nazwisko: ");
                String lastname = scanner.next();
                System.out.print("Podaj PESEL");
                String pesel = scanner.next();
                System.out.print("Podaj numer telefonu rodzica: ");
                int phoneNumber = scanner.nextInt();

                ArrayList<Pair<Integer, String>> classes = dbManager.getAllClasses();
                System.out.println("Wybierz klasę, do której chcesz dodać ucznia: ");
                boolean isOrderCorrect = false;
                int order = -1;
                while (!isOrderCorrect) {
                        for (int i = 0; i < classes.size(); i++) {
                                System.out.println("[" + i + "] " + classes.get(i).getY());
                        }
                        order = scanner.nextInt();
                        isOrderCorrect = checkOrderValidity(order, 0, classes.size() - 1);
                        if (!isOrderCorrect) {
                                System.out.println("Niepoprawny wybór");
                        }
                }
                int classID = classes.get(order).getX();
                dbManager.addStudent(name, lastname, pesel, phoneNumber, classID);
                System.out.println("Pomyslnie dodano ucznia");
                manageSchool();

        }

        @Override
        public void deactivateStudent() {
                System.out.println("Wybierz ucznia, którech chcesz deaktywować");
                ArrayList<Pair<String, String>> students = dbManager.getAllStudents();
                boolean isOrderCorrect = false;
                int order = -1;
                while (!isOrderCorrect) {
                        for (int i = 0; i < students.size(); i++) {
                                System.out.println("[" + i + "] " + students.get(i).getY());
                        }
                        order = scanner.nextInt();
                        isOrderCorrect = checkOrderValidity(order, 0, students.size() - 1);
                        if (!isOrderCorrect) {
                                System.out.println("Niepoprawny wybór");
                        }
                }
                String userID = students.get(order).getX();
                dbManager.deactivateStudent(userID);
                System.out.println("Pomyślnie deaktywowano studenta");
                manageSchool();
        }

        @Override
        public void addTeacher() {
                System.out.print("Podaj imie: ");
                String name = scanner.next();
                System.out.print("Podaj nazwisko: ");
                String lastname = scanner.next();
                dbManager.addTeacher(name, lastname);
                System.out.println("Dodano nauczyciela. \n");
                manageSchool();
        }

        @Override
        public void endSchoolYear() {
                int order = 0;
                System.out.println("Czy na pewno chcesz zakonczyc rok szkolny?");
                System.out.println("[0] NIE");
                System.out.println("[1] TAK");
                order = scanner.nextInt();
                if (order == 1) {
                        System.out.println("Zakonczono rok szkolny");
                        dbManager.yearEnd();
                } else {
                        System.out.println("Powracam do zarządzania szkołą");
                        manageSchool();
                }
                manageSchool();
        }

        @Override
        public void changeAdminPassword() {
                String oldPassword;
                do {
                        System.out.print("Podaj stare hasło: ");
                        oldPassword = scanner.next();
                } while (!oldPassword.equals(ADMIN_PASSWORD));

                System.out.print("Podaj nowe hasło: ");
                String newPassword = scanner.next();
                try {
                        Class Constants = Class.forName("utils.Constants");
                        try {
                                Field AdminPassword = Constants.getDeclaredField("ADMIN_PASSWORD"); //NoSuchFieldException
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

        @Override //TODO NOT YET IMPLEMENTED
        public void addSubject() {
                System.out.println("Podaj nazwę nowego przedmiotu");
                String name = scanner.next();

                System.out.println("Wybierz klasę:");
                ArrayList<Pair<Integer, String>> classes = dbManager.getAllClasses();

                boolean isOrderCorrect = false;
                int order = -1;
                while (!isOrderCorrect) {
                        System.out.println("Wybierz klasę, do której chcesz dodać ucznia: ");
                        for (int i = 0; i < classes.size(); i++) {
                                System.out.println("[" + i + "] " + classes.get(i).getY());
                        }
                        order = scanner.nextInt();
                        isOrderCorrect = checkOrderValidity(order, 0, classes.size() - 1);
                        if (!isOrderCorrect) {
                                System.out.println("Niepoprawny wybór");
                        }
                }
                int classID = classes.get(order).getX();

                System.out.println("Wybierz nauczyciela przypisanego do przedmiotu");

                isOrderCorrect = false;
                order = -1;
                ArrayList<Pair<Integer, String>> teachers = dbManager.getAllTeachers();
                while (!isOrderCorrect) {
                        for (int i = 0; i < teachers.size(); i++) {
                                System.out.println("[" + i + "] " + teachers.get(i).getY());
                        }
                        order = scanner.nextInt();
                        isOrderCorrect = checkOrderValidity(order, 0, classes.size() - 1);
                        if (!isOrderCorrect) {
                                System.out.println("Niepoprawny wybór");
                        }
                }
                int teacherID = classes.get(order).getX();

                dbManager.addSubject(name, classID, teacherID);
                System.out.println("Pomyślnie dodano przedmiot");
                manageSchool();
        }


// ------------------------------------ END ------------------------------------ \\
// ------------------------------------ TEACHER VIEW METHODS ------------------------------------ \\

        @Override
        public void teacherMain() {
                while (true) {
                        System.out.println("Zalogowano jako nauczyciel o ID" + user.getId() + "\n" +
                                "Wybierz działanie:");
                        System.out.println("[-1] Wyloguj");
                        System.out.println("[0] Zakończ program");
                        System.out.println("[1] Dodaj ocene");
                        System.out.println("[2] Dodaj uwage");
                        System.out.println("[3] Dodaj lekcje z nieobecnosciami");
                        System.out.println("[4] Wyświetl swoje przemioty");
                        int order = scanner.nextInt();

                        switch (order) {
                                case 1:
                                        addStudentGrade();
                                        break;
                                case 2:
                                        addStudentNote();
                                        break;
                                case 3:
                                        addCompletedLesson();
                                        break;
                                case 4:
                                        //getTeacherSubject();
                                        break;
                                case 0:
                                        System.out.println("goodbye");
                                        System.exit(0);
                                        break;
                                case -1:
                                        //logout(user.getAccountType());
                                        break;

                                default:
                                        System.out.println("Niepoprawne polecenie,\n Wybierz wartość z zakresu");
                                        break;
                        }
                }
        }

        @Override //TODO NOT YET IMPLEMENTED
        public void addStudentGrade() {
                System.out.println("Wybierz przedmiot, z którego chcesz dodać ocenę");
                ArrayList<Pair<Integer, String>> subjects = dbManager.getTeacherSubjects(Integer.parseInt(user.getId()));
                boolean isOrderCorrect = false;
                int order = -1;
                while (!isOrderCorrect) {
                        for (int i = 0; i < subjects.size(); i++) {
                                System.out.println("[" + i + "] " + subjects.get(i).getY());
                        }
                        order = scanner.nextInt();
                        isOrderCorrect = checkOrderValidity(order, 0, subjects.size() - 1);
                        if (!isOrderCorrect) {
                                System.out.println("Niepoprawny wybór");
                        }
                }
                int subjectId = subjects.get(order).getX();
//TODO Tutaj trzeba dokończyć
                ArrayList<Pair<String, String>> studentsBySubject = dbManager.getSubjectStudents(subjectId);
                for (int i = 0; i < studentsBySubject.size(); i++) {
                        System.out.println("[" + i + "] " + studentsBySubject.get(i).getY());
                }
                //int
        }

        //--------------------------------------------------------------------------------------------------------------------=======================TUTAJ SKONCZYLEM NAPRAWIAC!!!!
        @Override //TODO NOT TESTED
        public void addStudentNote() {

                System.out.println("Wybierz ucznia, któremu chcesz wystawić uwagę");
                ArrayList<Pair<String, String>> students = dbManager.getAllStudents();
                for (int i = 0; i < students.size(); i++) {
                        System.out.println("[" + i + "] " + students.get(i).getY());
                }
                int order = scanner.nextInt();
                String studentID = students.get(order).getX();

                System.out.println("Podaj treść uwagi i zatwierdź ENTEREM:");
                String note = scanner.nextLine(); //TODO CZY NA PEWNO NEXTLINE?

                System.out.println("Czy uwaga jest pozytywna? ");
                System.out.println("[1] TAK");
                System.out.println("[0] NIE");
                int orderForPositive = scanner.nextInt(); //TUTAJ UWAGA Z BOOLEANEM
                boolean isPositive = orderForPositive == 1 ? true : false;
                System.out.println("Podaj datę wystawienia");
                String date = scanner.next();

                dbManager.addStudentNote(studentID, Integer.parseInt(user.getId()), note, isPositive, date);
                System.out.println("Dodano uwagę");
                teacherMain();
        }

        @Override
        public void addCompletedLesson() {
                System.out.println("Podaj date przeprowadzonej lekcji: DD.MM.RRRR");
                String data = scanner.next();
                System.out.println("Wybierz lekcje z podzialu godzin ktora przeprowadziles");
                ArrayList<Pair<Integer, String>> lessons = dbManager.getLessonsByDate(data);
                for (int i = 0; i < lessons.size(); i++) {
                        System.out.println("[" + i + "] " + lessons.get(i).getY());
                }
                int order = scanner.nextInt();
                int lessonID = lessons.get(order).getX();
                System.out.println("Podaj temat lekcji");
                String topic = scanner.next();
                int lID = dbManager.addCompletedLesson(data, Integer.parseInt(user.getId()), lessonID, topic);
                ArrayList<Pair<String, String>> students = dbManager.getStudentsByLesson(lID);
                System.out.println("Podaj nieobecnych uczniow:([-1] zakoncz podawanie nieobecnych)");
                for (int i = 0; i < students.size(); i++) {
                        System.out.println("[" + i + "] " + students.get(i).getY());
                }
                order = scanner.nextInt();
                while (order >= 0) {
                        dbManager.addStudentAbsence(students.get(order).getX(), lID);
                        order = scanner.nextInt();
                }
                teacherMain();
        }

        @Override //TODO NOT YET IMPLEMENTED
        public void getTeacherSubject() {

        }

        @Override //TODO NOT YET IMPLEMENTED
        public void getSubjectStudents() {
        }

        @Override
        public void changeTeacherPassword() { //TODO NOT TESTED
                while (true) {
                        System.out.println("Podaj stare hasło: ");
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

                dbManager.changeTeacherPassword(Integer.parseInt(user.getId()), newPassword);
                user.setPassword(newPassword);
                System.out.println("Hasło zostało zmienione");
                teacherMain();
        }
}

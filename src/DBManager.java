

import utils.AccountType;
import utils.Pair;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class DBManager {
        Connection c;
        Statement stmt;

        public DBManager() {
                try {
                        Class.forName("org.postgresql.Driver");
                        c = DriverManager
                                .getConnection("jdbc:postgresql://localhost:5432/School register",
                                        "postgres", "kamil");
                        System.out.println("Opened database successfully");
                } catch (Exception e) {
                }
        }

        public void openConnection() {
                try {

                        stmt = c.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT * FROM UCZNIOWIE;");
                        while (rs.next()) {
                                String name = rs.getString("imie");
                                String lastname = rs.getString("nazwisko");
                                System.out.println("NAME = " + name);
                                System.out.println("LASTNAME = " + lastname);
                                System.out.println();
                        }
                        rs.close();
                        stmt.close();
                        //c.close();
                } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println(e.getClass().getName() + ": " + e.getMessage());
                        System.exit(0);
                }
        }

        public ArrayList<String> getStudentGrades(String studentID, int subjectID) {
                ArrayList<String> grades = new ArrayList<String>();
                try {
                        stmt = c.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT wartosc,tematyka,nazwa from oceny_uczniow join rodzaje_aktywnosci ra on id_aktywnosci=ra.id where id_ucznia='" + studentID + "' and id_przedmiotu=" + subjectID + ";");
                        while (rs.next()) {
                                int value = rs.getInt("wartosc");
                                String topic = rs.getString("tematyka");
                                String name = rs.getString("nazwa");
                                System.out.println(topic + " " + name + " " + value);
                                grades.add(topic + " " + name + " " + value);
                        }
                        System.out.println("success");
                        rs.close();
                        stmt.close();
                } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println(e.getClass().getName() + ": " + e.getMessage());
                }
                return grades;
        }

        public ArrayList<String> getStudentAbsences(String studentID, String fromData, String toData) {
                ArrayList<String> absences = new ArrayList<String>();
                try {
                        stmt = c.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT pl.data,p.nr_lekcji from nieobecnosci n join przeprowadzone_lekcje pl on n.id_lekcji=pl.id join plan_lekcji p on pl.id_lekcji=p.id where id_ucznia='" + studentID + "' and data>=to_date('" + fromData + "', 'DD.MM.YYYY') and data<=to_date('" + toData + "', 'DD.MM.YYYY');");
                        while (rs.next()) {
                                String date = rs.getDate("data").toString();
                                int lesson = rs.getInt("nr_lekcji");
                                System.out.println(date + " lekcja nr: " + lesson);
                                absences.add(date + " lekcja nr: " + lesson);
                        }
                        System.out.println("success");
                        rs.close();
                        stmt.close();
                } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println(e.getClass().getName() + ": " + e.getMessage());
                }
                return absences;
        }

        public ArrayList<String> getStudentNotes(String studentID, String fromData, String toData) {
                ArrayList<String> notes = new ArrayList<String>();
                try {
                        stmt = c.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT opis,data_wystawienia,czy_pozytywna from uwagi where id_ucznia='" + studentID + "' and data_wystawienia>=to_date('" + fromData + "', 'DD.MM.YYYY')" + " and data_wystawienia<=to_date('" + toData + "', 'DD.MM.YYYY');");
                        while (rs.next()) {
                                String description = rs.getString("opis");
                                String date = rs.getDate("data_wystawienia").toString();
                                boolean positive = rs.getBoolean("czy_pozytywna");
                                System.out.println(date + " " + positive + ": " + description);
                                notes.add(date + " " + positive + ": " + description);
                        }
                        System.out.println("success");
                        rs.close();
                        stmt.close();
                } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println(e.getClass().getName() + ": " + e.getMessage());
                }
                return notes;
        }

        public ArrayList<String> getStudentSubjects(String studentID) {
                ArrayList<String> subjects = new ArrayList<String>();
                try {
                        stmt = c.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT nazwa from przedmioty p join klasy k on p.id_klasy=k.id join uczniowie u on u.id_klasy=k.id where pesel='" + studentID + "';");
                        while (rs.next()) {
                                String name = rs.getString("nazwa");
                                System.out.println(name);
                                subjects.add(name);
                        }
                        System.out.println("success");
                        rs.close();
                        stmt.close();
                } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println(e.getClass().getName() + ": " + e.getMessage());
                }
                return subjects;
        }

        public ArrayList<String> getTeacherSubjects(int teacherID) {
                ArrayList<String> subjects = new ArrayList<String>();
                try {
                        stmt = c.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT nazwa,oddzial,rok_rozpoczecia from przedmioty p join klasy k on p.id_klasy=k.id where aktywny=true and id_prowadzacego='" + teacherID + "';");
                        while (rs.next()) {
                                String name = rs.getString("nazwa");
                                String section = rs.getString("oddzial");
                                int startYear = rs.getInt("rok_rozpoczecia");
                                System.out.println(name + " klasa " + startYear + section);
                                subjects.add(name + " klasa " + startYear + section);
                        }
                        System.out.println("success");
                        rs.close();
                        stmt.close();
                } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println(e.getClass().getName() + ": " + e.getMessage());
                }
                return subjects;
        }

        public ArrayList<String> getSubjectStudents(int subjectID) {
                ArrayList<String> students = new ArrayList<String>();
                try {
                        stmt = c.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT imie,nazwisko,pesel from przedmioty p join klasy k on p.id_klasy=k.id join uczniowie u on u.id_klasy=k.id where p.id='" + subjectID + "';");
                        while (rs.next()) {
                                String name = rs.getString("imie");
                                String lastname = rs.getString("nazwisko");
                                String pesel = rs.getString("pesel");
                                System.out.println(name + " " + lastname + " " + pesel);
                                students.add(name + " " + lastname + " " + pesel);
                        }
                        System.out.println("success");
                        rs.close();
                        stmt.close();
                } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println(e.getClass().getName() + ": " + e.getMessage());
                }
                return students;
        }

        public void addStudentGrade(int subjectID, String studentID, int gradeValue, int activityID, String topic) {
                try {
                        stmt = c.createStatement();
                        stmt.executeUpdate("INSERT INTO oceny_uczniow(id_przedmiotu,id_ucznia,wartosc,id_aktywnosci,tematyka) values(" + subjectID + "," + studentID + "," + gradeValue + "," + activityID + ",'" + topic + "');");
                        System.out.println("success");
                        stmt.close();
                } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println(e.getClass().getName() + ": " + e.getMessage());
                }
        }

        public void addStudentNote(String studentID, int teacherID, String note, boolean isPositive, String data) {
                try {
                        stmt = c.createStatement();
                        stmt.executeUpdate("INSERT INTO uwagi(id_ucznia,id_nauczyciela,opis,czy_pozytywna,data_wystawienia) values('" + studentID + "'," + teacherID + ",'" + note + "'," + isPositive + ",to_date('" + data + "', 'DD.MM.YYYY'));");
                        System.out.println("success");
                        stmt.close();
                } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println(e.getClass().getName() + ": " + e.getMessage());
                }
        }

        public void addStudentAbsence(String studentID, int lessonID) {
                try {
                        stmt = c.createStatement();
                        stmt.executeUpdate("INSERT INTO nieobecnosci(id_ucznia,id_lekcji) values('" + studentID + "'," + lessonID + ");");
                        System.out.println("success");
                        stmt.close();
                } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println(e.getClass().getName() + ": " + e.getMessage());
                }
        }

        public void addCompletedLesson(String data, int teacherID, int lessonID, String topic) {
                try {
                        stmt = c.createStatement();
                        stmt.executeUpdate("INSERT INTO przeprowadzone_lekcje(data,id_prowadzacego,id_lekcji,temat_zajec) values(to_date('" + data + "', 'DD.MM.YYYY')," + teacherID + "," + lessonID + ",'" + topic + "');");
                        System.out.println("success");
                        stmt.close();
                } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println(e.getClass().getName() + ": " + e.getMessage());
                }
        }

        public void addStudent(String name, String lastname, String pesel, int phoneNumber, int classID) {
                try {
                        stmt = c.createStatement();
                        stmt.executeUpdate("INSERT INTO uczniowie(imie,nazwisko,pesel,telefon_do_rodzica,id_klasy) values('" + name + "','" + lastname + "','" + pesel + "'," + phoneNumber + "," + classID + ");");
                        System.out.println("success");
                        stmt.close();
                } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println(e.getClass().getName() + ": " + e.getMessage());
                }
        }

        public void addclass(String section, int startYear, int tutorID) {
                try {
                        stmt = c.createStatement();
                        stmt.executeUpdate("INSERT INTO klasy(oddzial,rok_rozpoczecia,id_wychowawcy) values('" + section + "'," + startYear + "," + tutorID + ");");
                        System.out.println("success");
                        stmt.close();
                } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println(e.getClass().getName() + ": " + e.getMessage());
                }
        }

        public void addTeacher(String name, String lastname) {
                try {
                        stmt = c.createStatement();
                        stmt.executeUpdate("INSERT INTO nauczyciele(imie,nazwisko) values('" + name + "','" + lastname + "');");
                        System.out.println("success");
                        stmt.close();
                } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println(e.getClass().getName() + ": " + e.getMessage());
                }
        }

        public void addSubject(String name, int classID, int teacherID) {
                try {
                        stmt = c.createStatement();
                        stmt.executeUpdate("INSERT INTO przedmioty(nazwa,id_klasy,id_prowadzacego) values('" + name + "'," + classID + "," + teacherID + ");");
                        System.out.println("success");
                        stmt.close();
                } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println(e.getClass().getName() + ": " + e.getMessage());
                }
        }

        public void addScheduleLesson(int subjectID, int lessonNumber, int weekday) {
                try {
                        stmt = c.createStatement();
                        stmt.executeUpdate("INSERT INTO plan_lekcji(id_przedmiotu,nr_lekcji,dzien_tygodnia) values(" + subjectID + "," + lessonNumber + "," + weekday + ");");
                        System.out.println("success");
                        stmt.close();
                } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println(e.getClass().getName() + ": " + e.getMessage());
                }
        }

        public void yearEnd() {
                try {
                        stmt = c.createStatement();
                        stmt.executeUpdate("UPDATE przedmioty SET aktywny = FALSE;");
                        stmt.close();
                } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println(e.getClass().getName() + ": " + e.getMessage());
                }
        }

        public void deactivateStudent(String studentID) {
                try {
                        stmt = c.createStatement();
                        stmt.executeUpdate("UPDATE uczniowie SET aktywny = false where pesel='" + studentID + "';");
                        stmt.close();
                } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println(e.getClass().getName() + ": " + e.getMessage());
                }
        }

        public void addStudentUser(String login, String password, String pesel) {
                try {
                        stmt = c.createStatement();
                        stmt.executeUpdate("INSERT INTO uzytkownicy(login,haslo) values('" + login + "','" + password + "');");
                        stmt.executeUpdate("UPDATE uczniowie SET id_uzytkownika = '" + login + "' where pesel='" + pesel + "';");
                        stmt.close();
                } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println(e.getClass().getName() + ": " + e.getMessage());
                }
        }

        public void addTeacherUser(String login, String password, int teacherID) {
                try {
                        stmt = c.createStatement();
                        stmt.executeUpdate("INSERT INTO uzytkownicy(login,haslo) values('" + login + "','" + password + "');");
                        stmt.executeUpdate("UPDATE nauczyciele SET id_uzytkownika = '" + login + "' where id=" + teacherID + ";");
                        stmt.close();
                } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println(e.getClass().getName() + ": " + e.getMessage());
                }
        }

        public Pair<String, AccountType> signIn(String login, String password) {
                //w parze pierwszy argument to string drugi boolean
                //i true jesli to uczen, false jesli to nauczyciel
                //null jesli login nie istnieje lub zle haslo
                try {
                        stmt = c.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT * from uzytkownicy join uczniowie on login=id_uzytkownika where login='" + login + "';");
                        while (rs.next()) {
                                String pesel = rs.getString("pesel");
                                if (!password.equals(rs.getString("haslo"))) return null;
                                Pair<String, AccountType> pair = new Pair<String,AccountType>(pesel, AccountType.STUDENT);

                                return pair;
                        }
                        rs = stmt.executeQuery("SELECT * from uzytkownicy join nauczyciele on login=id_uzytkownika where login='" + login + "';");
                        while (rs.next()) {
                                int id = rs.getInt("id");
                                if (!password.equals(rs.getString("haslo"))) return null;
                                Pair<String, AccountType> pair = new Pair<String,AccountType>(Integer.toString(id), AccountType.TEACHER);
                                return pair;
                        }
                        return null;
                } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println(e.getClass().getName() + ": " + e.getMessage());
                }
                return null;
        }


        public static void main(String args[]) {
                DBManager dbManager = new DBManager();
                //te funkcje sa przetestowane wiec powinny dzialac
                //dbManager.openConnection();
                //dbManager.getStudentGrades("96091227824",4);
                //dbManager.getStudentAbsences("96091227824","01.01.2014","12.12.2015");
                //dbManager.getStudentNotes("96091227824","01.01.2014","12.12.2015");
                //dbManager.getStudentSubjects("96091227824");
                //dbManager.getSubjectStudents(4);
                //dbManager.getTeacherSubjects(7);
                //dbManager.addTeacher("kamil","pietruszka");
                //dbManager.addStudent("kyuub","kurama","94112607253",111111111,6);
                //dbManager.addStudentUser("kyuu","kyuu","94112607253");
                //dbManager.addTeacherUser("pietruchacz","kamil",15)
                //dbManager.deactivateStudent("95010881893");
                //dbManager.addclass("d",2014,15);
                //dbManager.addSubject("probabili",6,22);
                //dbManager.addStudentGrade(18,"94112607253",5,6,"cokolwiek");
                //dbManager.addScheduleLesson(6,3,4);
                //dbManager.addCompletedLesson("28.01.2015",7,5,"gowno");
                //dbManager.addStudentAbsence("96091227824",3);
                //dbManager.addStudentNote("96091227824",7,"asasasas",false,"10.04.2013");

                //dbManager.yearEnd();
                //Pair p = dbManager.signIn("kyuu", "kyuu");
               // System.out.println(p.getX().toString() + p.getY().toString());
                //launch(args);

        }
}

import utils.*;

import java.sql.*;
import java.util.ArrayList;

public class DBManager {
        Connection c;
        Statement stmt;

        public DBManager() {
                try {
                        Class.forName("org.postgresql.Driver");
                        c = DriverManager
                                .getConnection(Constants.DB_ADRESS,
                                        Constants.LOGIN, Constants.PASSWORD);

                } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println(e.getClass().getName() + ": " + e.getMessage());
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

                                grades.add(topic + " " + name + " " + value);
                        }

                        rs.close();
                        stmt.close();
                } catch (Exception e) {
                        return null;
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

                                absences.add(date + " lekcja nr: " + lesson);
                        }

                        rs.close();
                        stmt.close();
                } catch (Exception e) {
                        return null;

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

                                notes.add(date + " " + positive + ": " + description);
                        }

                        rs.close();
                        stmt.close();
                } catch (Exception e) {
                        return null;

                }
                return notes;
        }

        public ArrayList<Pair<Integer, String>> getStudentSubjects(String studentID) {
                ArrayList<Pair<Integer, String>> subjects = new ArrayList<Pair<Integer, String>>();
                try {
                        stmt = c.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT p.id,nazwa from przedmioty p join klasy k on p.id_klasy=k.id join uczniowie u on u.id_klasy=k.id where pesel='" + studentID + "';");
                        while (rs.next()) {
                                Pair<Integer, String> pair = new Pair<Integer, String>(rs.getInt("id"), rs.getString("nazwa"));


                                subjects.add(pair);
                        }

                        rs.close();
                        stmt.close();
                } catch (Exception e) {
                        return null;

                }
                return subjects;
        }

        public ArrayList<Pair<Integer, String>> getTeacherSubjects(int teacherID) {
                ArrayList<Pair<Integer, String>> subjects = new ArrayList<Pair<Integer, String>>();
                try {
                        stmt = c.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT p.id,nazwa,oddzial,rok_rozpoczecia from przedmioty p join klasy k on p.id_klasy=k.id where aktywny=true and id_prowadzacego='" + teacherID + "';");
                        while (rs.next()) {
                                Pair<Integer, String> pair = new Pair<Integer, String>(rs.getInt("id"), rs.getString("nazwa") + " klasa: " + rs.getString("oddzial") + " " + rs.getInt("rok_rozpoczecia"));
                                String name = rs.getString("nazwa");
                                String section = rs.getString("oddzial");
                                int startYear = rs.getInt("rok_rozpoczecia");

                                subjects.add(pair);
                        }

                        rs.close();
                        stmt.close();
                } catch (Exception e) {
                        return null;

                }
                return subjects;
        }

        public ArrayList<Pair<String, String>> getSubjectStudents(int subjectID) {
                ArrayList<Pair<String, String>> students = new ArrayList<Pair<String, String>>();
                try {
                        stmt = c.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT imie,nazwisko,pesel from przedmioty p join klasy k on p.id_klasy=k.id join uczniowie u on u.id_klasy=k.id where p.id='" + subjectID + "';");
                        while (rs.next()) {
                                String name = rs.getString("imie");
                                String lastname = rs.getString("nazwisko");
                                String pesel = rs.getString("pesel");
                                Pair<String, String> pair = new Pair<String, String>(rs.getString("pesel"), rs.getString("imie") + " " + rs.getString("nazwisko"));

                                students.add(pair);
                        }

                        rs.close();
                        stmt.close();
                } catch (Exception e) {
                        return null;

                }
                return students;
        }

        public boolean addStudentGrade(int subjectID, String studentID, int gradeValue, int activityID, String topic) {
                try {
                        PreparedStatement ps = c.prepareStatement("INSERT INTO oceny_uczniow(id_przedmiotu,id_ucznia,wartosc,id_aktywnosci,tematyka) values(?,?,?,?,?);");
                        ps.setInt(1, subjectID);
                        ps.setString(2, studentID);
                        ps.setInt(3, gradeValue);
                        ps.setInt(4, activityID);
                        ps.setString(5, topic);
                        ps.executeUpdate();
                        ps.close();
                        return true;
                } catch (Exception e) {
                        return false;
                }
        }

        public boolean addStudentNote(String studentID, int teacherID, String note, boolean isPositive, String date) {
                try {
                        PreparedStatement ps = c.prepareStatement("INSERT INTO uwagi(id_ucznia,id_nauczyciela,opis,czy_pozytywna,data_wystawienia) values(?,?,?,?,to_date(?, 'DD.MM.YYYY'));");
                        ps.setString(1, studentID);
                        ps.setInt(2, teacherID);
                        ps.setString(3, note);
                        ps.setBoolean(4, isPositive);
                        ps.setString(5, date);
                        ps.executeUpdate();
                        ps.close();

                        return true;
                } catch (Exception e) {
                        return false;
                }
        }

        public boolean addStudentAbsence(String studentID, int lessonID) {
                try {
                        stmt = c.createStatement();
                        stmt.executeUpdate("INSERT INTO nieobecnosci(id_ucznia,id_lekcji) values('" + studentID + "'," + lessonID + ");");

                        stmt.close();
                        return true;
                } catch (Exception e) {
                        return false;
                }
        }

        public int addCompletedLesson(String data, int teacherID, int lessonID, String topic) {
                int i = -1;
                try {
                        PreparedStatement ps = c.prepareStatement("INSERT INTO przeprowadzone_lekcje(data,id_prowadzacego,id_lekcji,temat_zajec) values(to_date(?, 'DD.MM.YYYY'),?,?,?);");
                        ps.setString(1, data);
                        ps.setInt(2, teacherID);
                        ps.setInt(3, lessonID);
                        ps.setString(4, topic);
                        ps.executeUpdate();
                        ps.close();
                        stmt = c.createStatement();
                        ResultSet rs = stmt.executeQuery("select lastval();");
                        rs.next();
                        i = rs.getInt(1);
                        stmt.close();
                } catch (Exception e) {
                        return -1;
                }
                return i;
        }

        public boolean addStudent(String name, String lastname, String pesel, int phoneNumber, int classID) {
                try {
                        PreparedStatement ps = c.prepareStatement("INSERT INTO uczniowie(imie,nazwisko,pesel,telefon_do_rodzica,id_klasy) values(?,?,?,?,?);");
                        ps.setString(1, name);
                        ps.setString(2, lastname);
                        ps.setString(3, pesel);
                        ps.setInt(4, phoneNumber);
                        ps.setInt(5, classID);
                        ps.executeUpdate();
                        ps.close();
                        return true;
                } catch (Exception e) {
                        return false;
                }
        }

        public boolean addclass(String section, int startYear, int tutorID) {
                try {
                        PreparedStatement ps = c.prepareStatement("INSERT INTO klasy(oddzial,rok_rozpoczecia,id_wychowawcy) values(?,?,?);");
                        ps.setString(1, section);
                        ps.setInt(2, startYear);
                        ps.setInt(3, tutorID);
                        ps.executeUpdate();
                        ps.close();
                        return true;
                } catch (Exception e) {
                        return false;
                }
        }

        public boolean addTeacher(String name, String lastname) {
                try {
                        PreparedStatement ps = c.prepareStatement("INSERT INTO nauczyciele(imie,nazwisko) values(?,?);");
                        ps.setString(1, name);
                        ps.setString(2, lastname);
                        ps.executeUpdate();
                        ps.close();
                        return true;
                } catch (Exception e) {
                        return false;
                }
        }

        public boolean addSubject(String name, int classID, int teacherID) {
                try {
                        PreparedStatement ps = c.prepareStatement("INSERT INTO przedmioty(nazwa,id_klasy,id_prowadzacego) values(?,?,?);");
                        ps.setString(1, name);
                        ps.setInt(2, classID);
                        ps.setInt(3, teacherID);
                        ps.executeUpdate();
                        ps.close();
                        return true;
                } catch (Exception e) {
                        return false;
                }
        }

        public boolean addScheduleLesson(int subjectID, int lessonNumber, int weekday) {
                try {
                        stmt = c.createStatement();
                        stmt.executeUpdate("INSERT INTO plan_lekcji(id_przedmiotu,nr_lekcji,dzien_tygodnia) values(" + subjectID + "," + lessonNumber + "," + weekday + ");");
                        stmt.close();
                        return true;
                } catch (Exception e) {
                        return false;
                }
        }

        public boolean yearEnd() {
                try {
                        stmt = c.createStatement();
                        stmt.executeUpdate("UPDATE przedmioty SET aktywny = FALSE;");
                        stmt.close();
                        return true;
                } catch (Exception e) {
                        return false;
                }
        }

        public boolean deactivateStudent(String studentID) {
                try {
                        stmt = c.createStatement();
                        stmt.executeUpdate("UPDATE uczniowie SET aktywny = false where pesel='" + studentID + "';");
                        stmt.close();
                        return true;
                } catch (Exception e) {
                        return false;
                }
        }

        public boolean addStudentUser(String login, String password, String pesel) {
                try {
                        PreparedStatement ps = c.prepareStatement("INSERT INTO uzytkownicy(login,haslo) values(?,?);");
                        ps.setString(1, login);
                        ps.setString(2, password);
                        ps.executeUpdate();
                        ps.close();
                        ps = c.prepareStatement("UPDATE uczniowie SET id_uzytkownika = ? where pesel=?;");
                        ps.setString(1, login);
                        ps.setString(2, pesel);
                        ps.executeUpdate();
                        ps.close();
                        return true;
                } catch (Exception e) {
                        return false;
                }
        }

        public boolean addTeacherUser(String login, String password, int teacherID) {
                try {
                        PreparedStatement ps = c.prepareStatement("INSERT INTO uzytkownicy(login,haslo) values(?,?);");
                        ps.setString(1, login);
                        ps.setString(2, password);
                        ps.executeUpdate();
                        ps.close();
                        ps = c.prepareStatement("UPDATE nauczyciele SET id_uzytkownika = ? where id=?;");
                        ps.setString(1, login);
                        ps.setInt(2, teacherID);
                        ps.executeUpdate();
                        ps.close();
                        return true;
                } catch (Exception e) {
                        return false;
                }
        }

        public User signIn(String login, String password) {
                try {
                        PreparedStatement ps = c.prepareStatement("SELECT * from uzytkownicy join uczniowie on login=id_uzytkownika where login=?;");
                        ps.setString(1, login);
                        ResultSet rs = ps.executeQuery();
                        while (rs.next()) {
                                String pesel = rs.getString("pesel");
                                String pass = rs.getString("haslo");
                                if (!password.equals(pass)) return null;
                                return new User(pesel, AccountType.STUDENT, pass);
                        }
                        ps = c.prepareStatement("SELECT * from uzytkownicy join nauczyciele on login=id_uzytkownika where login=?';");
                        ps.setString(1, login);
                        rs = ps.executeQuery();
                        while (rs.next()) {
                                int id = rs.getInt("id");
                                String pass = rs.getString("haslo");
                                if (!password.equals(pass)) return null;
                                return new User(Integer.toString(id), AccountType.TEACHER, pass);
                        }
                        ps.close();
                        return null;
                } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println(e.getClass().getName() + ": " + e.getMessage());
                }
                return null;
        }

        public ArrayList<ArrayList<String>> getLessonShedule(String pesel) {
                ArrayList<ArrayList<String>> shedule = new ArrayList<ArrayList<String>>();
                for (int i = 0; i < 5; i++) {
                        shedule.add(new ArrayList<String>());
                }
                try {
                        for (int i = 2; i <= 6; i++) {
                                stmt = c.createStatement();
                                ResultSet rs = stmt.executeQuery("select p.nazwa,pl.nr_lekcji from plan_lekcji pl join przedmioty p on pl.id_przedmiotu = p.id join klasy k on p.id_klasy = k.id join uczniowie u on k.id=u.id_klasy where dzien_tygodnia = " + i + " and u.pesel = '" + pesel + "' order by pl.nr_lekcji;");
                                int j = 0;
                                while (rs.next()) {
                                        int tmp = rs.getInt("nr_lekcji");
                                        while (j < tmp) {
                                                shedule.get(i - 2).add("");
                                                j++;
                                        }
                                        shedule.get(i - 2).add(rs.getString("nazwa"));
                                        j++;
                                }
                                while (shedule.get(i - 2).size() < 10) shedule.get(i - 2).add("");
                                rs.close();
                                stmt.close();
                        }
                } catch (Exception e) {
                        return null;
                }
                return shedule;
        }

        public ArrayList<Pair<String, String>> getStudentsWithoutUser() {
                ArrayList<Pair<String, String>> students = new ArrayList<Pair<String, String>>();
                try {
                        stmt = c.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT imie,nazwisko,pesel from uczniowie where id_uzytkownika is null;");
                        while (rs.next()) {
                                Pair<String, String> pair = new Pair<String, String>(rs.getString("pesel"), rs.getString("imie") + " " + rs.getString("nazwisko"));
                                students.add(pair);
                        }
                        rs.close();
                        stmt.close();
                } catch (Exception e) {
                        return null;
                }
                return students;
        }

        public ArrayList<Pair<Integer, String>> getTeachersWithoutUser() {
                ArrayList<Pair<Integer, String>> teachers = new ArrayList<Pair<Integer, String>>();
                try {
                        stmt = c.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT imie,nazwisko,id from nauczyciele where id_uzytkownika is null;");
                        while (rs.next()) {
                                Pair<Integer, String> pair = new Pair<Integer, String>(rs.getInt("id"), rs.getString("imie") + " " + rs.getString("nazwisko"));
                                teachers.add(pair);
                        }

                        rs.close();
                        stmt.close();
                } catch (Exception e) {
                        return null;
                }
                return teachers;
        }

        public boolean changeStudentPassword(String pesel, String password) {
                try {
                        PreparedStatement ps = c.prepareStatement("select id_uzytkownika from uzytkownicy uz join uczniowie on id_uzytkownika=uz.login where pesel=?;");
                        ps.setString(1, pesel);

                        ResultSet rs = ps.executeQuery();
                        rs.next();
                        String userID = rs.getString("id_uzytkownika");
                        ps = c.prepareStatement("UPDATE uzytkownicy SET haslo = ? where login=?;");
                        ps.setString(1, password);
                        ps.setString(2, userID);
                        ps.executeUpdate();
                        ps.close();
                        return true;
                } catch (Exception e) {
                        return false;
                }
        }

        public boolean changeTeacherPassword(int id, String password) {
                try {
                        PreparedStatement ps = c.prepareStatement("select id_uzytkownika from uzytkownicy uz join nauczyciele on id_uzytkownika=uz.login where id=?;");
                        ps.setInt(1, id);

                        ResultSet rs = ps.executeQuery();
                        rs.next();
                        String userID = rs.getString("id_uzytkownika");
                        ps = c.prepareStatement("UPDATE uzytkownicy SET haslo = '" + password + "' where login = '" + userID + "';");
                        ps.setString(1, password);
                        ps.setString(2, userID);
                        ps.executeUpdate();
                        ps.close();
                        return true;
                } catch (Exception e) {
                        return false;
                }
        }

        public ArrayList<Pair<Integer, String>> getActivities() {
                ArrayList<Pair<Integer, String>> activities = new ArrayList<Pair<Integer, String>>();
                try {
                        stmt = c.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT id,nazwa from rodzaje_aktywnosci;");
                        while (rs.next()) {
                                Pair<Integer, String> pair = new Pair<Integer, String>(rs.getInt("id"), rs.getString("nazwa"));
                                activities.add(pair);
                        }
                        rs.close();
                        stmt.close();
                } catch (Exception e) {
                        return null;
                }
                return activities;
        }

        public ArrayList<Pair<Integer, String>> getLessonsByDate(String data) {
                ArrayList<Pair<Integer, String>> lessons = new ArrayList<Pair<Integer, String>>();
                try {
                        stmt = c.createStatement();

                        ResultSet rs = stmt.executeQuery("SELECT pl.id,k.oddzial,k.rok_rozpoczecia,p.nazwa,pl.nr_lekcji from plan_lekcji pl join przedmioty p on pl.id_przedmiotu = p.id join klasy k on p.id_klasy = k.id where pl.dzien_tygodnia=extract(dow from to_date('" + data + "', 'DD.MM.YYYY'))+1;");
                        while (rs.next()) {
                                Pair<Integer, String> pair = new Pair<Integer, String>(rs.getInt("id"), "godzina lekcyjna: " + rs.getInt("nr_lekcji") + " " + rs.getString("nazwa") + " klasa: " + rs.getString("oddzial") + " " + rs.getInt("rok_rozpoczecia"));
                                lessons.add(pair);
                        }

                        rs.close();
                        stmt.close();
                } catch (Exception e) {
                        return null;
                }
                return lessons;
        }

        public ArrayList<Pair<String, String>> getStudentsByLesson(int lessonID) {
                ArrayList<Pair<String, String>> students = new ArrayList<Pair<String, String>>();
                try {
                        stmt = c.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT imie,nazwisko,pesel from uczniowie u join klasy k on k.id =u.id_klasy join przedmioty p on p.id_klasy=k.id join plan_lekcji pl on pl.id_przedmiotu =p.id join przeprowadzone_lekcje p_l on pl.id=p_l.id_lekcji where p_l.id=" + lessonID + ";");
                        while (rs.next()) {
                                Pair<String, String> pair = new Pair<String, String>(rs.getString("pesel"), rs.getString("imie") + " " + rs.getString("nazwisko"));
                                students.add(pair);
                        }

                        rs.close();
                        stmt.close();
                } catch (Exception e) {
                        return null;
                }
                return students;
        }

        public ArrayList<Pair<Integer, String>> getAllClasses() {
                ArrayList<Pair<Integer, String>> classes = new ArrayList<Pair<Integer, String>>();
                try {
                        stmt = c.createStatement();
                        ResultSet rs = stmt.executeQuery("select * from klasy");
                        while (rs.next()) {
                                classes.add(new Pair<Integer, String>(rs.getInt("id"), rs.getString("oddzial") + " " + rs.getString("rok_rozpoczecia")));
                        }
                        stmt.close();
                } catch (Exception e) {
                        return null;
                }
                return classes;
        }

        public ArrayList<Pair<String, String>> getAllStudents() {
                ArrayList<Pair<String, String>> students = new ArrayList<Pair<String, String>>();
                try {
                        stmt = c.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT imie,nazwisko,pesel from uczniowie where aktywny=true;");
                        while (rs.next()) {
                                Pair<String, String> pair = new Pair<String, String>(rs.getString("pesel"), rs.getString("imie") + " " + rs.getString("nazwisko"));
                                students.add(pair);
                        }

                        rs.close();
                        stmt.close();
                } catch (Exception e) {
                        return null;
                }
                return students;
        }

        public ArrayList<Pair<String, String>> getAllStudentsByAdmin() {
                ArrayList<Pair<String, String>> students = new ArrayList<Pair<String, String>>();
                try {
                        stmt = c.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT * from uczniowie where aktywny=true;");
                        while (rs.next()) {
                                Pair<String, String> pair = new Pair<String, String>(rs.getString("pesel"), rs.getString("pesel") + " " + rs.getString("imie") + " " + rs.getString("nazwisko") + " " + rs.getString("telefon_do_rodzica"));
                                students.add(pair);
                        }
                        rs.close();
                        stmt.close();
                } catch (Exception e) {
                        return null;
                }
                return students;
        }

        public ArrayList<Pair<Integer, String>> getTeachersWithoutClass() {
                ArrayList<Pair<Integer, String>> teachers = new ArrayList<Pair<Integer, String>>();
                try {
                        stmt = c.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT imie,nazwisko,id from (SELECT imie,nazwisko,n.id,id_wychowawcy from nauczyciele n left join klasy k on n.id=k.id_wychowawcy) f where id_wychowawcy is null;");
                        while (rs.next()) {
                                Pair<Integer, String> pair = new Pair<Integer, String>(rs.getInt("id"), rs.getString("imie") + " " + rs.getString("nazwisko"));
                                teachers.add(pair);
                        }
                        rs.close();
                        stmt.close();
                } catch (Exception e) {
                        return null;
                }
                return teachers;
        }

        public ArrayList<Pair<Integer, String>> getAllTeachers() {
                ArrayList<Pair<Integer, String>> teachers = new ArrayList<Pair<Integer, String>>();
                try {
                        stmt = c.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT imie,nazwisko,id from nauczyciele");
                        while (rs.next()) {
                                Pair<Integer, String> pair = new Pair<Integer, String>(rs.getInt("id"), rs.getString("imie") + " " + rs.getString("nazwisko"));
                                teachers.add(pair);
                        }
                        rs.close();
                        stmt.close();
                } catch (Exception e) {
                        return null;
                }
                return teachers;
        }

        public ArrayList<ArrayList<String>> getTeacherSchedule(int id) {
                ArrayList<ArrayList<String>> shedule = new ArrayList<ArrayList<String>>();
                for (int i = 0; i < 5; i++) {
                        shedule.add(new ArrayList<String>());
                }
                try {
                        for (int i = 2; i <= 6; i++) {
                                stmt = c.createStatement();
                                ResultSet rs = stmt.executeQuery("select p.nazwa,pl.nr_lekcji,k.oddzial,k.rok_rozpoczecia from plan_lekcji pl join przedmioty p on pl.id_przedmiotu = p.id join klasy k on p.id_klasy = k.id join nauczyciele n on n.id=p.id_prowadzacego where dzien_tygodnia = " + i + " and n.id = '" + id + "' order by pl.nr_lekcji;");
                                int j = 0;
                                while (rs.next()) {
                                        int tmp = rs.getInt("nr_lekcji");
                                        while (j < tmp) {
                                                shedule.get(i - 2).add("");
                                                j++;
                                        }
                                        shedule.get(i - 2).add(rs.getString("nazwa") + "(" + rs.getInt("rok_rozpoczecia") + rs.getString("oddzial") + ")");
                                        j++;
                                }
                                while (shedule.get(i - 2).size() < 10) shedule.get(i - 2).add("");
                                rs.close();
                                stmt.close();
                        }
                } catch (Exception e) {
                        return null;
                }
                return shedule;
        }
}

import utils.AccountType;
import utils.*;

/**
 * Created by Kyuubi on 2015-02-04.
 */
public interface UserInterface {
    public void initApplication();
    public Pair<String, AccountType> signIn();
    public void getStudentGrades();
    public void getStudentAbsences();
    public void getStudentNotes();

    public void getTeacherSubject();
    public void getSubjectStudents();
    public void addStudentGrade();
    public void addStudentNote();
    public void addStudentAbsence();
    public void addCompletedLesson();




}

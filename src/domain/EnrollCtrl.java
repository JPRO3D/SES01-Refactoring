package domain;

import java.util.List;
import java.util.Map;

import domain.exceptions.EnrollmentRulesViolationException;

public class EnrollCtrl {
    public void enroll(Student s, List<CSE> courses) throws EnrollmentRulesViolationException {
        Map<Term, Map<Course, Double>> transcript = s.getTranscript();

        checkIsPassed(courses, transcript);

        checkPrerequisitesPassed(courses, transcript);

        checkExamTimeConflict(courses);

        checkDuplicateCourse(courses);

        int unitsRequested = 0;
        for (CSE object : courses)
            unitsRequested += object.getCourse().getUnits();
        double points = 0;
        int totalUnits = 0;

        for (Map.Entry<Term, Map<Course, Double>> tr : transcript.entrySet()) {
            for (Map.Entry<Course, Double> r : tr.getValue().entrySet()) {
                points += r.getValue() * r.getKey().getUnits();
                totalUnits += r.getKey().getUnits();
            }
        }

        if ((getGpa(points, totalUnits) < 12 && unitsRequested > 14) || (getGpa(points, totalUnits) < 16 && unitsRequested > 16)
                || (unitsRequested > 20))
            throw new EnrollmentRulesViolationException(String.format("Number of units (%d) requested does not match GPA of %f",
                    unitsRequested, getGpa(points, totalUnits)));
        for (CSE o : courses)
            s.takeCourse(o.getCourse(), o.getSection());
    }

    private void checkDuplicateCourse(List<CSE> courses) throws EnrollmentRulesViolationException {
        for (CSE object1 : courses) {
            for (CSE object2 : courses) {
                if (object1 == object2) continue;
                if (object1.getCourse().equals(object2.getCourse()))
                    throw new EnrollmentRulesViolationException(String.format("%s is requested to be taken twice",
                            object1.getCourse().getName()));
            }
        }
    }

    private void checkExamTimeConflict(List<CSE> courses) throws EnrollmentRulesViolationException {
        for (CSE object1 : courses) {
            for (CSE object2 : courses) {
                if (object1 == object2) continue;
                if (object1.getExamTime().equals(object2.getExamTime()))
                    throw new EnrollmentRulesViolationException(String.format("Two offerings %s and %s have the same exam time",
                            object1, object2));
            }
        }
    }

    private void checkIsPassed(List<CSE> courses, Map<Term, Map<Course, Double>> transcript) throws EnrollmentRulesViolationException {
        for (CSE object : courses) {
            for (Map.Entry<Term, Map<Course, Double>> tr : transcript.entrySet()) {
                for (Map.Entry<Course, Double> r : tr.getValue().entrySet()) {
                    if (r.getKey().equals(object.getCourse()) && r.getValue() >= 10)
                        throw new EnrollmentRulesViolationException(String.format("The student has already passed %s",
                                object.getCourse().getName()));
                }
            }
        }
    }

    private void checkPrerequisitesPassed(List<CSE> courses, Map<Term, Map<Course, Double>> transcript)
            throws EnrollmentRulesViolationException {
        for (CSE object : courses) {
            List<Course> prereqs = object.getCourse().getPrerequisites();

            nextPre:
            for (Course pre : prereqs) {
                for (Map.Entry<Term, Map<Course, Double>> tr : transcript.entrySet()) {
                    for (Map.Entry<Course, Double> r : tr.getValue().entrySet()) {
                        if (r.getKey().equals(pre) && r.getValue() >= 10) continue nextPre;
                    }
                }
                throw new EnrollmentRulesViolationException(String.format("The student has not passed %s as a prerequisite of %s",
                        pre.getName(), object.getCourse().getName()));
            }
        }
    }

    private double getGpa(double points, int totalUnits) {
        return points / totalUnits;
    }
}
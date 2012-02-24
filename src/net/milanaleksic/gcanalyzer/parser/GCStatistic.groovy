package net.milanaleksic.gcanalyzer.parser

/**
 * User: Milan Aleksic
 * Date: 1/29/12
 * Time: 12:09 PM
 */
@groovy.transform.Immutable
final class GCStatistic {

    String gcName

    long startValueInB

    long endValueInB

    long maxValueInB

    long startValueInKB

    long endValueInKB

    long maxValueInKB

    @Newify([GCStatistic])
    public static GCStatistic create(String gcName, long startValueInB, long endValueInB, long maxValueInB) {
        GCStatistic(
                gcName: gcName,
                startValueInB: startValueInB, endValueInB: endValueInB, maxValueInB: maxValueInB,
                startValueInKB: startValueInB >> 10, endValueInKB: endValueInB >> 10, maxValueInKB: maxValueInB >> 10
        )
    }

}

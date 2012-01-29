package net.milanaleksic.gcanalyzer

import groovy.transform.Immutable

/**
 * User: Milan Aleksic
 * Date: 1/29/12
 * Time: 12:09 PM
 */
@Immutable
final class SingleGCStatistic {

    String gcName

    long startValueInB

    long endValueInB

    long maxValueInB

}

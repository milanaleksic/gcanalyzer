package net.milanaleksic.gcanalyzer.parser

import net.milanaleksic.gcanalyzer.parser.SingleGCStatistic

/**
 * User: Milan Aleksic
 * Date: 1/29/12
 * Time: 11:37 AM
 */
@Immutable
final class GCEvent {

    def Date time

    def long timeInMillis

    def Map<String, SingleGCStatistic> stats

    def String gcEventName

    long userTiming

    long sysTiming

    long realTiming

    long completeEventTimeInMicroSeconds

    public boolean isFullGarbageCollection() {
        return gcEventName?.contains('Full')
    }

}

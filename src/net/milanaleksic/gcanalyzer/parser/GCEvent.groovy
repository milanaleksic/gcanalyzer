package net.milanaleksic.gcanalyzer.parser

/**
 * User: Milan Aleksic
 * Date: 1/29/12
 * Time: 11:37 AM
 */
@Immutable
final class GCEvent {

    def Date moment

    def long momentInMillis

    def Map<String, SingleGCStatistic> stats

    def String gcEventName

    long userTiming

    long sysTiming

    long realTiming

    long completeEventTimeInMicroSeconds

    GCSurvivorDetails survivorDetails

    public boolean isFullGarbageCollection() {
        return gcEventName?.contains('Full')
    }

    public boolean isExplicitFullGarbageCollection() {
        return isFullGarbageCollection() && gcEventName?.contains('(System)')
    }

}

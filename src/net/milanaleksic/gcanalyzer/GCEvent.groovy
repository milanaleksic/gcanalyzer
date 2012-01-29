package net.milanaleksic.gcanalyzer

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

    public boolean isFullGarbageCollection() {
        return gcEventName?.contains('Full')
    }

}

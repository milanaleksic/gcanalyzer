package net.milanaleksic.gcanalyzer.parser

/**
 * User: Milan Aleksic
 * Date: 1/29/12
 * Time: 11:37 AM
 */
@groovy.transform.Immutable
final class GCEvent {

    def Date moment

    def long momentInMillis

    def GCStatistics stats

    def String gcEventName

    def Long userTiming

    def Long sysTiming

    def Long realTiming

    def long completeEventTimeInMicroSeconds

    def GCSurvivorDetails survivorDetails

    public boolean isFullGarbageCollection() {
        return gcEventName?.contains('Full')
    }

    public boolean isExplicitFullGarbageCollection() {
        return isFullGarbageCollection() && gcEventName?.contains('(System)')
    }

}

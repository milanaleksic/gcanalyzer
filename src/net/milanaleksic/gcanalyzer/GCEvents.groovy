package net.milanaleksic.gcanalyzer

/**
 * User: Milan Aleksic
 * Date: 1/29/12
 * Time: 1:21 PM
 */
@Immutable
final class GCEvents {

    def HashMap<Date, GCEvent> hashMapOnDate

    def HashMap<Long, GCEvent> hashMapOnMillis

    public int size() {
        return hashMapOnDate.size()
    }

}

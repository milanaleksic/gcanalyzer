package net.milanaleksic.gcanalyzer.parser

/**
 * User: Milan Aleksic
 * Date: 1/29/12
 * Time: 1:21 PM
 */
final class GCEvents {

    private def HashMap<Date, GCEvent> hashMapOnDate

    private def HashMap<Long, GCEvent> hashMapOnMillis

    private def List<GCEvent> linkedList

    private int size

    public GCEvents(LinkedList<GCEvent> events) {
        hashMapOnDate = new LinkedHashMap<Date, GCEvent>()
        hashMapOnMillis = new LinkedHashMap<Long, GCEvent>()
        size = events.size()
        this.linkedList = Collections.unmodifiableList(events)
        linkedList.each { GCEvent event ->
            hashMapOnDate[event.moment] = hashMapOnMillis[event.momentInMillis] = event
        }
    }

    HashMap<Date, GCEvent> getHashMapOnDate() {
        return hashMapOnDate
    }

    HashMap<Long, GCEvent> getHashMapOnMillis() {
        return hashMapOnMillis
    }

    List<GCEvent> getLinkedList() {
        return linkedList
    }

    public int size() {
        return size
    }

}

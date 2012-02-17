package net.milanaleksic.gcanalyzer.graphing;

import net.milanaleksic.gcanalyzer.parser.GCEvents;
import org.junit.Test
import net.milanaleksic.gcanalyzer.parser.GCEvent
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.MatcherAssert.assertThat;

public class GCLogInformationSourceTest {

    private GCEvents getDummyData() {
        def event1 = new GCEvent(moment: new Date(), momentInMillis: 5,
            gcEventName: 'GC', stats: null,
            survivorDetails: null,
            userTiming: 0, sysTiming: 1, realTiming: 2,
            completeEventTimeInMicroSeconds: 5000
        )
        def event2 = new GCEvent(moment: new Date(), momentInMillis: 10,
            gcEventName: 'GC', stats: null,
            survivorDetails: null,
            userTiming: 0, sysTiming: 1, realTiming: 2,
            completeEventTimeInMicroSeconds: 3000
        )
        def event3 = new GCEvent(moment: new Date(), momentInMillis: 15,
            gcEventName: 'Full GC', stats: null,
            survivorDetails: null,
            userTiming: 0, sysTiming: 1, realTiming: 2,
            completeEventTimeInMicroSeconds: 1000
        )
        def mapOnMillis = [:]
        mapOnMillis[event1.momentInMillis] = event1
        mapOnMillis[event2.momentInMillis] = event2
        mapOnMillis[event3.momentInMillis] = event3
        def mapOnDate = [:]
        mapOnDate[event1.moment] = event1
        mapOnDate[event2.moment] = event2
        mapOnDate[event3.moment] = event3
        return new GCEvents(hashMapOnDate:mapOnDate, hashMapOnMillis: mapOnMillis)
    }

    @Test
    void parseSimpleGc() {
        GCLogInformationSource source = new GCLogInformationSource(getDummyData(), "", "")

        assertThat(source.numberOfDetectedYoungGCEvents(), equalTo(2))
        assertThat(source.numberOfDetectedFullGCEvents(), equalTo(1))

        assertThat(new BigDecimal(source.averageYoungGCEventLength()), equalTo(new BigDecimal(4.0)))
        assertThat(new BigDecimal(source.averageFullGCEventLength()), equalTo(new BigDecimal(1.0)))

        assertThat(new BigDecimal(source.standardDeviationYoungGCEventLength()), equalTo(new BigDecimal(Math.sqrt(2))))
        assertThat(Double.isNaN(source.standardDeviationFullGCEventLength()), equalTo(true))
    }

}

package net.milanaleksic.gcanalyzer.graphing;

import net.milanaleksic.gcanalyzer.parser.GCEvents;
import org.junit.Test
import net.milanaleksic.gcanalyzer.parser.GCEvent
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.MatcherAssert.assertThat;

public class GCLogInformationSourceTest {

    private GCEvents getDummyData() {
        LinkedList<GCEvent> events = new LinkedList<GCEvent>([
            new GCEvent(moment: new Date(), momentInMillis: 5,
                gcEventName: 'GC', stats: null,
                survivorDetails: null,
                userTiming: 0, sysTiming: 1, realTiming: 2,
                completeEventTimeInMicroSeconds: 5000
            ),
            new GCEvent(moment: new Date(), momentInMillis: 10,
                gcEventName: 'GC', stats: null,
                survivorDetails: null,
                userTiming: 0, sysTiming: 1, realTiming: 2,
                completeEventTimeInMicroSeconds: 3000
            ),
            new GCEvent(moment: new Date(), momentInMillis: 15,
                gcEventName: 'Full GC', stats: null,
                survivorDetails: null,
                userTiming: 0, sysTiming: 1, realTiming: 2,
                completeEventTimeInMicroSeconds: 1000
            )
        ])
        return new GCEvents(events)
    }

    @Test
    void parseSimpleGc() {
        GCLogInformationSource source = new GCLogInformationSource(getDummyData(), "", "")

        assertThat(source.numberOfDetectedYoungGCEvents(), equalTo(2))
        assertThat(source.numberOfDetectedFullGCEvents(), equalTo(1))

        assertThat(new BigDecimal(source.averageYoungGCEventLength()), equalTo(new BigDecimal(4.0)))
        assertThat(new BigDecimal(source.averageFullGCEventLength()), equalTo(new BigDecimal(1.0)))

        assertThat(new BigDecimal(source.standardDeviationYoungGCEventLength()), equalTo(new BigDecimal(Math.sqrt(2.0))))
        assertThat(Double.isNaN(source.standardDeviationFullGCEventLength()), equalTo(true))
    }

}

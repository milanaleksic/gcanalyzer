package net.milanaleksic.gcanalyzer

import org.junit.Test
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

/**
 * User: Milan Aleksic
 * Date: 1/29/12
 * Time: 12:33 PM
 */
class GCLogParserTest {

    @Test
    void parseSingleLine() {
        GCEvents events = new GCLogParser('2012-01-24T07:45:45.767+0000: 135213.292: [GC [PSYoungGen: 42939K->1191K(44480K)] 60259K->18511K(72256K), 0.0065950 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] ').parse()
        assertThat(events.size(), equalTo(1))
        GCEvent event = events.hashMapOnMillis[135213292L]
        assertThat(event, not(nullValue(GCEvent.class)))
        assertThat(events.hashMapOnDate[event.time], equalTo(event))

        assertThat(event.gcEventName, equalTo('GC'))

        assertThat(event.stats.size(), equalTo(2))
        assertThat(event.stats[null].startValueInB, equalTo(60259L*1024))
        assertThat(event.stats[null].endValueInB, equalTo(18511L*1024))
        assertThat(event.stats[null].maxValueInB, equalTo(72256L*1024))
        assertThat(event.stats['PSYoungGen'].startValueInB, equalTo(42939L*1024))
        assertThat(event.stats['PSYoungGen'].endValueInB, equalTo(1191L*1024))
        assertThat(event.stats['PSYoungGen'].maxValueInB, equalTo(44480L*1024))

    }

}

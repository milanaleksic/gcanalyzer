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
    void parseSimpleGc() {
        GCEvents events = new GCLogParser().parse('2012-01-24T07:45:45.767+0000: 135213.292: [GC [PSYoungGen: 42939K->1191K(44480K)] 60259K->18511K(72256K), 0.0065950 secs] [Times: user=0.01 sys=0.02, real=0.03 secs] ')
        assertThat(events.size(), equalTo(1))
        GCEvent event = events.hashMapOnMillis[135213292L]
        assertThat(event, not(nullValue(GCEvent.class)))
        assertThat(events.hashMapOnDate[event.time], equalTo(event))

        assertThat(event.gcEventName, equalTo('GC'))
        assertThat(event.isFullGarbageCollection(), equalTo(false))

        assertThat(event.stats.size(), equalTo(2))
        assertThat(event.stats[null].startValueInB, equalTo(60259L*1024))
        assertThat(event.stats[null].endValueInB, equalTo(18511L*1024))
        assertThat(event.stats[null].maxValueInB, equalTo(72256L*1024))
        assertThat(event.stats['PSYoungGen'].startValueInB, equalTo(42939L*1024))
        assertThat(event.stats['PSYoungGen'].endValueInB, equalTo(1191L*1024))
        assertThat(event.stats['PSYoungGen'].maxValueInB, equalTo(44480L*1024))
        assertThat(event.userTiming, equalTo(10L))
        assertThat(event.sysTiming, equalTo(20L))
        assertThat(event.realTiming, equalTo(30L))
        assertThat(event.completeEventTimeInMicroSeconds, equalTo(6595L))
    }

    @Test
    void parseFullGc() {
        GCEvents events = new GCLogParser().parse('2012-01-24T07:43:16.086+0000: 135063.611: [Full GC (System) [PSYoungGen: 1105K->0K(44544K)] [ParOldGen: 17108K->17319K(27776K)] 18213K->17319K(72320K) [PSPermGen: 40705K->40646K(41088K)], 0.2675810 secs] [Times: user=0.31 sys=0.01, real=0.27 secs] ')
        assertThat(events.size(), equalTo(1))
        GCEvent event = events.hashMapOnMillis[135063611L]
        assertThat(event, not(nullValue(GCEvent.class)))
        assertThat(events.hashMapOnDate[event.time], equalTo(event))

        assertThat(event.gcEventName, equalTo('Full GC (System)'))
        assertThat(event.isFullGarbageCollection(), equalTo(true))

        assertThat(event.stats.size(), equalTo(4))
        assertThat(event.stats[null].startValueInB, equalTo(18213L*1024))
        assertThat(event.stats[null].endValueInB, equalTo(17319L*1024))
        assertThat(event.stats[null].maxValueInB, equalTo(72320L*1024))
        assertThat(event.stats['PSYoungGen'].startValueInB, equalTo(1105L*1024)) // 1105K->0K(44544K)
        assertThat(event.stats['PSYoungGen'].endValueInB, equalTo(0L))
        assertThat(event.stats['PSYoungGen'].maxValueInB, equalTo(44544L*1024))
        assertThat(event.stats['ParOldGen'].startValueInB, equalTo(17108L*1024))  // 17108K->17319K(27776K)
        assertThat(event.stats['ParOldGen'].endValueInB, equalTo(17319L*1024))
        assertThat(event.stats['ParOldGen'].maxValueInB, equalTo(27776L*1024))
        assertThat(event.stats['PSPermGen'].startValueInB, equalTo(40705L*1024))
        assertThat(event.stats['PSPermGen'].endValueInB, equalTo(40646L*1024))
        assertThat(event.stats['PSPermGen'].maxValueInB, equalTo(41088L*1024))
        assertThat(event.userTiming, equalTo(310L))
        assertThat(event.sysTiming, equalTo(10L))
        assertThat(event.realTiming, equalTo(270L))
        assertThat(event.completeEventTimeInMicroSeconds, equalTo(267581L))
    }

}
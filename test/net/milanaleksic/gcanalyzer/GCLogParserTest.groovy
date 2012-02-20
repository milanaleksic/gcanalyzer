package net.milanaleksic.gcanalyzer

import org.junit.Test
import net.milanaleksic.gcanalyzer.parser.*
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*
import java.text.SimpleDateFormat
import org.hamcrest.Matchers

/**
 * User: Milan Aleksic
 * Date: 1/29/12
 * Time: 12:33 PM
 */
class GCLogParserTest {

    @Test
    void parseSimpleGc() {
        GCEvents events = new GCLogParser().parse('2012-01-24T07:45:12.765+0000: 135213.292: [GC [PSYoungGen: 42939K->1191K(44480K)] 60259K->18511K(72256K), 0.0065950 secs] [Times: user=0.01 sys=0.02, real=0.03 secs] ')
        assertThat(events.size(), equalTo(1))
        GCEvent event = events.hashMapOnMillis[135213292L]
        assertThat(event, not(nullValue(GCEvent.class)))
        assertThat(events.hashMapOnDate[event.moment], equalTo(event))
        assertThat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS").format(event.moment), equalTo("2012-01-24 07:45:12.765"))


        assertThat(event.gcEventName, equalTo('GC'))
        assertThat(event.isFullGarbageCollection(), equalTo(false))
        assertThat(event.isExplicitFullGarbageCollection(), equalTo(false))

        assertThat(event.stats.heapWithoutPermGen.startValueInB, equalTo(60259L*1024))
        assertThat(event.stats.heapWithoutPermGen.endValueInB, equalTo(18511L*1024))
        assertThat(event.stats.heapWithoutPermGen.maxValueInB, equalTo(72256L*1024))
        assertThat(event.stats.youngGeneration.startValueInB, equalTo(42939L*1024))
        assertThat(event.stats.youngGeneration.endValueInB, equalTo(1191L*1024))
        assertThat(event.stats.youngGeneration.maxValueInB, equalTo(44480L*1024))
        assertThat(event.userTiming, equalTo(10L))
        assertThat(event.sysTiming, equalTo(20L))
        assertThat(event.realTiming, equalTo(30L))
        assertThat(event.completeEventTimeInMicroSeconds, equalTo(6595L))
    }

    @Test
    void parseSimpleGcWithSurvivorSize() {
        // original text:
        // 2012-02-15T11:44:56.162+0100: 0.428: [GC
        // Desired survivor size 655360 bytes, new threshold 7 (max 15)
        // - age   1:   16690480 bytes,   16690480 total
        // [PSYoungGen: 8959K->631K(8960K)] 10158K->3053K(19904K), 0.0029015 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
        GCEvents events = new GCLogParser().parse('2012-02-15T11:44:56.162+0100: 0.428: [Full GCDesired survivor size 655360 bytes, new threshold 7 (max 15)- age   1:   16690480 bytes,   16690480 total [PSYoungGen: 8959K->631K(8960K)] 10158K->3053K(19904K), 0.0029015 secs] [Times: user=0.01 sys=0.02, real=0.03 secs]')
        assertThat(events.size(), equalTo(1))
        GCEvent event = events.hashMapOnMillis[428L]
        assertThat(event, not(nullValue(GCEvent.class)))
        assertThat(events.hashMapOnDate[event.moment], equalTo(event))

        assertThat(event.gcEventName, equalTo('Full GC'))
        assertThat(event.isFullGarbageCollection(), equalTo(true))
        assertThat(event.isExplicitFullGarbageCollection(), equalTo(false))

        assertThat(event.survivorDetails.newThreshold, equalTo(7))
        assertThat(event.survivorDetails.maxThreshold, equalTo(15))
        assertThat(event.survivorDetails.desiredSize, equalTo(655360L))
        assertThat(event.survivorDetails.endingTotalSize, equalTo(16690480L))

        assertThat(event.stats.heapWithoutPermGen.startValueInB, equalTo(10158L*1024))
        assertThat(event.stats.heapWithoutPermGen.endValueInB, equalTo(3053L*1024))
        assertThat(event.stats.heapWithoutPermGen.maxValueInB, equalTo(19904L*1024))
        assertThat(event.stats.youngGeneration.startValueInB, equalTo(8959L*1024))
        assertThat(event.stats.youngGeneration.endValueInB, equalTo(631L*1024))
        assertThat(event.stats.youngGeneration.maxValueInB, equalTo(8960L*1024))
        assertThat(event.userTiming, equalTo(10L))
        assertThat(event.sysTiming, equalTo(20L))
        assertThat(event.realTiming, equalTo(30L))
        assertThat(event.completeEventTimeInMicroSeconds, equalTo(2901L))
    }

    @Test
    void parseSimpleGcWithNoAgesSurvivorSize() {
        // original text:
        // 2012-02-15T11:44:56.162+0100: 0.428: [GC
        // Desired survivor size 655360 bytes, new threshold 7 (max 15)
        // - age   1:   16690480 bytes,   16690480 total
        // [PSYoungGen: 8959K->631K(8960K)] 10158K->3053K(19904K), 0.0029015 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
        GCEvents events = new GCLogParser().parse('2013-13-25T11:44:56.162+0100: 4.432: [Full GCDesired survivor size 655360 bytes, new threshold 2 (max 31) [PSYoungGen: 8959K->631K(8960K)] 10158K->3053K(19904K), 0.0029015 secs] [Times: user=0.01 sys=0.02, real=0.03 secs]')
        assertThat(events.size(), equalTo(1))
        GCEvent event = events.hashMapOnMillis[4432L]
        assertThat(event, not(nullValue(GCEvent.class)))

        assertThat(event.survivorDetails.newThreshold, equalTo(2))
        assertThat(event.survivorDetails.maxThreshold, equalTo(31))
        assertThat(event.survivorDetails.desiredSize, equalTo(655360L))
        assertThat(event.survivorDetails.endingTotalSize, Matchers.<Long>nullValue())
    }

    @Test
    void parseSimpleGcWithSingleAgeSurvivorSize() {
        // original text:
        // 2012-02-15T11:44:56.162+0100: 0.428: [GC
        // Desired survivor size 655360 bytes, new threshold 7 (max 15)
        // - age   1:   16690480 bytes,   16690480 total
        // [PSYoungGen: 8959K->631K(8960K)] 10158K->3053K(19904K), 0.0029015 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
        GCEvents events = new GCLogParser().parse('2013-13-25T11:44:56.162+0100: 4.432: [Full GCDesired survivor size 655360 bytes, new threshold 7 (max 15)- age   1:   16690480 bytes,   16690480 total [PSYoungGen: 8959K->631K(8960K)] 10158K->3053K(19904K), 0.0029015 secs] [Times: user=0.01 sys=0.02, real=0.03 secs]')
        assertThat(events.size(), equalTo(1))
        GCEvent event = events.hashMapOnMillis[4432L]
        assertThat(event, not(nullValue(GCEvent.class)))

        assertThat(event.survivorDetails.newThreshold, equalTo(7))
        assertThat(event.survivorDetails.maxThreshold, equalTo(15))
        assertThat(event.survivorDetails.desiredSize, equalTo(655360L))
        assertThat(event.survivorDetails.endingTotalSize, equalTo(16690480L))
    }

    @Test
    void parseSimpleGcWithDoubleAgeSurvivorSize() {
        GCEvents events = new GCLogParser().parse('''2012-02-16T08:51:03.093+0100: 0.164: [GC
Desired survivor size 655360 bytes, new threshold 5 (max 10)
- age   1:   16690480 bytes,   16690480 total
- age   2:   16690480 bytes,   10000000 total
 [PSYoungGen: 4160K->631K(4800K)] 4160K->1108K(15744K), 0.0020732 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
''')
        assertThat(events.size(), equalTo(1))
        GCEvent event = events.hashMapOnMillis[164L]
        assertThat(event, not(nullValue(GCEvent.class)))

        assertThat(event.survivorDetails.newThreshold, equalTo(5))
        assertThat(event.survivorDetails.maxThreshold, equalTo(10))
        assertThat(event.survivorDetails.desiredSize, equalTo(655360L))
        assertThat(event.survivorDetails.endingTotalSize, equalTo(10000000L))
    }

    @Test
    void parseFullGc() {
        GCEvents events = new GCLogParser().parse('2012-01-24T07:43:16.086+0000: 135063.611: [Full GC (System) [PSYoungGen: 1105K->0K(44544K)] [ParOldGen: 17108K->17319K(27776K)] 18213K->17319K(72320K) [PSPermGen: 40705K->40646K(41088K)], 0.2675810 secs] [Times: user=0.31 sys=0.01, real=0.27 secs] ')
        assertThat(events.size(), equalTo(1))
        GCEvent event = events.hashMapOnMillis[135063611L]
        assertThat(event, not(nullValue(GCEvent.class)))
        assertThat(events.hashMapOnDate[event.moment], equalTo(event))

        assertThat(event.gcEventName, equalTo('Full GC (System)'))
        assertThat(event.isFullGarbageCollection(), equalTo(true))
        assertThat(event.isExplicitFullGarbageCollection(), equalTo(true))

        assertThat(event.stats.heapWithoutPermGen.startValueInB, equalTo(18213L*1024))
        assertThat(event.stats.heapWithoutPermGen.endValueInB, equalTo(17319L*1024))
        assertThat(event.stats.heapWithoutPermGen.maxValueInB, equalTo(72320L*1024))
        assertThat(event.stats.youngGeneration.startValueInB, equalTo(1105L*1024)) // 1105K->0K(44544K)
        assertThat(event.stats.youngGeneration.endValueInB, equalTo(0L))
        assertThat(event.stats.youngGeneration.maxValueInB, equalTo(44544L*1024))
        assertThat(event.stats.oldGeneration.startValueInB, equalTo(17108L*1024))  // 17108K->17319K(27776K)
        assertThat(event.stats.oldGeneration.endValueInB, equalTo(17319L*1024))
        assertThat(event.stats.oldGeneration.maxValueInB, equalTo(27776L*1024))
        assertThat(event.stats.permanentGeneration.startValueInB, equalTo(40705L*1024))
        assertThat(event.stats.permanentGeneration.endValueInB, equalTo(40646L*1024))
        assertThat(event.stats.permanentGeneration.maxValueInB, equalTo(41088L*1024))
        assertThat(event.userTiming, equalTo(310L))
        assertThat(event.sysTiming, equalTo(10L))
        assertThat(event.realTiming, equalTo(270L))
        assertThat(event.completeEventTimeInMicroSeconds, equalTo(267581L))
    }

    @Test
    void serialGCComplexNonFullGC() {
        GCEvents events = new GCLogParser().parse('''2012-02-20T14:56:58.644+0100: 1.847: [GC 1.847: [DefNew
Desired survivor size 425984 bytes, new threshold 4 (max 15)
- age   1:     457704 bytes,     457704 total
- age   2:     259368 bytes,     717072 total
: 6938K->700K(7488K), 0.0033936 secs] 19845K->13607K(23980K), 0.0034379 secs] [Times: user=1.23 sys=3.21, real=4.44 secs]
''')
        assertThat(events.size(), equalTo(1))
        GCEvent event = events.hashMapOnMillis[1847L]
        assertThat(event, not(nullValue(GCEvent.class)))

        assertThat(event.gcEventName, equalTo('GC'))
        assertThat(event.isFullGarbageCollection(), equalTo(false))
        assertThat(event.isExplicitFullGarbageCollection(), equalTo(false))

        assertThat(event.stats.youngGeneration.startValueInB, equalTo(6938L*1024)) // 1105K->0K(44544K)
        assertThat(event.stats.youngGeneration.endValueInB, equalTo(700L))
        assertThat(event.stats.youngGeneration.maxValueInB, equalTo(7488L*1024))
        assertThat(event.stats.heapWithoutPermGen.startValueInB, equalTo(19845L*1024))
        assertThat(event.stats.heapWithoutPermGen.endValueInB, equalTo(13607L*1024))
        assertThat(event.stats.heapWithoutPermGen.maxValueInB, equalTo(23980L*1024))
        assertThat(event.stats.oldGeneration, nullValue(GCStatistic.class))
        assertThat(event.stats.permanentGeneration, nullValue(GCStatistic.class))
        assertThat(event.userTiming, equalTo(123L))
        assertThat(event.sysTiming, equalTo(321L))
        assertThat(event.realTiming, equalTo(444L))
        assertThat(event.completeEventTimeInMicroSeconds, equalTo(3438L))
        assertThat(event.survivorDetails.newThreshold, equalTo(4))
        assertThat(event.survivorDetails.maxThreshold, equalTo(15))
        assertThat(event.survivorDetails.desiredSize, equalTo(425984L))
        assertThat(event.survivorDetails.endingTotalSize, equalTo(717072L))
    }

}
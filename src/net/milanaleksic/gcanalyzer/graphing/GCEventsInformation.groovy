package net.milanaleksic.gcanalyzer.graphing

import java.text.SimpleDateFormat
import net.milanaleksic.gcanalyzer.Utils
import org.jfree.chart.ChartFactory
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.DateAxis
import org.jfree.chart.plot.XYPlot
import net.milanaleksic.gcanalyzer.parser.*
import org.jfree.data.time.*
import java.awt.Paint
import java.awt.Color
import org.jfree.chart.axis.ValueAxis

/**
 * User: Milan Aleksic
 * Date: 1/29/12
 * Time: 11:27 AM
 */
class GCEventsInformation {

    private def GCEvents events

    private String title

    GCEventsInformation(URL url) {
        title = url.getPath()
        events = new GCLogParser().parse(url)
    }

    GCEventsInformation(String fileName) {
        File target = new File(fileName)
        title = target.name
        events = new GCLogParser().parse(target)
    }

    JFreeChart[] getChartsForCategory(GCEventCategory category) {
        switch (category) {
            case GCEventCategory.HEAP_CALCULATION:
                return [
                        getLiveSizeChart(),
                        getPermGenSizeChart(),
                        getOldGenerationMemoryOccupancy(),
                        getOldGenerationIncreaseChart(),
                        getPermGenerationMaxChart()
                    ]
            case GCEventCategory.MEMORY_OCCUPANCY:
                return [
                        getYoungGenerationChart(),
                        getLiveSizeChart(),
                        getPermGenerationChart()
                ]
            case GCEventCategory.MEMORY_MAX_SIZE:
                return [
                        getHeapWithoutPermanentGenerationGCChart(),
                        getYoungGenerationMaxChart(),
                        getOldGenerationMaxChart(),
                        getPermanentGenerationMaxChart()
                ]
            case GCEventCategory.TIME_SPENT_PER_HOUR:
                return [
                        getTimeSpentOnAllGC(),
                        getTimeSpentOnYoungGC(),
                        getTimeSpentOnFullGC()
                ]
            case GCEventCategory.EVENTS_PER_HOUR:
                return [
                        getFrequencyPerHourOnAllGC(),
                        getFrequencyPerHourOnYoungGC(),
                        getFrequencyPerHourOnFullGC()
                ]
            case GCEventCategory.INDIVIDUAL_EVENT_TIMING:
                return [
                        getEventTimingsChart(),
                        getYoungGCEventTimingsChart(),
                        getFullGCEventTimingsChart()
                ]
        }
    }

    JFreeChart getYoungGenerationChart() {
        return getTimeChartBasedOnIndependentEvents('Young generation memory after GC events', 'Memory (KB)') {
            GCEvent event ->
                return event.stats['PSYoungGen'].endValueInB / 1024
        }
    }

    JFreeChart getOldGenerationIncreaseChart() {
        return getTimeChartBasedOnTwoConsecutiveEvents('Old generation increase per event', 'Memory (KB)') {
            GCEvent previousEvent, GCEvent event ->
                def previousOldGenerationSize = (previousEvent.stats[null].endValueInB - previousEvent.stats['PSYoungGen'].endValueInB)
                def currentOldGenerationSize = (event.stats[null].endValueInB - event.stats['PSYoungGen'].endValueInB)
                return (currentOldGenerationSize - previousOldGenerationSize) / 1024
        }
    }

    JFreeChart getOldGenerationMemoryOccupancy() {
        return getTimeChartBasedOnIndependentEvents('Old generation memory occupancy after GC events', '%') {
            GCEvent event ->
                if (!event.fullGarbageCollection)
                    return null
                return event.stats['ParOldGen'].endValueInB * 100 / event.stats['ParOldGen'].maxValueInB
        }
    }

    JFreeChart getPermGenerationChart() {
        return getTimeChartBasedOnIndependentEvents('Permanent generation memory occupancy after GC events', 'Memory (KB)') {
            GCEvent event ->
                if (!event.fullGarbageCollection)
                    return null
                return event.stats['PSPermGen'].endValueInB / 1024
        }
    }

    JFreeChart getPermGenerationMaxChart() {
        return getTimeChartBasedOnIndependentEvents('Permanent generation memory occupancy after GC events', '%') {
            GCEvent event ->
                if (!event.fullGarbageCollection)
                    return null
                return event.stats['PSPermGen'].endValueInB * 100 / event.stats['PSPermGen'].maxValueInB
        }
    }

    JFreeChart getLiveSizeChart() {
        return getTimeChartBasedOnIndependentEvents('Memory live size (Old gen memory occupancy after Full GCs)', 'Memory (KB)') {
            GCEvent event ->
                if (!event.fullGarbageCollection)
                    return null
                return event.stats['ParOldGen'].endValueInB / 1024
        }
    }

    JFreeChart getPermGenSizeChart() {
        return getTimeChartBasedOnIndependentEvents('Permanent generation size after Full GC', 'Memory (KB)') {
            GCEvent event ->
                if (!event.fullGarbageCollection)
                    return null
                return event.stats['PSPermGen'].endValueInB / 1024
        }
    }

    JFreeChart getEventTimingsChart() {
        return getTimeChartBasedOnIndependentEvents('Complete event time', 'Milliseconds (ms)') {
            GCEvent event ->
                return event.completeEventTimeInMicroSeconds / 1000
        }
    }

    JFreeChart getYoungGCEventTimingsChart() {
        return getTimeChartBasedOnIndependentEvents('Only Young Generation GC event time', 'Milliseconds (ms)') {
            GCEvent event ->
                return event.fullGarbageCollection ? null : event.completeEventTimeInMicroSeconds / 1000
        }
    }

    JFreeChart getFullGCEventTimingsChart() {
        return getTimeChartBasedOnIndependentEvents('Full Generation GC event time', 'Milliseconds (ms)') {
            GCEvent event ->
                return event.fullGarbageCollection ? event.completeEventTimeInMicroSeconds / 1000 : null
        }
    }

    JFreeChart getHeapWithoutPermanentGenerationGCChart() {
        return getTimeChartBasedOnIndependentEvents('Heap without Permanent generation', 'Memory (KB)') {
            GCEvent event ->
                return event.stats[null].maxValueInB / 1024
        }
    }

    JFreeChart getYoungGenerationMaxChart() {
        return getTimeChartBasedOnIndependentEvents('Young generation Max Size', 'Memory (KB)') {
            GCEvent event ->
                return event.stats['PSYoungGen'].maxValueInB / 1024
        }
    }

    JFreeChart getOldGenerationMaxChart() {
        return getTimeChartBasedOnIndependentEvents('Old generation Max Size', 'Memory (KB)') { GCEvent event ->
            SingleGCStatistic value = event.stats['ParOldGen']
            return value ? value.maxValueInB / 1024 : null
        }
    }

    JFreeChart getPermanentGenerationMaxChart() {
        return getTimeChartBasedOnIndependentEvents('Permanent generation Max Size', 'Memory (KB)') { GCEvent event ->
            SingleGCStatistic value = event.stats['PSPermGen']
            return value ? value.maxValueInB / 1024 : null
        }
    }

    JFreeChart getTimeSpentOnAllGC() {
        return getTimeChartBasedOnPerHour('Time spent on all GC', 'ms per hour') { GCEvent event,  Number previousValue->
            BigDecimal valueToAdd = event.completeEventTimeInMicroSeconds / 1000
            return previousValue ? previousValue+valueToAdd : valueToAdd
        }
    }

    JFreeChart getTimeSpentOnYoungGC() {
        return getTimeChartBasedOnPerHour('Time spent on Young GC', 'ms per hour') { GCEvent event,  Number previousValue->
            if (event.fullGarbageCollection)
                return previousValue
            else {
                BigDecimal valueToAdd = event.completeEventTimeInMicroSeconds / 1000
                return previousValue ? previousValue+valueToAdd : valueToAdd
            }
        }
    }

    JFreeChart getTimeSpentOnFullGC() {
        return getTimeChartBasedOnPerHour('Time spent on Full GC', 'ms per hour') { GCEvent event,  Number previousValue->
            if (event.fullGarbageCollection) {
                BigDecimal valueToAdd = event.completeEventTimeInMicroSeconds / 1000
                return previousValue ? previousValue+valueToAdd : valueToAdd
            }
            else
                return previousValue
        }
    }

    JFreeChart getFrequencyPerHourOnAllGC() {
        return getTimeChartBasedOnPerHour('Events per hour for all GC', 'times per hour') { GCEvent event,  Number previousValue->
            return previousValue ? previousValue+1 : 1
        }
    }

    JFreeChart getFrequencyPerHourOnYoungGC() {
        return getTimeChartBasedOnPerHour('Events per hour on Young GC', 'times per hour') { GCEvent event,  Number previousValue->
            if (event.fullGarbageCollection)
                return previousValue
            else
                return previousValue ? previousValue+1 : 1
        }
    }

    JFreeChart getFrequencyPerHourOnFullGC() {
        return getTimeChartBasedOnPerHour('Events per hour on Full GC', 'times per hour') { GCEvent event,  Number previousValue->
            if (event.fullGarbageCollection)
                return previousValue ? previousValue+1 : 1
            else
                return previousValue
        }
    }

    private JFreeChart getTimeChartBasedOnPerHour(String graphName, String yAxisName, Closure process) {
        return getTimeChart(graphName, yAxisName) { TimeSeries series ->
            if (!events.hashMapOnDate || events.hashMapOnDate.size()==0)
                return
            HashMap<Date, Number> timesPerHour = new LinkedHashMap<Date, Number>()
            Date minTime = null, maxTime = null
            events.hashMapOnDate.each { Date date, GCEvent event ->
                Date roundTime = Utils.roundToHour(date)
                if (!minTime || roundTime.before(minTime))
                    minTime = roundTime
                if (!maxTime || roundTime.after(maxTime))
                    maxTime = roundTime
                timesPerHour[roundTime] = process(event, timesPerHour[roundTime])
            }
            Calendar iterator = Calendar.getInstance()
            iterator.setTime(minTime)
            while (iterator.getTime().before(maxTime)) {
                Date iteratorTime = iterator.getTime()
                Number timeSpent = timesPerHour[iteratorTime]
                if (!timeSpent) {
                    timeSpent = 0
                }
                series.add(new Millisecond(iteratorTime), timeSpent)
                iterator.add(Calendar.HOUR, 1)
            }
        }
    }

    private JFreeChart getTimeChartBasedOnIndependentEvents(String graphName, String yAxisName, Closure process) {
        return getTimeChart(graphName, yAxisName) { TimeSeries series ->
            events.hashMapOnDate.each { Date date, GCEvent event ->
                Number value = (Number) process(event)
                if (value)
                    series.add(new Millisecond(date), value)
            }
        }
    }

    private JFreeChart getTimeChart(String graphName, String yAxisName, Closure process) {
        TimeSeriesCollection dataSet = new TimeSeriesCollection()
        TimeSeries series = new TimeSeries("Time",  Millisecond.class)

        process(series)

        dataSet.addSeries(series)
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
            graphName, 'Time', yAxisName, dataSet, true, true, false)
        XYPlot plot = (XYPlot) chart.getPlot()
        plot.backgroundPaint = Color.WHITE
        plot.renderer.setSeriesPaint(0, Color.BLUE)
        plot.setDomainGridlinePaint(Color.GRAY)
        plot.setRangeGridlinePaint(Color.GRAY)

        def axis = (DateAxis) plot.getDomainAxis()
        axis.setDateFormatOverride(new SimpleDateFormat("dd/MM HH:mm"))
        axis.setTickMarkPaint(Color.GRAY)
        return chart
    }

    private JFreeChart getTimeChartBasedOnTwoConsecutiveEvents(String graphName, String yAxisName, Closure process) {
        return getTimeChart(graphName, yAxisName) { TimeSeries series ->
            def previousDate = null
            def previousEvent = null
            events.hashMapOnDate.each { Date date, GCEvent event ->
                if (previousEvent && previousDate) {
                    Number value = (Number) process(previousEvent, event)
                    if (value)
                        series.add(new Millisecond(date), value)
                }
                previousDate = date
                previousEvent = event
            }
        }
    }

    public String getTitle() {
        return title
    }
}

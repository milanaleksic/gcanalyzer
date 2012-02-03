package net.milanaleksic.gcanalyzer.graphing

import java.text.SimpleDateFormat
import net.milanaleksic.gcanalyzer.Utils
import org.jfree.chart.ChartFactory
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.DateAxis
import org.jfree.chart.plot.XYPlot
import net.milanaleksic.gcanalyzer.parser.*
import org.jfree.data.time.*

/**
 * User: Milan Aleksic
 * Date: 1/29/12
 * Time: 11:27 AM
 */
class GCEventsInformation {

    private def GCEvents events

    GCEventsInformation(String fileName) {
        events = new GCLogParser().parse(new File(fileName))
    }

    JFreeChart[] getChartsForCategory(GCEventCategory category) {
        switch (category) {
            case GCEventCategory.HEAP_CALCULATION:
                return [
                        getLiveSizeChart(),
                        getPermGenSizeChart()
                    ]
            case GCEventCategory.MEMORY_MAX_SIZE:
                return [
                        getHeapWithoutPermanentGenerationGCChart(),
                        getYoungGenerationChart(),
                        getOldGenerationChart(),
                        getPermanentGenerationChart()
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

    JFreeChart getLiveSizeChart() {
        return getTimeChartBasedOnIndependentEvents('Memory live size', 'Memory (KB)') {
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

    JFreeChart getYoungGenerationChart() {
        return getTimeChartBasedOnIndependentEvents('Young generation Max Size', 'Memory (KB)') {
            GCEvent event ->
                return event.stats['PSYoungGen'].maxValueInB / 1024
        }
    }

    JFreeChart getOldGenerationChart() {
        return getTimeChartBasedOnIndependentEvents('Old generation Max Size', 'Memory (KB)') { GCEvent event ->
            SingleGCStatistic value = event.stats['ParOldGen']
            return value ? value.maxValueInB / 1024 : null
        }
    }

    JFreeChart getPermanentGenerationChart() {
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
            graphName, 'Time', yAxisName, dataSet, true, true, false);
        XYPlot plot = (XYPlot) chart.getPlot()

        def axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("dd/MM HH:mm"));
        return chart
    }

}

package net.milanaleksic.gcanalyzer.graphing

import java.awt.Color
import java.text.SimpleDateFormat
import net.milanaleksic.gcanalyzer.util.Utils
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
class GCLogInformationSource {

    private def GCEvents events

    private String title

    private String location

    private long parsingTime

    private long chartCreationTime

    private static final GCLogParser parser = new GCLogParser()

    GCLogInformationSource(URL url) {
        location = url.toString()
        title = url.getPath()
        long begin = System.currentTimeMillis()
        events = parser.parse(url)
        parsingTime = System.currentTimeMillis() - begin
    }

    GCLogInformationSource(String fileName) {
        location = fileName
        File target = new File(fileName)
        title = target.name
        long begin = System.currentTimeMillis()
        events = parser.parse(target)
        parsingTime = System.currentTimeMillis() - begin
    }

    GCLogInformationSource(GCEvents events, String location, String title) {
        this.events = events
        this.location = location
        this.title = title
        this.parsingTime = 0
    }

    long getParsingTime() {
        parsingTime
    }

    String getTitle() {
        title
    }

    String getLocation() {
        location
    }

    JFreeChart[] getChartsForCategory(GCEventCategory category) {
        switch (category) {
            case GCEventCategory.HEAP_CALCULATION:
                return [
                        getLiveSizeChart(),
                        getPermGenSizeChart(),
                        getOldGenerationIncreaseChart(),
                        getOldGenerationMemoryOccupancyChart(),
                        getPermGenerationMemoryOccupancyChart()
                ]
            case GCEventCategory.MEMORY_OCCUPANCY:
                return [
                        getYoungGenerationCompleteChart(),
                        getOldGenerationCompleteChart(),
                        getPermanentGenerationCompleteChart(),
                        getHeapWithoutPermanentGenerationGCChart()
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
            case GCEventCategory.SURVIVOR_DETAILS:
                return [
                        getSurvivorPoolNewAndMaxThresholdChart(),
                        getSurvivorSizeChart()
                ]
        }
    }

    private JFreeChart getOldGenerationCompleteChart() {
        getTimeChartBasedOnIndependentEvents('Old generation memory occupancy', 'Memory (KB)', [
                'Start memory occupancy': { GCEvent event ->
                    if (!event.fullGarbageCollection)
                        return null
                    event.stats['ParOldGen'].startValueInB / 1024
                },
                'End memory occupancy': { GCEvent event ->
                    if (!event.fullGarbageCollection)
                        return null
                    event.stats['ParOldGen'].endValueInB / 1024
                },
                'Max memory occupancy': { GCEvent event ->
                    if (!event.fullGarbageCollection)
                        return null
                    event.stats['ParOldGen'].maxValueInB / 1024
                }
        ] as Map<String, Closure>)
    }

    private JFreeChart getPermanentGenerationCompleteChart() {
        getTimeChartBasedOnIndependentEvents('Permanent generation memory occupancy', 'Memory (KB)', [
                'Start memory occupancy': { GCEvent event ->
                    if (!event.fullGarbageCollection)
                        return null
                    event.stats['PSPermGen'].startValueInB / 1024
                },
                'End memory occupancy': { GCEvent event ->
                    if (!event.fullGarbageCollection)
                        return null
                    event.stats['PSPermGen'].endValueInB / 1024
                },
                'Max memory occupancy': { GCEvent event ->
                    if (!event.fullGarbageCollection)
                        return null
                    event.stats['PSPermGen'].maxValueInB / 1024
                }
        ] as Map<String, Closure>)
    }

    private JFreeChart getYoungGenerationCompleteChart() {
        getTimeChartBasedOnIndependentEvents('Young generation memory occupancy', 'Memory (KB)', [
                'Start memory occupancy': { GCEvent event ->
                    event.stats['PSYoungGen'].startValueInB / 1024
                },
                'End memory occupancy': { GCEvent event ->
                    event.stats['PSYoungGen'].endValueInB / 1024
                },
                'Max memory occupancy': { GCEvent event ->
                    event.stats['PSYoungGen'].maxValueInB / 1024
                }
        ] as Map<String, Closure>)
    }

    private JFreeChart getSurvivorPoolNewAndMaxThresholdChart() {
        getTimeChartBasedOnIndependentEvents('New and Max threshold', 'Threshold size', [
                'New Threshold': { GCEvent event ->
                    GCSurvivorDetails details = event.survivorDetails
                    details ? details.newThreshold : null
                },
                'Max Threshold': { GCEvent event ->
                    GCSurvivorDetails details = event.survivorDetails
                    details ? details.maxThreshold : null
                }
        ] as Map<String, Closure>)
    }


    private JFreeChart getSurvivorSizeChart() {
        getTimeChartBasedOnIndependentEvents('Survivor pool size', 'Memory (KB)', [
                'Desired size': { GCEvent event ->
                    GCSurvivorDetails details = event.survivorDetails
                    details ? details.desiredSize / 1024 : null
                },
                'Ending total size': { GCEvent event ->
                    GCSurvivorDetails details = event.survivorDetails
                    details ? details.endingTotalSize ? details.endingTotalSize / 1024 : null : null
                }
        ] as Map<String, Closure>)
    }

    private JFreeChart getOldGenerationIncreaseChart() {
        getTimeChartBasedOnTwoConsecutiveEvents('Old generation increase per event', 'Memory (KB)') {
            GCEvent previousEvent, GCEvent event ->
            def previousOldGenerationSize = (previousEvent.stats[null].endValueInB - previousEvent.stats['PSYoungGen'].endValueInB)
            def currentOldGenerationSize = (event.stats[null].endValueInB - event.stats['PSYoungGen'].endValueInB)
            (currentOldGenerationSize - previousOldGenerationSize) / 1024
        }
    }

    private JFreeChart getOldGenerationMemoryOccupancyChart() {
        getTimeChartBasedOnIndependentEvents('Old generation memory occupancy in %', '%') {
            GCEvent event ->
            if (!event.fullGarbageCollection)
                return null
            event.stats['ParOldGen'].endValueInB * 100 / event.stats['ParOldGen'].maxValueInB
        }
    }

    private JFreeChart getPermGenerationMemoryOccupancyChart() {
        getTimeChartBasedOnIndependentEvents('Permanent generation memory occupancy in %', '%') {
            GCEvent event ->
            if (!event.fullGarbageCollection)
                return null
            event.stats['PSPermGen'].endValueInB * 100 / event.stats['PSPermGen'].maxValueInB
        }
    }

    private JFreeChart getLiveSizeChart() {
        getTimeChartBasedOnIndependentEvents('Memory live size (Old gen memory occupancy after Full GCs)', 'Memory (KB)') {
            GCEvent event ->
            if (!event.fullGarbageCollection)
                return null
            event.stats['ParOldGen'].endValueInB / 1024
        }
    }

    private JFreeChart getPermGenSizeChart() {
        getTimeChartBasedOnIndependentEvents('Permanent generation size after Full GC', 'Memory (KB)') {
            GCEvent event ->
            if (!event.fullGarbageCollection)
                return null
            event.stats['PSPermGen'].endValueInB / 1024
        }
    }

    private JFreeChart getEventTimingsChart() {
        getTimeChartBasedOnIndependentEvents('Complete event time', 'Milliseconds (ms)') {
            GCEvent event ->
            event.completeEventTimeInMicroSeconds / 1000
        }
    }

    private JFreeChart getYoungGCEventTimingsChart() {
        getTimeChartBasedOnIndependentEvents('Only Young Generation GC event time', 'Milliseconds (ms)') {
            GCEvent event ->
            event.fullGarbageCollection ? null : event.completeEventTimeInMicroSeconds / 1000
        }
    }

    private JFreeChart getFullGCEventTimingsChart() {
        getTimeChartBasedOnIndependentEvents('Full Generation GC event time', 'Milliseconds (ms)') {
            GCEvent event ->
            event.fullGarbageCollection ? event.completeEventTimeInMicroSeconds / 1000 : null
        }
    }

    private JFreeChart getHeapWithoutPermanentGenerationGCChart() {
        getTimeChartBasedOnIndependentEvents('Heap without Permanent generation', 'Memory (KB)') {
            GCEvent event ->
            event.stats[null].maxValueInB / 1024
        }
    }

    private JFreeChart getTimeSpentOnAllGC() {
        getTimeChartBasedOnPerHour('Time spent on all GC', 'ms per hour') { GCEvent event, Number previousValue ->
            BigDecimal valueToAdd = event.completeEventTimeInMicroSeconds / 1000
            previousValue ? previousValue + valueToAdd : valueToAdd
        }
    }

    private JFreeChart getTimeSpentOnYoungGC() {
        getTimeChartBasedOnPerHour('Time spent on Young GC', 'ms per hour') { GCEvent event, Number previousValue ->
            if (event.fullGarbageCollection)
                previousValue
            else {
                BigDecimal valueToAdd = event.completeEventTimeInMicroSeconds / 1000
                previousValue ? previousValue + valueToAdd : valueToAdd
            }
        }
    }

    private JFreeChart getTimeSpentOnFullGC() {
        getTimeChartBasedOnPerHour('Time spent on Full GC', 'ms per hour') { GCEvent event, Number previousValue ->
            if (event.fullGarbageCollection) {
                BigDecimal valueToAdd = event.completeEventTimeInMicroSeconds / 1000
                previousValue ? previousValue + valueToAdd : valueToAdd
            }
            else
                previousValue
        }
    }

    private JFreeChart getFrequencyPerHourOnAllGC() {
        getTimeChartBasedOnPerHour('Events per hour by all GCs', 'times per hour') { GCEvent event, Number previousValue ->
            previousValue ? previousValue + 1 : 1
        }
    }

    private JFreeChart getFrequencyPerHourOnYoungGC() {
        getTimeChartBasedOnPerHour('Events per hour by Young GC', 'times per hour') { GCEvent event, Number previousValue ->
            if (event.fullGarbageCollection)
                previousValue
            else
                previousValue ? previousValue + 1 : 1
        }
    }

    private JFreeChart getFrequencyPerHourOnFullGC() {
        getTimeChartBasedOnPerHour('Events per hour by Full GC', 'times per hour') { GCEvent event, Number previousValue ->
            if (event.fullGarbageCollection)
                previousValue ? previousValue + 1 : 1
            else
                previousValue
        }
    }

    private JFreeChart getTimeChartBasedOnPerHour(String graphName, String yAxisName, Closure process) {
        getTimeChart(graphName, yAxisName) { TimeSeries series ->
            if (!events.hashMapOnDate || events.hashMapOnDate.size() == 0)
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
        getTimeChart(graphName, yAxisName) { TimeSeries series ->
            events.hashMapOnDate.each { Date date, GCEvent event ->
                Number value = process(event)
                if (value)
                    series.add(new Millisecond(date), value)
            }
        }
    }

    private JFreeChart getTimeChartBasedOnIndependentEvents(String graphName, String yAxisName, Map<String, Closure> seriesToClosureMapping) {
        getTimeChart(graphName, yAxisName, seriesToClosureMapping) { String seriesName, TimeSeries series ->
            events.hashMapOnDate.each { Date date, GCEvent event ->
                Number value = (Number) seriesToClosureMapping[seriesName](event)
                if (value)
                    series.add(new Millisecond(date), value)
            }
        }
    }

    private JFreeChart getTimeChart(String graphName, String yAxisName, Closure process) {
        TimeSeriesCollection dataSet = new TimeSeriesCollection()

        TimeSeries series = new TimeSeries(graphName, Millisecond.class)
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

    private JFreeChart getTimeChart(String graphName, String yAxisName, Map<String, Closure> seriesToClosureMapping, Closure process) {
        TimeSeriesCollection dataSet = new TimeSeriesCollection()

        def seriesMap = [:]
        seriesToClosureMapping.keySet().each { String seriesName ->
            seriesMap[seriesName] = new TimeSeries(seriesName, Millisecond.class)
        }

        seriesMap.each { String seriesName, TimeSeries series ->
            process(seriesName, series)
            dataSet.addSeries(series)
        }

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                graphName, 'Time', yAxisName, dataSet, true, true, false)
        XYPlot plot = (XYPlot) chart.getPlot()
        plot.backgroundPaint = Color.WHITE
        plot.renderer.setSeriesPaint(0, Color.BLUE)
        plot.renderer.setSeriesPaint(1, Color.BLACK)
        plot.renderer.setSeriesPaint(2, Color.RED)
        plot.setDomainGridlinePaint(Color.GRAY)
        plot.setRangeGridlinePaint(Color.GRAY)

        def axis = (DateAxis) plot.getDomainAxis()
        axis.setDateFormatOverride(new SimpleDateFormat("dd/MM HH:mm"))
        axis.setTickMarkPaint(Color.GRAY)
        return chart
    }

    private JFreeChart getTimeChartBasedOnTwoConsecutiveEvents(String graphName, String yAxisName, Closure process) {
        getTimeChart(graphName, yAxisName) { TimeSeries series ->
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

    private Map<String, Number> cachedStatistics = [:]

    private synchronized Number calculateOrUseCachedStatistics(String statisticName, Closure process) {
        Number ofTheJedi = cachedStatistics[statisticName]
        if (ofTheJedi)
            ofTheJedi
        Number statisticValue = process(ofTheJedi)
        cachedStatistics[statisticName] = statisticValue
        statisticValue
    }

    public int numberOfDetectedYoungGCEvents() {
        calculateOrUseCachedStatistics('numberOfDetectedYoungGCEvents') {
            int ofTheJedi = 0
            events.linkedList.each { GCEvent event ->
                if (!event.fullGarbageCollection)
                    ofTheJedi++
            }
            ofTheJedi
        }
    }

    public int numberOfDetectedFullGCEvents() {
        calculateOrUseCachedStatistics('numberOfDetectedFullGCEvents') {
            double ofTheJedi = 0
            events.linkedList.each { GCEvent event ->
                if (event.fullGarbageCollection)
                    ofTheJedi++
            }
            ofTheJedi
        }
    }

    private double sumYoungGCEventLengthMicroSeconds() {
        calculateOrUseCachedStatistics('sumYoungGCEventLengthMicroSeconds') {
            double sum = 0
            events.linkedList.each { GCEvent event ->
                if (!event.fullGarbageCollection) {
                    sum += event.completeEventTimeInMicroSeconds
                }
            }
            return sum
        }
    }

    private double sumFullGCEventLengthMicroSeconds() {
        calculateOrUseCachedStatistics('sumFullGCEventLengthMicroSeconds') {
            long sum = 0
            events.hashMapOnMillis.values().each { GCEvent event ->
                if (event.fullGarbageCollection) {
                    sum += event.completeEventTimeInMicroSeconds
                }
            }
            return sum
        }
    }

    public double averageYoungGCEventLength() {
        calculateOrUseCachedStatistics('averageYoungGCEventLength') {
            sumYoungGCEventLengthMicroSeconds() / numberOfDetectedYoungGCEvents() / 1000
        }
    }

    public double averageFullGCEventLength() {
        calculateOrUseCachedStatistics('averageFullGCEventLength') {
            sumFullGCEventLengthMicroSeconds() / numberOfDetectedFullGCEvents() / 1000
        }
    }

    public double standardDeviationYoungGCEventLength() {
        calculateOrUseCachedStatistics('standardDeviationYoungGCEventLength') {
            double sum = 0
            double averageMicrosecondsForYoungGC = sumYoungGCEventLengthMicroSeconds() / numberOfDetectedYoungGCEvents()
            events.linkedList.each { GCEvent event ->
                if (!event.fullGarbageCollection) {
                    sum += Math.pow(event.completeEventTimeInMicroSeconds - averageMicrosecondsForYoungGC, 2)
                }
            }
            return Math.sqrt((double) sum / (numberOfDetectedYoungGCEvents() - 1)) / 1000
        }
    }

    public double standardDeviationFullGCEventLength() {
        calculateOrUseCachedStatistics('standardDeviationFullGCEventLength') {
            double sum = 0
            double averageMicrosecondsForFullGC = sumFullGCEventLengthMicroSeconds() / numberOfDetectedFullGCEvents()
            events.linkedList.each { GCEvent event ->
                if (event.fullGarbageCollection) {
                    sum += Math.pow(event.completeEventTimeInMicroSeconds - averageMicrosecondsForFullGC, 2)
                }
            }
            return Math.sqrt((double) sum / (numberOfDetectedFullGCEvents() - 1)) / 1000
        }
    }

    public double totalTimeSpentOnYoungGCInPercent() {
        calculateOrUseCachedStatistics('totalTimeSpentOnYoungGCInPercent') {
            if (events.size() < 2)
                return 0
            double gcTime = sumYoungGCEventLengthMicroSeconds() / 1000
            double totalTime = events.linkedList.last().momentInMillis - events.linkedList.first().momentInMillis
            return (double) 100 * gcTime / totalTime
        }
    }

    public double totalTimeSpentOnFullGCInPercent() {
        calculateOrUseCachedStatistics('totalTimeSpentOnFullGCInPercent') {
            if (events.size() < 2)
                return 0
            double gcTime = sumFullGCEventLengthMicroSeconds() / 1000
            double totalTime = events.linkedList.last().momentInMillis - events.linkedList.first().momentInMillis
            return (double) 100 * gcTime / totalTime
        }
    }

    public double totalTimeSpentOnGCInPercent() {
        totalTimeSpentOnYoungGCInPercent() + totalTimeSpentOnFullGCInPercent()
    }

    public long totalTimeSpentOnYoungGC() {
        sumYoungGCEventLengthMicroSeconds() / 1000
    }

    public long totalTimeSpentOnFullGC() {
        sumFullGCEventLengthMicroSeconds() / 1000
    }

    public long totalTimeSpentOnGC() {
        totalTimeSpentOnYoungGC() + totalTimeSpentOnFullGC()
    }
}

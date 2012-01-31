package net.milanaleksic.gcanalyzer

import java.text.SimpleDateFormat
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

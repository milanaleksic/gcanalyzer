package net.milanaleksic.gcanalyzer

import java.text.SimpleDateFormat
import org.jfree.chart.ChartFactory
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.DateAxis
import org.jfree.chart.plot.XYPlot
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

    JFreeChart getEventTimingsChart(boolean b) {
        return getTimeChartBasedOnClosure('Complete event time', 'Microseconds (us)') {
            GCEvent event ->
                return event.completeEventTimeInMicroSeconds
        }
    }

    JFreeChart getNonPerGenGCChart(boolean non) {
        return getTimeChartBasedOnClosure('Non-PermGen GC log', 'Memory (KB)') {
            GCEvent event ->
                return event.stats[null].maxValueInB / 1024
        }
    }

    private JFreeChart getTimeChartBasedOnClosure(String graphName, String yAxisName, Closure process) {
        TimeSeriesCollection dataSet = new TimeSeriesCollection()
        TimeSeries series = new TimeSeries("Time",  Millisecond.class)

        events.hashMapOnDate.each { Date date, GCEvent event ->
            series.add(new Millisecond(date), (Number) process(event))
        }

        dataSet.addSeries(series)
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
            graphName, 'Time', yAxisName, dataSet, true, true, false);
        XYPlot plot = (XYPlot) chart.getPlot()

        def axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("dd/MM HH:mm"));
        return chart
    }

}

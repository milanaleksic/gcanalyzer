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

    JFreeChart getNonPerGenGCChart(boolean non) {
        TimeSeriesCollection dataset = new TimeSeriesCollection()
        TimeSeries series = new TimeSeries("Time",  Millisecond.class)

        events.hashMapOnDate.each { Date date, GCEvent event ->
            series.add(new Millisecond(date), event.stats[null].maxValueInB)
        }

        dataset.addSeries(series)
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
            "non-PermGen GC log", 'Time', 'Memory (K)', dataset, true, true, false);
        XYPlot plot = (XYPlot) chart.getPlot()

        def axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("dd/MM HH:mm"));

        return chart
    }

    JFreeChart getEventTimingsChart(boolean b) {
        TimeSeriesCollection dataset = new TimeSeriesCollection()
        TimeSeries series = new TimeSeries("Time",  Millisecond.class)

        events.hashMapOnDate.each { Date date, GCEvent event ->
            series.add(new Millisecond(date), event.completeEventTimeInMicroSeconds)
        }

        dataset.addSeries(series)
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
            "Complete event time", 'Time', 'Microseconds (us)', dataset, true, true, false);
        XYPlot plot = (XYPlot) chart.getPlot()

        def axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("dd/MM HH:mm"));

        return chart
    }
}

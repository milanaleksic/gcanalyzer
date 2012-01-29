package net.milanaleksic.gcanalyzer

import java.text.SimpleDateFormat
import java.util.regex.Pattern
import javax.swing.JFrame
import javax.swing.JTabbedPane
import org.jfree.chart.axis.DateAxis
import org.jfree.chart.plot.CategoryMarker
import org.jfree.chart.plot.CategoryPlot
import java.awt.*
import org.jfree.chart.*
import org.jfree.data.time.*
import org.jfree.ui.*

/**
 * User: Milan Aleksic
 * Date: 1/29/12
 * Time: 11:09 AM
 */
class GCAnalyzer {

    public static void main(String[] args) {
        if (args.length == 0) {
            println "Script should be used by sending a GC output log as parameter"
            return
        }

        new GCAnalyzer().exec(args[0])
    }

    public def exec(String file) {
        def text = new File(file).text
        TimeSeries series = new TimeSeries("Time",  Millisecond.class);
        text.eachLine { line ->
            processLine(line, series)
        }

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(series)
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
            "non-PermGen GC log", 'Time', 'Memory (K)', dataset, true, true, false);
        CategoryPlot localCategoryPlot = (CategoryPlot) chart.getPlot()

        CategoryMarker localCategoryMarker = new CategoryMarker("Category 4", Color.blue, new BasicStroke(1.0F));
        localCategoryMarker.setDrawAsLine(true);
        localCategoryMarker.setLabel("Marker Label");
        localCategoryMarker.setLabelFont(new Font("Dialog", 0, 11));
        localCategoryMarker.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
        localCategoryMarker.setLabelOffset(new RectangleInsets(2.0D, 5.0D, 2.0D, 5.0D));
        localCategoryPlot.addDomainMarker(localCategoryMarker, Layer.BACKGROUND);

        def axis = (DateAxis) localCategoryPlot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("dd/MM HH:mm"));

        JFrame frame = new JFrame("Garbage Collector Log analysis");

        JTabbedPane tabs = new JTabbedPane();
        tabs.add('non-PermGen GC log', new ChartPanel(chart))
        frame.add(tabs);
        frame.pack();
        frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }

    def private final lineRegEx = Pattern.compile(
 /(\d{4})-(\d{2})-(\d{2})T/ +        // 1, 2, 3 - date parts
 /(\d{2}):(\d{2}):(\d{2})\.(\d{3})/+ // 4, 5, 6, 7 - time parts
 /\+\d{4}:\s/ +                      // [noise]
 /(\d+\.\d+):\s/ +                   // 8 - time in seconds from program start
 /\[([^\[]+)\s/ +                    // 9 - Garbage collection event
 /(/ +                               // 10 - [helper group, don't use]
    /(\[/ +                          // 11 - [helper group, don't use]
        /(/ +                        // 12 - [helper group, don't use]
			/(\w+)\:\s/ +            // 13 - GC name
			/([\d]+)K->/ +           // 14 - GC start value
			/([\d]+)K\(/ +           // 15 - GC end value
			/([\d]+)K\)/ +           // 16 - memory segment Max size
		/)+/ +
    /\]\s?)/ +
	/|/ +
 /(/ +                               // 17 - [helper group, don't use]
    /([\d]+)K->/ +                   // 18 - non-PermGen start value
	/([\d]+)K\(/ +                   // 19 - non-PermGen end value
	/([\d]+)K\)\s?/ +                // 20 - non-PermGen Max size
 /))+/ +
 /,\s([\d\.]+)\ssecs\]/ +            // 21 - total garbage collection event time
 /(\s\[Times:\s/ +                   // 22 - [helper group, don't use]
	/(/ +                            // 23 - helper group, don't use
		/(\w+)=/ +                   // 24 - timing name (user/sys/real)
		/([\d\.]+)/ +                // 25 - timing value (user/sys/real)
	/,?\s)+/ +
 /secs\]\s?)?/)

    private void processLine(line, TimeSeries dataset) {
        def matcher = (line =~ lineRegEx)
        if (matcher.find()) {
            def calendar = Calendar.getInstance()
            calendar.set(matcher.group(1) as int, (matcher.group(2) as int) - 1, matcher.group(3) as int,
                matcher.group(4) as int, matcher.group(5) as int, matcher.group(6) as int)
            calendar.set(Calendar.MILLISECOND, matcher.group(7) as int)
            float timeFromStartAsFloat = matcher.group(8) as float
            int millisFromStart = Math.round(timeFromStartAsFloat * 1000)

            dataset.add(new Millisecond(calendar.getTime()), Double.parseDouble(matcher.group(20)))
            dataset.get
        } else {
            throw new IllegalArgumentException("Not matched garbage collection log line: $line")
        }
    }

}

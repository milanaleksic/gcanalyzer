package net.milanaleksic.gcanalyzer

import javax.swing.JFrame
import javax.swing.JTabbedPane
import org.jfree.chart.ChartPanel

/**
 * User: Milan Aleksic
 * Date: 1/29/12
 * Time: 11:09 AM
 */
class GCAnalyzer {

    public static void main(String[] args) {
        if (args.length == 0) {
            println "GCAnalyzer should be used by sending a GC output log as parameter"
            return
        }
        new GCAnalyzer().exec(args[0])
    }

    public def exec(String file) {
        def gcEventsInformation = new GCEventsInformation(file)
        boolean markFullGCs = true
        JFrame frame = new JFrame('Garbage Collector Log analysis');

        JTabbedPane tabs = new JTabbedPane();
        tabs.add('non-PermGen GC log', new ChartPanel(gcEventsInformation.getNonPerGenGCChart(markFullGCs)))
        frame.add(tabs);
        frame.pack();
        frame.setExtendedState((int) frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

}

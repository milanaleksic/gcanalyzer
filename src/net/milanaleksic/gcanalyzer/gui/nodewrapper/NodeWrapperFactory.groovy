package net.milanaleksic.gcanalyzer.gui.nodewrapper

import javax.swing.JComponent
import org.jfree.chart.JFreeChart
import org.jfree.data.time.TimeSeriesCollection
import org.jfree.data.time.TimeSeries
import org.jfree.chart.ChartPanel

class NodeWrapperFactory {

    public static NodeWrapper createNodeWrapper(JComponent component) {
        if (component instanceof ChartPanel) {
            ChartPanel chartPanel = (ChartPanel) component
            if (isEmptyChart(chartPanel.chart))
                return new NoDataChartNodeWrapper(chartPanel)
            else
                return new ChartNodeWrapper(chartPanel)
        }
        return new NodeWrapper(component)
    }

    private static boolean isEmptyChart(JFreeChart chart) {
        TimeSeriesCollection dataSet = (TimeSeriesCollection) chart.getXYPlot().getDataset()
        for (TimeSeries series: dataSet.series) {
            if (!series.isEmpty())
                return false
        }
        return true
    }

}

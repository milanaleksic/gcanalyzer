package net.milanaleksic.gcanalyzer

import org.jfree.chart.ChartPanel

/**
 * Created by IntelliJ IDEA.
 * User: b25791
 * Date: 2/3/12
 * Time: 8:52 AM
 * To change this template use File | Settings | File Templates.
 */
class ChartPanelNodeWrapper {

    private def chartPanel

    public ChartPanel getChartPanel() {
        return chartPanel
    }

    @Override
    String toString() {
        return chartPanel.chart.title.text
    }
}

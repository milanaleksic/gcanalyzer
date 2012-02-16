package net.milanaleksic.gcanalyzer

import javax.swing.JComponent

class ChartPanelNodeWrapper {

    protected def chartPanel

    @Override
    String toString() {
        return chartPanel.chart.title.text
    }

    JComponent getUIComponent() {
        return chartPanel
    }

}

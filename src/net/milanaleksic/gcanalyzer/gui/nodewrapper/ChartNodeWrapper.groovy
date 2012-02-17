package net.milanaleksic.gcanalyzer.gui.nodewrapper

import org.jfree.chart.ChartPanel

class ChartNodeWrapper extends NodeWrapper {

    public ChartNodeWrapper(ChartPanel chartPanel) {
        super(chartPanel.chart.title.text, chartPanel)
    }

}

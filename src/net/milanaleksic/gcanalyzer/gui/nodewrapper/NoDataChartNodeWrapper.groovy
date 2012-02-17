package net.milanaleksic.gcanalyzer.gui.nodewrapper

import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import org.jfree.chart.ChartPanel
import javax.swing.*

class NoDataChartNodeWrapper extends ChartNodeWrapper {

    private JComponent uiComponent

    public NoDataChartNodeWrapper(ChartPanel chartPanel) {
        super (chartPanel)
        uiComponent = createNoDataComponent()
    }

    private JComponent createNoDataComponent() {
        JPanel panel = new JPanel(new GridBagLayout())
        def constraints = new GridBagConstraints()
        JLabel label = new JLabel('''<html><body><h3 align="center">No data available for this chart</h3>
<p align="center">Please read recommendations to find out how to configure your target JVM ro create logfiles which are
recognizable by GCAnalyzer application</p>
</body></html>''')
        constraints.gridx=0
        constraints.fill = GridBagConstraints.HORIZONTAL
        panel.add(label, constraints)
        return panel
    }

    @Override
    JComponent getUIComponent() {
        return uiComponent
    }

}

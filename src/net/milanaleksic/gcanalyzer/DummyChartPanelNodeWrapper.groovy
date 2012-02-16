package net.milanaleksic.gcanalyzer

import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.*

class DummyChartPanelNodeWrapper extends ChartPanelNodeWrapper {

    @Override
    JComponent getUIComponent() {
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

}

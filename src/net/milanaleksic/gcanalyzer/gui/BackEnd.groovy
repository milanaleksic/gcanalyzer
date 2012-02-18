package net.milanaleksic.gcanalyzer.gui

import java.awt.CardLayout
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.table.AbstractTableModel
import net.milanaleksic.gcanalyzer.graphing.GCEventCategory
import net.milanaleksic.gcanalyzer.graphing.GCLogInformationSource
import net.milanaleksic.gcanalyzer.gui.nodewrapper.NodeWrapper
import net.milanaleksic.gcanalyzer.gui.nodewrapper.NodeWrapperFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import javax.swing.*
import javax.swing.tree.*
import java.text.NumberFormat

class BackEnd {

    private static final String PANEL_NAME_DEFAULT = 'default'
    private static final String PANEL_NAME_OVERVIEW = 'overview'

    private JSplitPane fileAnalysisPane
    private GCLogInformationSource gcLogInformationSource
    private JTable statisticsTable

    private long chartCreationInterval

    BackEnd(GCLogInformationSource gcLogInformationSource) {
        this.gcLogInformationSource = gcLogInformationSource
        JPanel graphPanels = createGraphPanel()
        JTree tree = getTree(graphPanels)
        createTreeSelectionListener(tree, graphPanels)
        createSplitPane(tree, graphPanels)
    }

    public JSplitPane getFileAnalysisPane() {
        return fileAnalysisPane
    }

    private def createSplitPane(JTree tree, JPanel graphPanels) {
        fileAnalysisPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT)
        fileAnalysisPane.setLeftComponent(new JScrollPane(tree))
        fileAnalysisPane.setRightComponent(graphPanels)
        fileAnalysisPane.setDividerLocation(250)
    }

    private def createTreeSelectionListener(JTree tree, JPanel graphPanels) {
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            void valueChanged(TreeSelectionEvent e) {
                Object nodeWrapperAsObject = ((DefaultMutableTreeNode) e.path.getLastPathComponent())?.userObject
                CardLayout layout = (CardLayout) graphPanels.getLayout()
                if (!(nodeWrapperAsObject instanceof NodeWrapper)) {
                    layout.show(graphPanels, PANEL_NAME_DEFAULT)
                    return
                }
                NodeWrapper nodeWrapper = (NodeWrapper) nodeWrapperAsObject
                if (!nodeWrapper)
                    return
                layout.show(graphPanels, nodeWrapper.toString())
            }
        })
    }

    private JTree getTree(JPanel graphPanels) {
        TreeNode root = new DefaultMutableTreeNode('GC log details')
        root.add(prepareTreeNodeForComponent(createBasicStatisticsTable(), graphPanels))
        root.add(createTreeNodeForAllCharts(root, graphPanels))

        ((AbstractTableModel)statisticsTable.getModel()).fireTableStructureChanged()

        JTree ofTheJedi = new JTree(root)
        ofTheJedi.showsRootHandles = false
        return ofTheJedi
    }

    private MutableTreeNode createTreeNodeForAllCharts(DefaultMutableTreeNode root, JPanel graphPanels) {
        DefaultMutableTreeNode allCharts = new DefaultMutableTreeNode('All charts')
        long begin = System.currentTimeMillis()
        GCEventCategory.each { GCEventCategory category ->
            DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(category.getTitle())
            allCharts.add(categoryNode)
            gcLogInformationSource.getChartsForCategory(category).each { JFreeChart chart ->
                categoryNode.add(prepareTreeNodeForChart(chart, graphPanels))
            }
        }
        chartCreationInterval = System.currentTimeMillis() - begin
        root.add(allCharts)
        return allCharts
    }

    private JComponent createBasicStatisticsTable() {
        statisticsTable = new JTable(new AbstractTableModel() {

            private Object[][] data = [
                    ['Information', 'Value'],
                    ['Filename', { gcLogInformationSource.location }],
                    ['Parsing time', { "${gcLogInformationSource.parsingTime} ms" }],
                    ['Chart calculation and creation time', { "$chartCreationInterval ms" }],
                    ['',''],
                    ['Total time spent on garbage collection', { String.format('%d ms (%6.4f%%)',
                            gcLogInformationSource.totalTimeSpentOnGC(), gcLogInformationSource.totalTimeSpentOnGCInPercent()) }],
                    ['',''],
                    ['Detected Young GC events', { gcLogInformationSource.numberOfDetectedYoungGCEvents() }],
                    ['Average length of Young GC events', { "${NumberFormat.getNumberInstance().format(gcLogInformationSource.averageYoungGCEventLength())} ms" }],
                    ['Standard deviation of Young GC events', { "${NumberFormat.getNumberInstance().format(gcLogInformationSource.standardDeviationYoungGCEventLength())} ms" }],
                    ['Total time spent on Young garbage collection', { String.format('%d ms (%6.4f%%)',
                            gcLogInformationSource.totalTimeSpentOnYoungGC(), gcLogInformationSource.totalTimeSpentOnYoungGCInPercent()) }],
                    ['',''],
                    ['Detected Full GC events', { gcLogInformationSource.numberOfDetectedFullGCEvents() }],
                    ['Average length of Full GC events', { "${NumberFormat.getNumberInstance().format(gcLogInformationSource.averageFullGCEventLength())} ms" }],
                    ['Standard deviation of Full GC events', { "${NumberFormat.getNumberInstance().format(gcLogInformationSource.standardDeviationFullGCEventLength())} ms" }],
                    ['Total time spent on Full garbage collection', { String.format('%d ms (%6.4f%%)',
                            gcLogInformationSource.totalTimeSpentOnFullGC(), gcLogInformationSource.totalTimeSpentOnFullGCInPercent()) }],
                    ['','']
            ] as Object[][]


            @Override int getRowCount() { return data.length-1 }

            @Override int getColumnCount() { return 2 }

            @Override Object getValueAt(int rowIndex, int columnIndex) {
                Object datum = data[rowIndex + 1][columnIndex]
                return datum instanceof Closure ? datum() : datum.toString()
            }

            @Override String getColumnName(int column) { return data[0][column] }
        })

        JScrollPane ofTheJedi = new JScrollPane(statisticsTable)
        ofTheJedi.setName('Overview')
        return ofTheJedi
    }

    private MutableTreeNode prepareTreeNodeForComponent(JComponent tableWithData, JPanel graphPanels) {
        if (!tableWithData)
            return
        NodeWrapper wrapper = NodeWrapperFactory.createNodeWrapper(tableWithData)
        TreeNode newNode = new DefaultMutableTreeNode(wrapper)
        graphPanels.add(wrapper.getUIComponent(), wrapper.toString())
        return newNode
    }

    private def prepareTreeNodeForChart(JFreeChart chart, JPanel graphPanels) {
        if (!chart)
            return
        NodeWrapper wrapper = NodeWrapperFactory.createNodeWrapper(new ChartPanel(chart))
        TreeNode newNode = new DefaultMutableTreeNode(wrapper)
        graphPanels.add(wrapper.getUIComponent(), wrapper.toString())
        return newNode
    }

    private JPanel createGraphPanel() {
        JPanel graphPanels = new JPanel(new CardLayout())
        graphPanels.add(new JPanel(), PANEL_NAME_DEFAULT)
        return graphPanels
    }

}

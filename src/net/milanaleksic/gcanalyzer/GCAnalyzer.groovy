package net.milanaleksic.gcanalyzer

import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.DefaultMutableTreeNode
import net.milanaleksic.gcanalyzer.graphing.GCEventCategory
import net.milanaleksic.gcanalyzer.graphing.GCEventsInformation
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import java.awt.*
import javax.swing.*

class GCAnalyzer {

    private Container fileAnalysisContainer
    private JTextField fileNameTextField

    private FileParsingFinishedListener fileParsingFinishedListener

    public static def setUpNimbusLookAndFeel() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel")
        } catch (Throwable ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
            } catch (Throwable ignored2) {
            }
        }
    }

    def initGuiForApplication(Container container) {
        container.setLayout(new BorderLayout())
        container.add(getHeaderPanel(), BorderLayout.NORTH)

        fileAnalysisContainer = new JTabbedPane()
        fileAnalysisContainer.add("Heap size recommendations", new HeapSizeRecommendationsPanel())

        container.add(fileAnalysisContainer)
        container.add(getFooterVersionLabel(), BorderLayout.SOUTH)
    }

    def initGuiForApplet(Container container) {
        container.setLayout(new BorderLayout())
        fileAnalysisContainer = container
        container.add(getFooterVersionLabel(), BorderLayout.SOUTH)
    }

    private JPanel getHeaderPanel() {
        JPanel panel = new JPanel()
        def constraints = new GridBagConstraints()
        panel.setLayout(new GridBagLayout())
        constraints.gridx=0
        constraints.fill = GridBagConstraints.HORIZONTAL
        panel.add(new JLabel("Java GC file: "), constraints)
        constraints.gridx=1
        constraints.weightx = 0.9
        panel.add(fileNameTextField = new JTextField(), constraints)
        JButton button = new JButton("Load")
        button.addActionListener( { ActionEvent e ->
            addFile(fileNameTextField.text)
        } as ActionListener)
        constraints.gridx=2
        constraints.weightx = 0.1
        panel.add(button, constraints)
        return panel
    }

    private JLabel getFooterVersionLabel() {
        JLabel label = new JLabel("GC Analyzer v${Utils.getApplicationVersion()} by milan.aleksic@gmail.com")
        label.setBackground(Color.GRAY)
        label.font = new Font("Arial", Font.PLAIN, 10)
        label.setHorizontalAlignment(JLabel.CENTER)
        return label
    }

    void addFile(String filename) {
        createGuiForEvents(new GCEventsInformation(filename))
    }

    void addUrl(URL url) {
        createGuiForEvents(new GCEventsInformation(url))
    }

    private def createGuiForEvents(GCEventsInformation gcEventsInformation) {
        JSplitPane fileAnalysisPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT)
        JPanel graphPanels = new JPanel(new CardLayout())
        graphPanels.add(new JPanel(), "")

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Analysis graph")
        GCEventCategory.each { GCEventCategory category ->
            DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(category.getTitle())
            root.add(categoryNode)
            JFreeChart[] charts = gcEventsInformation.getChartsForCategory(category)
            charts.each { JFreeChart chart ->
                addGraph(categoryNode, graphPanels, new ChartPanel(chart))
            }
        }
        SwingUtilities.invokeLater {
            JTree tree = new JTree(root)
            tree.addTreeSelectionListener(new TreeSelectionListener() {
                @Override
                void valueChanged(TreeSelectionEvent e) {
                    Object nodeWrapperAsObject = ((DefaultMutableTreeNode) e.path.getLastPathComponent())?.userObject
                    CardLayout layout = (CardLayout) graphPanels.getLayout()
                    if (!(nodeWrapperAsObject instanceof ChartPanelNodeWrapper)) {
                        layout.show(graphPanels, "")
                        return
                    }
                    ChartPanelNodeWrapper nodeWrapper = (ChartPanelNodeWrapper) nodeWrapperAsObject
                    if (!nodeWrapper)
                        return
                    layout.show(graphPanels, nodeWrapper.toString())
                }
            })
            fileAnalysisPanel.setLeftComponent(new JScrollPane(tree))
            fileAnalysisPanel.setRightComponent(graphPanels)
            fileAnalysisPanel.setDividerLocation(250)

            if (fileAnalysisContainer instanceof JTabbedPane)
                fileAnalysisContainer.add(gcEventsInformation.getTitle(), fileAnalysisPanel)
            else
                fileAnalysisContainer.add(fileAnalysisPanel)
            fireFileParsingFinishedEvent(gcEventsInformation.getTitle())
        } as Runnable
    }

    private def fireFileParsingFinishedEvent(String fileName) {
        if (fileParsingFinishedListener)
            fileParsingFinishedListener.onFileParsingFinished(fileName)
    }

    def addGraph(DefaultMutableTreeNode root, JPanel graphPanels, ChartPanel chartPanel) {
        if (!chartPanel.chart)
            return
        ChartPanelNodeWrapper wrapper = new ChartPanelNodeWrapper(chartPanel: chartPanel)
        graphPanels.add(chartPanel, wrapper.toString())
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(wrapper)
        root.add(newNode)
    }
}

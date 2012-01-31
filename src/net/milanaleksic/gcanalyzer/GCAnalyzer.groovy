package net.milanaleksic.gcanalyzer

import groovy.io.FileType
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.util.concurrent.atomic.AtomicInteger
import org.jfree.chart.ChartPanel
import java.awt.*
import javax.swing.*

/**
 * User: Milan Aleksic
 * Date: 1/29/12
 * Time: 11:09 AM
 */
class GCAnalyzer {

    public static void main(String[] args) {
        new GCAnalyzer(args: args).exec()
    }

    private JFrame frame
    private JTabbedPane fileTabs
    private JTextField fileNameTextField

    private String[] args

    private AtomicInteger counter = new AtomicInteger(0)

    private static final String TITLE = 'Garbage Collector Log analysis'

    public def exec() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Throwable ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Throwable ignored2) {
            }
        }
        frame = new JFrame(TITLE)

        frame.add(getMainAnalyzerPanel(), BorderLayout.NORTH)

        fileTabs = new JTabbedPane()
        fileTabs.add("Heap size recommendations", new HeapSizeRecommendationsPanel())

        frame.add(fileTabs)
        frame.setPreferredSize(new Dimension(750, 550))
        frame.setLocation(new Point(100, 100))
        frame.pack()
        frame.setExtendedState((int) frame.getExtendedState() | JFrame.MAXIMIZED_BOTH)
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
        frame.setVisible(true)

        startRequestedFilesParsing()
    }

    private JPanel getMainAnalyzerPanel() {
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

    void startRequestedFilesParsing() {
        if (!args || args.size() == 0)
            return
        new Thread({
            def files = []
            args.each { String filename ->
                def file = new File(filename)
                if (file.isDirectory()) {
                    file.eachFile(FileType.FILES) { File childFile ->
                        files << childFile.absolutePath
                    }
                }
                else
                    files << filename
            }
            if (files.size() > 0) {
                frame.setTitle("$TITLE - Parsing input")
                counter.set(files.size())
                files.each {
                    addFile(it)
                }
            }
        } as Runnable).run()
    }

    void addFile(String fileName) {
        def gcEventsInformation = new GCEventsInformation(fileName)
        JTabbedPane graphTabs = new JTabbedPane()
        graphTabs.add('All Event timings', new ChartPanel(gcEventsInformation.getEventTimingsChart()))
        graphTabs.add('Young Gen Event timings', new ChartPanel(gcEventsInformation.getYoungGCEventTimingsChart()))
        graphTabs.add('Full GC Event timings', new ChartPanel(gcEventsInformation.getFullGCEventTimingsChart()))
        graphTabs.add('Heap without Permanent generation', new ChartPanel(gcEventsInformation.getHeapWithoutPermanentGenerationGCChart()))
        graphTabs.add('Young Gen Max Memory', new ChartPanel(gcEventsInformation.getYoungGenerationChart()))
        graphTabs.add('Old Gen Max Memory', new ChartPanel(gcEventsInformation.getOldGenerationChart()))
        graphTabs.add('Permanent Gen Max Memory', new ChartPanel(gcEventsInformation.getPermanentGenerationChart()))
        SwingUtilities.invokeLater {
            fileTabs.add(new File(fileName).name, graphTabs)
            if (counter.decrementAndGet() == 0) {
                frame.setTitle(TITLE)
            }
        } as Runnable
    }

}
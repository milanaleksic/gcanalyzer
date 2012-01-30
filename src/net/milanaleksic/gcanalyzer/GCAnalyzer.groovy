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
        frame = new JFrame(TITLE)
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

        frame.add(panel, BorderLayout.NORTH)

        fileTabs = new JTabbedPane()
        frame.add(fileTabs)
        frame.pack()
        frame.setExtendedState((int) frame.getExtendedState() | JFrame.MAXIMIZED_BOTH)
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
        frame.setVisible(true)
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
        boolean markFullGCs = true
        graphTabs.add('non-PermGen GC', new ChartPanel(gcEventsInformation.getNonPerGenGCChart(markFullGCs)))
        graphTabs.add('event timings', new ChartPanel(gcEventsInformation.getEventTimingsChart(markFullGCs)))
        SwingUtilities.invokeLater {
            fileTabs.add(new File(fileName).name, graphTabs)
            if (counter.decrementAndGet() == 0) {
                frame.setTitle(TITLE)
            }
        } as Runnable
    }

}
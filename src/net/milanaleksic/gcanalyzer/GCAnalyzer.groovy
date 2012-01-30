package net.milanaleksic.gcanalyzer

import javax.swing.JFrame
import javax.swing.JTabbedPane
import org.jfree.chart.ChartPanel
import javax.swing.JPanel
import javax.swing.JLabel
import java.awt.BorderLayout

import javax.swing.JButton
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JTextField
import java.awt.GridBagLayout
import java.awt.GridBagConstraints
import javax.swing.SwingUtilities

/**
 * User: Milan Aleksic
 * Date: 1/29/12
 * Time: 11:09 AM
 */
class GCAnalyzer {

    public static void main(String[] args) {
        new GCAnalyzer(args: args).exec()
    }

    private JTabbedPane fileTabs

    private JTextField fileNameTextField

    private String[] args

    public def exec() {
        JFrame frame = new JFrame('Garbage Collector Log analysis')
        JPanel panel = new JPanel()
        def constraints = new GridBagConstraints()
        panel.setLayout(new GridBagLayout())
        constraints.gridx=0
        constraints.fill = GridBagConstraints.HORIZONTAL
        panel.add(new JLabel("Java GC file: "), constraints)
        constraints.gridx=1
        constraints.weightx = 0.8
        panel.add(fileNameTextField = new JTextField(), constraints)
        JButton button = new JButton("Load")
        button.addActionListener( { ActionEvent e ->
            addFile(fileNameTextField.text)
        } as ActionListener)
        constraints.gridx=2
        constraints.weightx = 0.2
        panel.add(button, constraints)

        frame.add(panel, BorderLayout.NORTH)

        fileTabs = new JTabbedPane()
        frame.add(fileTabs)
        frame.pack()
        frame.setExtendedState((int) frame.getExtendedState() | JFrame.MAXIMIZED_BOTH)
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
        frame.setVisible(true)


        new Thread({

            Thread.sleep(100)

            SwingUtilities.invokeLater {
                args.each { String filename ->
                    def file = new File(filename)
                    if (file.isDirectory()) {
                        file.eachFile { File childFile ->
                            addFile(childFile.absolutePath)
                        }
                    }
                    else
                        addFile(filename)
                }
            } as Runnable

        } as Runnable).run()
    }

    void addFile(String fileName) {
        def gcEventsInformation = new GCEventsInformation(fileName)
        JTabbedPane graphTabs = new JTabbedPane()
        boolean markFullGCs = true
        graphTabs.add('non-PermGen GC', new ChartPanel(gcEventsInformation.getNonPerGenGCChart(markFullGCs)))
        graphTabs.add('event timings', new ChartPanel(gcEventsInformation.getEventTimingsChart(markFullGCs)))
        fileTabs.add(new File(fileName).name, graphTabs)
    }

}
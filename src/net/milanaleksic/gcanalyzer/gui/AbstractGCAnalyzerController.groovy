package net.milanaleksic.gcanalyzer.gui

import java.awt.event.ActionEvent
import java.awt.event.ActionListener

import net.milanaleksic.gcanalyzer.util.FileDrop
import net.milanaleksic.gcanalyzer.util.Utils
import java.awt.*
import javax.swing.*

import net.milanaleksic.gcanalyzer.graphing.GCLogInformationSource

abstract class AbstractGCAnalyzerController {

    protected Container fileAnalysisContainer

    private JTextField fileNameTextField

    private ParsingFinishedListener parsingFinishedListener

    public AbstractGCAnalyzerController(ParsingFinishedListener parsingFinishedListener) {
        this.parsingFinishedListener = parsingFinishedListener
    }

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

    protected def setupDragAndDropListener() {
        new FileDrop(fileAnalysisContainer, { java.io.File[] files ->
            for (int i = 0; i < files.length; i++) {
                addFile(files[i].absolutePath)
            }
        } as FileDrop.Listener)
    }

    protected JPanel getHeaderPanel() {
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

    protected JLabel getFooterVersionLabel() {
        JLabel label = new JLabel("GC Analyzer v${Utils.getApplicationVersion()} by milan.aleksic@gmail.com")
        label.setBackground(Color.GRAY)
        label.font = new Font("Arial", Font.PLAIN, 10)
        label.setHorizontalAlignment(JLabel.CENTER)
        return label
    }

    void addFile(String filename) {
        createGuiForEvents(new GCLogInformationSource(filename))
    }

    void addUrl(URL url) {
        createGuiForEvents(new GCLogInformationSource(url))
    }

    private def createGuiForEvents(GCLogInformationSource chartGenerator) {
        SwingUtilities.invokeLater {
            BackEnd backEnd = new BackEnd(chartGenerator)
            if (fileAnalysisContainer instanceof JTabbedPane)
                fileAnalysisContainer.add(chartGenerator.title, backEnd.fileAnalysisPane)
            else
                fileAnalysisContainer.add(backEnd.fileAnalysisPane)
            fireFileParsingFinishedEvent(chartGenerator.title)
        } as Runnable
    }

    protected def fireFileParsingFinishedEvent(String fileName) {
        if (parsingFinishedListener)
            parsingFinishedListener.onFileParsingFinished(fileName)
    }

}

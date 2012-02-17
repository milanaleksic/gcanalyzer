package net.milanaleksic.gcanalyzer.gui

import java.awt.BorderLayout
import javax.swing.JTabbedPane
import java.awt.Container

class ApplicationGCAnalyzerController extends AbstractGCAnalyzerController {

    public ApplicationGCAnalyzerController(Container container, ParsingFinishedListener parsingFinishedListener) {
        super(parsingFinishedListener)

        fileAnalysisContainer = new JTabbedPane()
        fileAnalysisContainer.add("Heap size recommendations", new HeapSizeRecommendationsPanel())

        container.setLayout(new BorderLayout())
        container.add(getHeaderPanel(), BorderLayout.NORTH)
        container.add(fileAnalysisContainer, BorderLayout.CENTER)
        container.add(getFooterVersionLabel(), BorderLayout.SOUTH)

        setupDragAndDropListener()
    }

}

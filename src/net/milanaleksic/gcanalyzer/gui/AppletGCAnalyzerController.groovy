package net.milanaleksic.gcanalyzer.gui

import javax.swing.JComponent
import java.awt.BorderLayout

class AppletGCAnalyzerController extends AbstractGCAnalyzerController {

    public AppletGCAnalyzerController(JComponent container, ParsingFinishedListener parsingFinishedListener) {
        super(parsingFinishedListener)
        fileAnalysisContainer = container
        fileAnalysisContainer.setLayout(new BorderLayout())
        fileAnalysisContainer.add(getFooterVersionLabel(), BorderLayout.SOUTH)
    }

}

package net.milanaleksic.gcanalyzer

import java.awt.Font
import javax.swing.JLabel
import javax.swing.SwingUtilities
import javax.swing.JApplet
import javax.swing.JPanel
import net.milanaleksic.gcanalyzer.gui.ParsingFinishedListener

import net.milanaleksic.gcanalyzer.gui.AbstractGCAnalyzerController
import net.milanaleksic.gcanalyzer.gui.AppletGCAnalyzerController

class GCAnalyzerApplet extends JApplet implements ParsingFinishedListener {

    private AbstractGCAnalyzerController controller

    @Override
    void init() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    JPanel panel = createContentPane()
                    controller = new AppletGCAnalyzerController(panel, GCAnalyzerApplet.this)
                }
            })
            loadLog(getLogLocation())
        } catch (Exception e) {
            showVeryBigErrorMessage("Problem: ${e.getMessage()}")
            e.printStackTrace()
        }
    }

    JPanel createContentPane() {
        JPanel panel = new JPanel()
        setContentPane(panel)
        return panel
    }

    private URL getLogLocation() {
        def logParameter = getParameter('completeLogUrl')
        if (logParameter)
            return new URL(logParameter)

        logParameter = getParameter('logUrl')
        if (!logParameter)
            throw new RuntimeException("Neither 'logUrl' nor 'completeLogUrl' parameter was sent to applet - so no graphs to show. Sorry!")

        URL targetUrl = new URL(getCodeBase(), logParameter)
        return targetUrl
    }

    private def showMessage(message) {
        println message
        showStatus(message)
    }

    private def loadLog(URL logUrl) {
        showMessage("Loading GC log from $logUrl")
        controller.addUrl(logUrl)
    }

    private def showVeryBigErrorMessage(String message) {
        JLabel label = new JLabel(message)
        Font font = new Font("Arial", Font.BOLD, 14)
        label.setFont(font)
        getContentPane().add(label)
    }

    @Override
    void onFileParsingFinished(String fileName) {
        showStatus("")
    }

}

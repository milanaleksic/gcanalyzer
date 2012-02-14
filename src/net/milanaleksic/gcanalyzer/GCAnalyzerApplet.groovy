package net.milanaleksic.gcanalyzer

import java.applet.Applet
import javax.swing.SwingUtilities
import javax.swing.JLabel
import java.awt.Font

class GCAnalyzerApplet extends Applet {

    private GCAnalyzer analyzer

    @Override
    void init() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    analyzer = new GCAnalyzer()
                    analyzer.initGuiForApplet(GCAnalyzerApplet.this)
                }
            })
            loadLog(getParameter('logUrl'))
        } catch (Exception e) {
            System.err.println("GUI applet creation didn't complete successfully")
            e.printStackTrace()
        }
    }

    private def showMessage(message) {
        println message
        showStatus(message)
    }

    private def loadLog(String logUrl) {
        if (!logUrl) {
            showNoLogUrlParamSentToApplet()
            return
        }
        URL targetUrl = new URL(getCodeBase(), logUrl)
        showMessage("Loading GC log from $targetUrl")
        analyzer.addUrl(targetUrl)
    }

    private def showNoLogUrlParamSentToApplet() {
        JLabel label = new JLabel("No 'logUrl' parameter sent to applet - so no graphs to show. Sorry!")
        Font font = new Font("Arial", Font.BOLD, 14)
        label.setFont(font)
        add(label)
    }

}

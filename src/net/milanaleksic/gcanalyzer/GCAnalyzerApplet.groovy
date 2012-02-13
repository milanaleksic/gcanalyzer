package net.milanaleksic.gcanalyzer

import java.applet.Applet
import javax.swing.SwingUtilities

class GCAnalyzerApplet extends Applet {

    @Override
    void init() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    GCAnalyzer.setUpNimbusLookAndFeel()
                    GCAnalyzer analyzer = new GCAnalyzer(container: GCAnalyzerApplet.this)
                    analyzer.initGui()
                }
            })
        } catch (Exception e) {
            System.err.println("GUI applet creation didn't complete successfully")
            e.printStackTrace()
        }
    }


}

package net.milanaleksic.gcanalyzer

import groovy.io.FileType
import java.awt.Dimension
import java.awt.Point
import java.lang.Thread.UncaughtExceptionHandler
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.*

/**
 * User: Milan Aleksic
 * Date: 1/29/12
 * Time: 11:09 AM
 */
class GCAnalyzerApplication implements FileParsingFinishedListener {

    public static final int STACK_TRACE_MAX_LENGTH = 1536

    private static final String TITLE = 'Garbage Collector Log analysis'

    private AtomicInteger counter = new AtomicInteger(0)

    private JFrame frame

    public static void main(String[] args) {
        new GCAnalyzerApplication().exec(args)
    }

    private def exec(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler({ Thread t, Throwable e ->
            def stackTrace = Utils.getStackTrace(e)
            if (stackTrace && stackTrace.size() > STACK_TRACE_MAX_LENGTH)
                stackTrace = stackTrace.substring(0, STACK_TRACE_MAX_LENGTH) + "..."
            JOptionPane.showMessageDialog(null, "Exception occurred: ${e.getMessage()}\r\n\r\nDetails:\r\n${stackTrace}")
        } as UncaughtExceptionHandler)

        GCAnalyzer.setUpNimbusLookAndFeel()

        GCAnalyzer analyzer = null
        SwingUtilities.invokeAndWait {
            frame = new JFrame(TITLE)

            analyzer = new GCAnalyzer(fileParsingFinishedListener: this)
            analyzer.initGuiForApplication(frame)

            frame.setPreferredSize(new Dimension(750, 550))
            frame.setLocation(new Point(100, 100))
            frame.pack()
            frame.setExtendedState((int) frame.getExtendedState() | JFrame.MAXIMIZED_BOTH)
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
            frame.setVisible(true)
        }
        if (analyzer)
            startRequestedFilesParsing(args, analyzer)
    }

    public void startRequestedFilesParsing(args, analyzer) {
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
                    analyzer.addFile(it)
                }
            }
        } as Runnable).run()
    }

    @Override
    void onFileParsingFinished(String fileName) {
        if (counter.decrementAndGet() == 0) {
            frame.setTitle(TITLE)
        }
    }

}

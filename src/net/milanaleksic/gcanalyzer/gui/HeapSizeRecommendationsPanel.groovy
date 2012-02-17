package net.milanaleksic.gcanalyzer.gui

import java.awt.BorderLayout
import javax.swing.*
import javax.swing.text.html.HTMLEditorKit

class HeapSizeRecommendationsPanel extends JPanel {

    HeapSizeRecommendationsPanel() {
        InputStream inputStream
        try {
            inputStream = this.getClass().getResourceAsStream("/net/milanaleksic/gcanalyzer/readme.html")
            setLayout(new BorderLayout())
            add(getReadMePane(inputStream))
        } finally {
            if (inputStream)
                inputStream.close()
        }
    }

    private JComponent getReadMePane(InputStream readMe) {
        JEditorPane area = new JEditorPane()
        area.setEditorKit(new HTMLEditorKit());
        area.read(readMe, 'readMe')
        JScrollPane pane = new JScrollPane(area)
        pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)
        pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED)
        return pane
    }

}

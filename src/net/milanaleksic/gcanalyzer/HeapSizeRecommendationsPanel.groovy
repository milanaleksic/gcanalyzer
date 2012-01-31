package net.milanaleksic.gcanalyzer

import javax.swing.JPanel
import java.awt.Graphics
import javax.imageio.ImageIO

class HeapSizeRecommendationsPanel extends JPanel {

    def image

    HeapSizeRecommendationsPanel() {
        InputStream inputStream
        try {
            inputStream = this.getClass().getResourceAsStream("/net/milanaleksic/gcanalyzer/heap_sizing.jpg")
            image = ImageIO.read(inputStream)
        } finally {
            if (inputStream)
                inputStream.close()
        }
    }

    @Override
    void paint(Graphics g) {
        super.paint(g)
        g.drawImage(image, 0, 0, null)
    }

}

package net.milanaleksic.gcanalyzer.gui.nodewrapper

import javax.swing.JComponent

class NodeWrapper {

    private String title
    private JComponent component

    public NodeWrapper(String title, JComponent component) {
        this.title = title
        this.component = component
    }

    public NodeWrapper(JComponent component) {
        this.title = component.getName()
        this.component = component
    }

    @Override
    String toString() {
        return title
    }

    JComponent getUIComponent() {
        return component
    }

}

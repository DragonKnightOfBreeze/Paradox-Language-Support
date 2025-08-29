package icu.windea.pls.extension.diagram

import com.intellij.diagram.AbstractDiagramElementManager
import com.intellij.diagram.DiagramBuilder
import javax.swing.JComponent

abstract class DiagramElementManagerEx<T> : AbstractDiagramElementManager<T>() {
    open fun getItemComponent(nodeElement: T, nodeItem: Any?, builder: DiagramBuilder): JComponent? = null
}

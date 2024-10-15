package icu.windea.pls.extension.diagram

import com.intellij.diagram.*
import javax.swing.*

abstract class DiagramElementManagerEx<T> : AbstractDiagramElementManager<T>() {
    open fun getItemComponent(nodeElement: T, nodeItem: Any?, builder: DiagramBuilder): JComponent? = null
}


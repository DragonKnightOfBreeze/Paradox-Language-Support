package icu.windea.pls.extension.diagram

import com.intellij.diagram.*

abstract class DiagramElementManagerEx<T> : AbstractDiagramElementManager<T>() {
    open fun handleItemComponent(nodeElement: T, nodeItem: Any?, builder: DiagramBuilder, itemComponent: DiagramNodeItemComponentEx) {
        
    }
}
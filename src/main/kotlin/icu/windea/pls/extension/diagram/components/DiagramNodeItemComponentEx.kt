package icu.windea.pls.extension.diagram.components

import com.intellij.diagram.*
import com.intellij.diagram.components.*
import icu.windea.pls.extension.diagram.*
import java.awt.*

//com.intellij.diagram.components.DiagramNodeContainer
//com.intellij.diagram.components.DiagramNodeBodyComponent
//com.intellij.diagram.components.DiagramNodeItemComponent

class DiagramNodeItemComponentEx : DiagramNodeItemComponent() {
    //使用自定义组件时myLeft和myRight的宽度应当为0

    @Suppress("UNCHECKED_CAST")
    override fun setUp(owner: DiagramNodeBodyComponent, builder: DiagramBuilder, node: DiagramNode<Any>, element: Any?, selected: Boolean) {
        super.setUp(owner, builder, node, element, selected)
        val elementManager = builder.provider.elementManager as DiagramElementManager<Any>
        if (elementManager is DiagramElementManagerEx) {
            if (components.size == 3) {
                remove(2)
            }
            val nodeElement = node.identifyingElement
            val component = elementManager.getItemComponent(nodeElement, element, builder)
            if (component != null) {
                add(component)
                left.size = Dimension(0, left.preferredSize.height)
                right.size = Dimension(0, right.preferredSize.height)
            } else {
                left.size = left.preferredSize
                right.size = right.preferredSize
            }
        }
    }
}

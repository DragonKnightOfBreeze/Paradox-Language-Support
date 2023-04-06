package icu.windea.pls.extension.diagram.components

import com.intellij.diagram.*
import com.intellij.diagram.components.*
import com.intellij.ui.*
import icu.windea.pls.core.*
import icu.windea.pls.extension.diagram.*
import java.awt.*

//com.intellij.diagram.components.DiagramNodeContainer
//com.intellij.diagram.components.DiagramNodeBodyComponent
//com.intellij.diagram.components.DiagramNodeItemComponent

class DiagramNodeItemComponentEx : DiagramNodeItemComponent() {
    private var useComponent = false
    
    //使用自定义组件时myLeft和myRight的宽度应当为0
    
    init {
        val left = object : SimpleColoredComponent() {
            override fun getPreferredSize() = super.getPreferredSize().alsoIf(useComponent) { it.width = 0 }
        }
        val right = object : SimpleColoredComponent() {
            override fun getPreferredSize() = super.getPreferredSize().alsoIf(useComponent) { it.width = 0 }
        }
        this.setFieldValue<DiagramNodeItemComponent>("myLeft", left)
        this.setFieldValue<DiagramNodeItemComponent>("myRight", right)
        removeAll()
        add(left, BorderLayout.WEST)
        add(right, BorderLayout.EAST)
        left.isOpaque = true
        left.isIconOpaque = true
        right.isOpaque = true
        right.isIconOpaque = true
        this.isOpaque = true
    }
    
    @Suppress("UNCHECKED_CAST")
    override fun setUp(owner: DiagramNodeBodyComponent, builder: DiagramBuilder, node: DiagramNode<Any>, element: Any?, selected: Boolean) {
        super.setUp(owner, builder, node, element, selected)
        val elementManager = builder.provider.elementManager as DiagramElementManager<Any>
        if(elementManager is DiagramElementManagerEx) {
            if(components.size == 3) {
                remove(2)
            }
            val nodeElement = node.identifyingElement
            val component = elementManager.getItemComponent(nodeElement, element, builder)
            if(component != null) {
                add(component)
                useComponent = true
            } else {
                useComponent = false
            }
            this.size = this.preferredSize
        }
    }
}
package icu.windea.pls.extension.diagram

import com.intellij.diagram.*
import com.intellij.diagram.components.*
import com.intellij.diagram.extras.custom.*
import com.intellij.openapi.graph.view.*
import com.intellij.psi.*
import com.intellij.ui.*
import javax.swing.*

private val myItemComponentField by lazy { DiagramNodeBodyComponent::class.java.getDeclaredField("myItemComponent").apply { trySetAccessible() } }
private val myLeftField by lazy { DiagramNodeItemComponent::class.java.getDeclaredField("myLeft").apply { trySetAccessible() } }
private val myRightField by lazy { DiagramNodeItemComponent::class.java.getDeclaredField("myRight").apply { trySetAccessible() } }

abstract class DiagramExtrasEx: CommonDiagramExtras<PsiElement>() {
    //com.intellij.diagram.components.DiagramNodeContainer
    //com.intellij.diagram.components.DiagramNodeBodyComponent
    //com.intellij.diagram.components.DiagramNodeItemComponent
    
    override fun createNodeComponent(node: DiagramNode<PsiElement>, builder: DiagramBuilder, nodeRealizer: NodeRealizer, wrapper: JPanel): JComponent {
        //允许添加自定义的组件
        val component = super.createNodeComponent(node, builder, nodeRealizer, wrapper)
        if(component is DiagramNodeContainer) {
            val nodeBodyComponent = component.nodeBodyComponent
            myItemComponentField.set(nodeBodyComponent, DiagramNodeItemComponentEx())
        }
        return component
    }
}

class DiagramNodeItemComponentEx: DiagramNodeItemComponent() {
    val left = myLeftField.get(this) as SimpleColoredComponent
    val right = myRightField.get(this) as SimpleColoredComponent
    
    @Suppress("UNCHECKED_CAST")
    override fun setUp(owner: DiagramNodeBodyComponent, builder: DiagramBuilder, node: DiagramNode<Any>, element: Any?, selected: Boolean) {
        val elementManager = builder.provider.elementManager as DiagramElementManager<Any>
        if(elementManager is DiagramElementManagerEx) {
            val nodeElement = node.identifyingElement
            elementManager.handleItemComponent(nodeElement, element, builder, this)
        }
        super.setUp(owner, builder, node, element, selected)
    }
    
    fun reset() {
        if(components.size == 2 && getComponent(0) == left && getComponent(1) == right) return
        removeAll()
        add(left)
        add(right)
    }
}
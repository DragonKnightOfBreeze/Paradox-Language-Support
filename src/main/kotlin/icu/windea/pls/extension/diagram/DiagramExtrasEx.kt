package icu.windea.pls.extension.diagram

import com.intellij.diagram.*
import com.intellij.diagram.components.*
import com.intellij.diagram.extras.custom.*
import com.intellij.openapi.graph.view.*
import com.intellij.psi.*
import javax.swing.*

private val myItemComponentField by lazy { DiagramNodeBodyComponent::class.java.getDeclaredField("myItemComponent").apply { trySetAccessible() } }
private val myLeftField by lazy { DiagramNodeItemComponent::class.java.getDeclaredField("myLeft").apply { trySetAccessible() } }

abstract class DiagramExtrasEx: CommonDiagramExtras<PsiElement>() {
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

//com.intellij.diagram.components.DiagramNodeContainer
//com.intellij.diagram.components.DiagramNodeBodyComponent
//com.intellij.diagram.components.DiagramNodeItemComponent

class DiagramNodeItemComponentEx: DiagramNodeItemComponent() {
    private val panel = JPanel()
    
    init {
        this.add(panel)
    }
    
    @Suppress("UNCHECKED_CAST")
    override fun setUp(owner: DiagramNodeBodyComponent, builder: DiagramBuilder, node: DiagramNode<Any>, element: Any?, selected: Boolean) {
        super.setUp(owner, builder, node, element, selected)
        val elementManager = builder.provider.elementManager as DiagramElementManager<Any>
        if(elementManager is DiagramElementManagerEx) {
            val nodeElement = node.identifyingElement
            val component = elementManager.getItemComponent(nodeElement, element, builder)
            if(component != null) {
                panel.add(component)
            } else {
                panel.removeAll()
            }
        }
    }
}
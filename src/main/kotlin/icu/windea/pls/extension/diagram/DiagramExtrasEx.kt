package icu.windea.pls.extension.diagram

import com.intellij.diagram.*
import com.intellij.diagram.components.*
import com.intellij.diagram.extras.custom.*
import com.intellij.openapi.graph.view.*
import com.intellij.psi.*
import com.intellij.ui.*
import icu.windea.pls.core.*
import java.awt.*
import javax.swing.*

private val myItemComponentField by lazy { DiagramNodeBodyComponent::class.java.getDeclaredField("myItemComponent").apply { trySetAccessible() } }

abstract class DiagramExtrasEx : CommonDiagramExtras<PsiElement>() {
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
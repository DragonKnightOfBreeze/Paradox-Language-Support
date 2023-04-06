package icu.windea.pls.extension.diagram.extras

import com.intellij.diagram.*
import com.intellij.diagram.components.*
import com.intellij.diagram.extras.custom.*
import com.intellij.openapi.graph.view.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.extension.diagram.components.*
import javax.swing.*

abstract class DiagramExtrasEx : CommonDiagramExtras<PsiElement>() {
    override fun createNodeComponent(node: DiagramNode<PsiElement>, builder: DiagramBuilder, nodeRealizer: NodeRealizer, wrapper: JPanel): JComponent {
        //允许添加自定义的组件
        val component = super.createNodeComponent(node, builder, nodeRealizer, wrapper)
        if(component is DiagramNodeContainer) {
            val nodeBodyComponent = component.nodeBodyComponent
            nodeBodyComponent.setFieldValue("myItemComponent", DiagramNodeItemComponentEx())
        }
        return component
    }
}
package icu.windea.pls.extension.diagram.extras

import com.intellij.diagram.DiagramBuilder
import com.intellij.diagram.DiagramNode
import com.intellij.diagram.components.DiagramNodeContainer
import com.intellij.diagram.extras.custom.CommonDiagramExtras
import com.intellij.openapi.graph.view.NodeRealizer
import com.intellij.psi.PsiElement
import icu.windea.pls.extension.diagram.components.DiagramNodeItemComponentEx
import icu.windea.pls.extension.diagram.itemComponent
import javax.swing.JComponent
import javax.swing.JPanel

abstract class DiagramExtrasEx : CommonDiagramExtras<PsiElement>() {
    override fun createNodeComponent(node: DiagramNode<PsiElement>, builder: DiagramBuilder, nodeRealizer: NodeRealizer, wrapper: JPanel): JComponent {
        //允许添加自定义的组件
        val component = super.createNodeComponent(node, builder, nodeRealizer, wrapper)
        if (component is DiagramNodeContainer) {
            val nodeBodyComponent = component.nodeBodyComponent
            nodeBodyComponent.itemComponent = DiagramNodeItemComponentEx()
        }
        return component
    }
}//

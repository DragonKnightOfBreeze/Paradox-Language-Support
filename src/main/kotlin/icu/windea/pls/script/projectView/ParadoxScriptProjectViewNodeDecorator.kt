package icu.windea.pls.script.projectView

import com.intellij.ide.projectView.*
import com.intellij.packageDependencies.ui.*
import com.intellij.psi.PsiFile
import com.intellij.ui.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

/**
 * 对脚本文件在项目视图中显示额外的信息。
 *
 * * 对于本身是定义的脚本文件，显示定义的额外信息（定义的名称、类型）
 */
class ParadoxScriptProjectViewNodeDecorator : ProjectViewNodeDecorator {
	override fun decorate(node: ProjectViewNode<*>, data: PresentationData) {
		val virtualFile = node.virtualFile
		val fileType = virtualFile?.fileInfo?.fileType
		if(fileType == ParadoxFileType.ParadoxScript) {
			val psiFile = virtualFile.toPsiFile<PsiFile>(node.project)
			if(psiFile is ParadoxScriptFile) {
				val definitionInfo = psiFile.definitionInfo
				if(definitionInfo != null) {
					node.presentation.locationString = psiFile.presentation.locationString
				}
			}
		}
	}
	
	override fun decorate(node: PackageDependenciesNode, cellRenderer: ColoredTreeCellRenderer) {
		//do nothing
	}
}
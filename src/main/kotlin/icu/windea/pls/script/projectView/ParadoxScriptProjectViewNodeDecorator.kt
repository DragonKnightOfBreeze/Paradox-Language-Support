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
			val file = virtualFile.toPsiFile<PsiFile>(node.project)
			if(file is ParadoxScriptFile) {
				node.presentation.locationString = getLocationString(file)
			}
		}
	}
	
	private fun getLocationString(file: ParadoxScriptFile): String? {
		//如果文件名是descriptor.mod（不区分大小写），这里不要显示定义信息
		val element = file
		if(element.name.equals(descriptorFileName, true)) return null
		val definitionInfo = element.definitionInfo ?: return null
		//如果definitionName和rootKey相同，则省略definitionName
		val name = definitionInfo.name
		val typesText = definitionInfo.typeText
		if(name.equals(definitionInfo.rootKey, true)){
			return ": $typesText"
		} else {
			return "$name: $typesText"
		}
	}
	
	override fun decorate(node: PackageDependenciesNode, cellRenderer: ColoredTreeCellRenderer) {
		//do nothing
	}
}
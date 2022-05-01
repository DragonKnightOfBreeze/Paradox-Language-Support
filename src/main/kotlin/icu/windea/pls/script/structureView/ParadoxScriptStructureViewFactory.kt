package icu.windea.pls.script.structureView

import com.intellij.ide.structureView.*
import com.intellij.lang.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*

/**
 * 对脚本文件提供结构视图的支持。
 * 
 * * 显示变量、属性的名字，值的截断后文本。
 * * 显示变量的名字和额外信息（变量的值）。
 * * 显示定义的额外信息（定义的名称、类型），覆盖属性的，并使用定义的图标（对于本身是定义的脚本文件也显示额外信息）。
 */
class ParadoxScriptStructureViewFactory : PsiStructureViewFactory {
	override fun getStructureViewBuilder(psiFile: PsiFile): StructureViewBuilder {
		return object : TreeBasedStructureViewBuilder() {
			override fun createStructureViewModel(editor: Editor?): StructureViewModel {
				return ParadoxScriptStructureViewModel(editor, psiFile)
			}
		}
	}
}

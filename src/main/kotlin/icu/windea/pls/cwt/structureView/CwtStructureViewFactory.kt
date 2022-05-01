package icu.windea.pls.cwt.structureView

import com.intellij.ide.structureView.*
import com.intellij.lang.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*

/**
 * 对CWT文件提供结构视图的支持。
 *
 * * 显示属性的名字，值的截断后文本。
 */
class CwtStructureViewFactory : PsiStructureViewFactory {
	override fun getStructureViewBuilder(psiFile: PsiFile): StructureViewBuilder {
		return object : TreeBasedStructureViewBuilder() {
			override fun createStructureViewModel(editor: Editor?): StructureViewModel {
				return CwtStructureViewModel(editor, psiFile)
			}
		}
	}
}


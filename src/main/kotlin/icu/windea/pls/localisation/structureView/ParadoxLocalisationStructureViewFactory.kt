package icu.windea.pls.localisation.structureView

import com.intellij.ide.structureView.*
import com.intellij.lang.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*

/**
 * 对本地化文件提供结构视图的支持。
 *
 * * 显示属性的键名。
 * * 显示本地化的键名，覆盖属性的。
 */
class ParadoxLocalisationStructureViewFactory : PsiStructureViewFactory {
	override fun getStructureViewBuilder(psiFile: PsiFile): StructureViewBuilder {
		return object : TreeBasedStructureViewBuilder() {
			override fun createStructureViewModel(editor: Editor?): StructureViewModel {
				return ParadoxLocalisationStructureViewModel(editor, psiFile)
			}
		}
	}
}


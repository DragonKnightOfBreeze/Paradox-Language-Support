package icu.windea.pls.localisation.structureView

import com.intellij.ide.structureView.StructureViewBuilder
import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder
import com.intellij.lang.PsiStructureViewFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile

/**
 * 对本地化文件提供结构视图的支持。
 *
 * * 显示语言环境的ID和描述，作为属性和本地化的父节点。
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

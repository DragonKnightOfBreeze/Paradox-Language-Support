package icu.windea.pls.csv.structureView

import com.intellij.ide.structureView.*
import com.intellij.lang.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*

/**
 * 对CSV文件提供结构视图的支持。
 *
 * * 显示行的信息。
 * * 显示列的文本。
 */
class ParadoxCsvStructureViewFactory : PsiStructureViewFactory {
    override fun getStructureViewBuilder(psiFile: PsiFile): StructureViewBuilder {
        return object : TreeBasedStructureViewBuilder() {
            override fun createStructureViewModel(editor: Editor?): StructureViewModel {
                return ParadoxCsvStructureViewModel(editor, psiFile)
            }
        }
    }
}

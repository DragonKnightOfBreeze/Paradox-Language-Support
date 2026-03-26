package icu.windea.pls.lang.search.implementation

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.DefinitionsScopedSearch
import com.intellij.util.Processor
import com.intellij.util.QueryExecutor
import icu.windea.pls.core.orNull
import icu.windea.pls.core.runSmartReadAction
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.search.selector.withSearchScope

/**
 * 文件的实现的查询。加入所有作用域内的同路径的文件。
 */
class ParadoxFileImplementationsSearch : QueryExecutor<PsiElement, DefinitionsScopedSearch.SearchParameters> {
    override fun execute(queryParameters: DefinitionsScopedSearch.SearchParameters, consumer: Processor<in PsiElement>): Boolean {
        val project = queryParameters.project
        val sourceElement = queryParameters.element
        if (sourceElement !is PsiFile) return true

        runSmartReadAction(project, inSmartMode = true) action@{
            val fileInfo = sourceElement.fileInfo ?: return@action true
            val path = fileInfo.path.path.orNull() ?: return@action true
            // 这里不进行排序
            val selector = selector(project, sourceElement).file()
                .withSearchScope(GlobalSearchScope.allScope(project)) // 使用全部作用域
            val consumer = Processor<VirtualFile> { consumer.process(it.toPsiFile(project)) }
            ParadoxFilePathSearch.search(path, null, selector).forEach(consumer)
        }
        return true
    }
}

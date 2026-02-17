package icu.windea.pls.lang.search.implementation

import com.intellij.openapi.application.ReadAction
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.DefinitionsScopedSearch
import com.intellij.util.Processor
import com.intellij.util.QueryExecutor
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.search.selector.withSearchScope
import java.util.concurrent.Callable

/**
 * 文件的实现的查询。加入所有作用域内的同路径的文件。
 */
class ParadoxFileImplementationsSearch : QueryExecutor<PsiElement, DefinitionsScopedSearch.SearchParameters> {
    override fun execute(queryParameters: DefinitionsScopedSearch.SearchParameters, consumer: Processor<in PsiElement>): Boolean {
        // 得到解析后的 PSI 元素
        val sourceElement = queryParameters.element
        if (sourceElement !is PsiFile) return true
        val fileInfo = sourceElement.fileInfo ?: return true
        val path = fileInfo.path.path
        if (path.isEmpty()) return true
        val project = queryParameters.project
        val task = Callable {
            // 这里不进行排序
            val selector = selector(project, sourceElement).file()
                .withSearchScope(GlobalSearchScope.allScope(project)) // 使用全部作用域
            val query = ParadoxFilePathSearch.search(path, null, selector)
            query.forEach(Processor { consumer.process(it.toPsiFile(project)) })
        }
        ReadAction.nonBlocking(task).inSmartMode(project).executeSynchronously()
        return true
    }
}

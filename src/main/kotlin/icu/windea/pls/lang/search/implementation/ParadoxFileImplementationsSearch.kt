package icu.windea.pls.lang.search.implementation

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*

/**
 * 文件的实现的查询。加入所有作用域内的同路径的文件。
 */
class ParadoxFileImplementationsSearch : QueryExecutor<PsiElement, DefinitionsScopedSearch.SearchParameters> {
    override fun execute(queryParameters: DefinitionsScopedSearch.SearchParameters, consumer: Processor<in PsiElement>): Boolean {
        //得到解析后的PSI元素
        val sourceElement = queryParameters.element
        if (sourceElement !is PsiFile) return true
        val fileInfo = sourceElement.fileInfo ?: return true
        val path = fileInfo.path.path
        if (path.isEmpty()) return true
        val project = queryParameters.project
        ReadAction.nonBlocking<Unit> {
            //这里不进行排序
            val selector = selector(project, sourceElement).file()
                .withSearchScope(GlobalSearchScope.allScope(project)) //使用全部作用域
            ParadoxFilePathSearch.search(path, null, selector).forEach(Processor {
                consumer.process(it.toPsiFile(project))
            })
        }.inSmartMode(project).executeSynchronously()
        return true
    }
}

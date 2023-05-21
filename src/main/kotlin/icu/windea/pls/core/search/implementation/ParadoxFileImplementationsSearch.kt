package icu.windea.pls.core.search.implementation

import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*

/**
 * 文件的实现的查询。加入所有作用域内的同路径的文件。
 */
class ParadoxFileImplementationsSearch : QueryExecutor<PsiElement, DefinitionsScopedSearch.SearchParameters> {
    override fun execute(queryParameters: DefinitionsScopedSearch.SearchParameters, consumer: Processor<in PsiElement>): Boolean {
        //得到解析后的PSI元素
        val sourceElement = queryParameters.element
        if(sourceElement !is PsiFile) return true
        val fileInfo = sourceElement.fileInfo
        val path = fileInfo?.path
        if(path == null || path.isEmpty()) return true
        val project = queryParameters.project
        DumbService.getInstance(project).runReadActionInSmartMode {
            //这里不进行排序
            val selector = fileSelector(project, sourceElement)
                .withSearchScope(GlobalSearchScope.allScope(project)) //使用全部作用域
            ParadoxFilePathSearch.search(path.path, null, selector).forEach(Processor {
                consumer.process(it.toPsiFile(project))
            })
        }
        return true
    }
}
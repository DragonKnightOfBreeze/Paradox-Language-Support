package icu.windea.pls.lang.search.usage

import com.intellij.openapi.application.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.ep.expression.*
import icu.windea.pls.lang.*
import kotlin.experimental.*

/**
 * （游戏或模组）文件的使用的查询。
 */
class ParadoxFileUsagesSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {
    override fun processQuery(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>) {
        //0.7.8 这里不能仅仅用fileName去查找，需要基于CWT规则文件判断
        val target = queryParameters.elementToSearch
        if (target !is PsiFile) return
        val fileInfo = target.fileInfo
        if (fileInfo == null) return
        val gameType = fileInfo.rootInfo.gameType
        val filePath = fileInfo.path.path
        val project = queryParameters.project
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        val extraWords = getExtraWords(target, filePath, configGroup)
        if (extraWords.isEmpty()) return

        //这里不能直接使用target.useScope，否则文件高亮会出现问题
        val useScope = queryParameters.effectiveSearchScope
        val searchContext = UsageSearchContext.IN_CODE or UsageSearchContext.IN_STRINGS or UsageSearchContext.IN_COMMENTS
        for (extraWord in extraWords) {
            queryParameters.optimizer.searchWord(extraWord, useScope, searchContext, true, target)
        }
    }

    private fun getExtraWords(target: PsiFile, filePath: String, configGroup: CwtConfigGroup): Set<String> {
        val extraWords = mutableSetOf<String>()
        configGroup.filePathExpressions.forEach { configExpression ->
            val name = ParadoxPathReferenceExpressionSupport.get(configExpression)?.extract(configExpression, target, filePath)?.orNull()
            if (name != null) extraWords.add(name)
        }
        return extraWords
    }
}

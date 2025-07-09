package icu.windea.pls.ai.services

import icu.windea.pls.*
import icu.windea.pls.lang.util.manipulators.*
import kotlinx.coroutines.flow.*
import kotlin.contracts.*

abstract class PlsAiManipulateLocalisationService : PlsAiService {
    @OptIn(ExperimentalContracts::class)
    fun checkResultFlow(resultFlow: Flow<ParadoxLocalisationResult>?) {
        contract {
            returns() implies (resultFlow != null)
        }
        if (resultFlow == null) { //resultFlow返回null，这意味着AI设置不合法，例如API KEY未填写（但不包括API kEY已填写但正确的情况）
            throw IllegalStateException(PlsBundle.message("intention.localisation.error.1"))
        }
    }

    fun checkOutputData(snippets: ParadoxLocalisationContext, data: ParadoxLocalisationResult) {
        if (snippets.key.isEmpty()) { //输出内容的格式不正确
            throw IllegalStateException(PlsBundle.message("intention.localisation.error.2"))
        }
        if (snippets.key != data.key) { //不期望的结果，直接报错，中断收集
            throw IllegalStateException(PlsBundle.message("intention.localisation.error.3", snippets.key, data.key))
        }
    }
}

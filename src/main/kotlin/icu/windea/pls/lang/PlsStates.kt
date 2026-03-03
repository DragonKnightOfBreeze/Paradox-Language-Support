package icu.windea.pls.lang

import icu.windea.pls.lang.resolve.CwtConfigContext
import icu.windea.pls.model.ParadoxDefinitionInfo
import java.util.*

object PlsStates {
    /**
     * 用于标记是否正在构建合并索引。
     *
     * @see icu.windea.pls.lang.index.ParadoxMergedIndex
     */
    val processMergedIndex = ThreadLocal<Boolean>()

    /**
     * 用于标记是否正在构建合并索引，并且正在解析引用。
     *
     * @see icu.windea.pls.lang.index.ParadoxMergedIndex
     */
    val resolveForMergedIndex = ThreadLocal<Boolean>()

    /**
     * 用于标记是否正在解析内部规则。
     *
     * @see icu.windea.pls.ep.config.configGroup.CwtFileBasedConfigGroupDataProvider
     */
    val resolveForInternalConfigs = ThreadLocal<Boolean>()

    /**
     * 用于标记是否允许不完整的复杂脚本表达式（用于兼容代码补全）。
     *
     * @see icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
     */
    val incompleteComplexExpression = ThreadLocal<Boolean>()

    /**
     * 用于得到正在处理的定义信息的堆栈。
     *
     * @see icu.windea.pls.lang.index.ParadoxMergedIndex
     */
    val procssingDefinitionInfoStack = ThreadLocal<ArrayDeque<ParadoxDefinitionInfo>>()

    /**
     * 用于得到正在解析的规则表达式的堆栈。
     *
     * @see icu.windea.pls.lang.resolve.ParadoxConfigService.getConfigsForConfigContext
     */
    val resolvingConfigContextStack = ThreadLocal<ArrayDeque<CwtConfigContext>>()
}

package icu.windea.pls.lang

import icu.windea.pls.lang.match.ParadoxMatchService
import icu.windea.pls.lang.resolve.CwtConfigContext
import icu.windea.pls.lang.resolve.ParadoxConfigService
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import java.util.*

object ParadoxThreadContext {
    /**
     * 得到正在解析的规则上下文的堆栈。
     *
     * @see ParadoxConfigService.getConfigsForConfigContext
     */
    val resolvingConfigContextStack = ThreadLocal<ArrayDeque<CwtConfigContext>>()

    /**
     * 得到正在解析的规则上下文。
     *
     * @see ParadoxMatchService.optimize
     */
    val resolvingConfigContext get() = resolvingConfigContextStack.get()?.peekLast()

    /**
     * 标记是否允许不完整的复杂脚本表达式（用于兼容代码补全）。
     *
     * @see ParadoxComplexExpression
     */
    val incompleteComplexExpression = ThreadLocal<Boolean>()
}

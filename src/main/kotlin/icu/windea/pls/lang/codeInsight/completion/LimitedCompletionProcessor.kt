package icu.windea.pls.lang.codeInsight.completion

import com.intellij.openapi.util.registry.*
import com.intellij.util.*

/**
 * 进行代码提示时，确保加入的提示项不超过`ide.completion.variant.limit`指定的上限。
 * 
 * 用于优化代码提示的性能。
 */
class LimitedCompletionProcessor<T>(
    private val processor: Processor<T>
): Processor<T> {
    private val limit: Int = Registry.intValue("ide.completion.variant.limit")
    @Volatile private var count = 0
    
    override fun process(e: T): Boolean {
        if(!processor.process(e)) return false
        if(++count > limit) return false
        return true
    }
}
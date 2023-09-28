package icu.windea.pls.lang.expressionIndex

import com.intellij.openapi.extensions.*
import icu.windea.pls.model.*
import icu.windea.pls.model.expression.*
import java.io.*

/**
 * 提供对需要索引的表达式信息的支持。
 */
interface ParadoxExpressionIndexSupport<T : ParadoxExpressionInfo> {
    fun id(): Byte
    
    fun type(): Class<T>
    
    fun compress(value: List<T>): List<T>
    
    fun writeData(storage: DataOutput, info: T, previousInfo: T?, gameType: ParadoxGameType)
    
    fun readData(storage: DataInput, previousInfo: T?, gameType: ParadoxGameType): T
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxExpressionIndexSupport<*>>("icu.windea.pls.expressionIndexSupport")
    }
}
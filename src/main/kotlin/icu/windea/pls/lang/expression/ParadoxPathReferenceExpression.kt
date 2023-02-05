package icu.windea.pls.lang.expression

import com.intellij.openapi.extensions.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.index.*

/**
 * 路径引用表达式。
 *
 * 用于实现如何匹配、解析脚本文件中使用的路径表达式，以及如何基于文件路径索引进行代码补全等功能。
 *
 * @see ParadoxFilePathIndex
 */
interface ParadoxPathReferenceExpression {
    fun supports(configExpression: CwtDataExpression): Boolean
    
    /**
     * 判断指定的文件路径表达式是否匹配另一个相对于游戏或模组目录根路径的路径。
     * @param configExpression 对应的CWT规则表达式。拥有数种写法的文件路径表达式。
     * @param ignoreCase 匹配时是否需要忽略大小写。
     */
    fun matches(configExpression: CwtDataExpression, filePath: String, ignoreCase: Boolean = true): Boolean
    
    /**
     * 根据指定的文件路径表达式，从精确路径中提取出需要的作为值的字符串。即脚本文件中使用的路径表达式。
     * @param configExpression 对应的CWT规则表达式。拥有数种写法的文件路径表达式。
     * @param ignoreCase 匹配时是否需要忽略大小写。
     */
    fun extract(configExpression: CwtDataExpression, filePath: String, ignoreCase: Boolean = true): String?
    
    /**
     * 解析指定的文件路径表达式，得到文件路径。
     * @param configExpression 对应的CWT规则表达式。拥有数种写法的文件路径表达式。
     * @param pathReference 作为值的字符串。即脚本文件中使用的路径表达式。
     */
    fun resolvePath(configExpression: CwtDataExpression, pathReference: String): String?
    
    /**
     * 解析指定的文件路径表达式，得到文件名。
     * @param configExpression 对应的CWT规则表达式。拥有数种写法的文件路径表达式。
     * @param pathReference 作为值的字符串。即脚本文件中使用的路径表达式。
     */
    fun resolveFileName(configExpression: CwtDataExpression, pathReference: String): String
    
    fun getUnresolvedMessage(configExpression: CwtDataExpression, pathReference: String): String
    
    companion object INSTANCE {
        @JvmStatic
        val EP_NAME = ExtensionPointName.create<ParadoxPathReferenceExpression>("icu.windea.pls.paradoxPathReferenceExpression")
        
        fun get(configExpression: CwtDataExpression): ParadoxPathReferenceExpression? {
            return EP_NAME.extensions.find { it.supports(configExpression) }
        }
    }
}




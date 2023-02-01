package icu.windea.pls.config.core

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.core.config.*
import icu.windea.pls.config.core.component.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*
import java.util.*

/**
 * 用于处理元素路径。
 */
object ParadoxElementPathHandler {
    /**
     * 解析指定定义相对于所属文件的属性路径。
     */
    @JvmStatic
    fun getFromFile(element: PsiElement, maxDepth: Int = -1): ParadoxElementPath? {
        if(element is ParadoxScriptFile) return EmptyParadoxElementPath
        return resolveFromFile(element, maxDepth)
    }
    
    private fun resolveFromFile(element: PsiElement, maxDepth: Int): ParadoxElementPath? {
        var current: PsiElement = element
        var depth = 0
        val originalSubPaths = LinkedList<String>()
        while(current !is PsiFile) {
            when {
                current is ParadoxScriptProperty -> {
                    originalSubPaths.addFirst(current.originalPathName) //这里需要使用原始文本
                    depth++
                }
                current is ParadoxScriptValue && current.isBlockValue() -> {
                    originalSubPaths.addFirst("-")
                    depth++
                }
            }
            //如果发现深度超出指定的最大深度，则直接返回null
            if(maxDepth != -1 && maxDepth < depth) return null
            current = current.parent ?: break
        }
        return ParadoxElementPath.resolve(originalSubPaths)
    }
    
    /**
     * 解析指定元素相对于所属定义的属性路径。
     */
    @JvmStatic
    fun getFromDefinitionWithDefinition(element: PsiElement, allowDefinitionSelf: Boolean): Tuple2<ParadoxElementPath, ParadoxScriptDefinitionElement>? {
        return resolveFromDefinitionWithDefinition(element, allowDefinitionSelf)
    }
    
    private fun resolveFromDefinitionWithDefinition(element: PsiElement, allowDefinitionSelf: Boolean): Pair<ParadoxElementPath, ParadoxScriptDefinitionElement>? {
        var current: PsiElement = element
        var depth = 0
        val originalSubPaths = LinkedList<String>()
        var definition: ParadoxScriptDefinitionElement? = null
        var flag = allowDefinitionSelf
        while(current !is PsiDirectory) { //这里的上限应当是null或PsiDirectory，不能是PsiFile，因为它也可能是定义
            when {
                current is ParadoxScriptDefinitionElement -> {
                    if(flag) {
                        val definitionInfo = current.definitionInfo
                        if(definitionInfo != null) {
                            definition = current
                            break
                        }
                    } else {
                        flag = true
                    }
                    if(current !is PsiFile) {
                        originalSubPaths.addFirst(current.originalPathName) //这里需要使用原始文本
                    }
                    depth++
                }
                current is ParadoxScriptValue && current.isBlockValue() -> {
                    originalSubPaths.addFirst("-")
                    depth++
                }
            }
            current = linkElement(current) ?: current
            current = current.parent ?: break
        }
        if(definition == null) return null //如果未找到所属的definition，则直接返回null
        return ParadoxElementPath.resolve(originalSubPaths) to definition
    }
    
    private fun linkElement(element: PsiElement): PsiElement? {
        return ParadoxElementLinker.EP_NAME.extensions.firstNotNullOfOrNull { it.linkElement(element) }
    }
}

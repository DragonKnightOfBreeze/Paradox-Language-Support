package icu.windea.pls.lang

import com.intellij.lang.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.inline.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import java.util.*

/**
 * 用于处理元素路径。
 *
 * @see ParadoxElementPath
 */
object ParadoxElementPathHandler {
    /**
     * 解析指定定义相对于所属文件的属性路径。
     */
    fun getFromFile(element: PsiElement, maxDepth: Int = -1): ParadoxElementPath? {
        return resolveFromFile(element, maxDepth)
    }
    
    private fun resolveFromFile(element: PsiElement, maxDepth: Int): ParadoxElementPath? {
        var current: PsiElement = element
        var depth = 0
        val originalSubPaths = LinkedList<String>()
        while(current !is PsiFile) {
            when {
                current is ParadoxScriptProperty -> {
                    val p = current.propertyKey.text
                    if(p.isParameterizedExpression()) return null
                    originalSubPaths.addFirst(p)
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
        if(current is PsiFile) {
            val virtualFile = selectFile(current)
            val injectedElementPathPrefix = virtualFile?.getUserData(PlsKeys.injectedElementPathPrefixKey)
            if(injectedElementPathPrefix != null && injectedElementPathPrefix.isNotEmpty()) {
                originalSubPaths.addAll(0, injectedElementPathPrefix.subPaths)
            }
        }
        return ParadoxElementPath.resolve(originalSubPaths)
    }
    
    /**
     * 解析指定定义相对于所属文件的属性路径。
     */
    fun getFromFile(node: LighterASTNode, tree: LighterAST, file: VirtualFile, maxDepth: Int = -1): ParadoxElementPath? {
        return resolveFromFile(node, tree, file, maxDepth)
    }
    
    private fun resolveFromFile(node: LighterASTNode, tree: LighterAST, file: VirtualFile, maxDepth: Int): ParadoxElementPath? {
        var current: LighterASTNode = node
        var depth = 0
        val originalSubPaths = LinkedList<String>()
        while(current !is PsiFile) {
            when {
                current.tokenType == PROPERTY -> {
                    val p = current.firstChild(tree, PROPERTY_KEY)
                        ?.firstChild(tree, PROPERTY_KEY_TOKEN)
                        ?.internNode(tree)?.toString() ?: return null
                    originalSubPaths.addFirst(p)
                    depth++
                }
                ParadoxScriptTokenSets.VALUES.contains(current.tokenType) && tree.getParent(current)?.tokenType == BLOCK -> {
                    originalSubPaths.addFirst("-")
                    depth++
                }
            }
            //如果发现深度超出指定的最大深度，则直接返回null
            if(maxDepth != -1 && maxDepth < depth) return null
            current = tree.getParent(current) ?: break
        }
        if(current.tokenType == ParadoxScriptStubElementTypes.FILE) {
            val virtualFile = file
            val injectedElementPathPrefix = virtualFile.getUserData(PlsKeys.injectedElementPathPrefixKey)
            if(injectedElementPathPrefix != null && injectedElementPathPrefix.isNotEmpty()) {
                originalSubPaths.addAll(0, injectedElementPathPrefix.subPaths)
            }
        }
        return ParadoxElementPath.resolve(originalSubPaths)
    }
    
    /**
     * 解析指定元素相对于所属定义的属性路径。
     */
    fun getFromDefinitionWithDefinition(element: PsiElement, allowDefinition: Boolean): Tuple2<ParadoxElementPath, ParadoxScriptDefinitionElement>? {
        return resolveFromDefinitionWithDefinition(element, allowDefinition)
    }
    
    private fun resolveFromDefinitionWithDefinition(element: PsiElement, allowDefinition: Boolean): Pair<ParadoxElementPath, ParadoxScriptDefinitionElement>? {
        var current: PsiElement = element
        val originalSubPaths = LinkedList<String>()
        var definition: ParadoxScriptDefinitionElement? = null
        var flag = allowDefinition
        val inlineStack = LinkedList<String>()
        while(current !is PsiDirectory) { //这里的上限应当是null或PsiDirectory，不能是PsiFile，因为它也可能是定义
            if(current is ParadoxScriptMemberElement) {
                val linked = ParadoxScriptMemberElementInlineSupport.linkElement(current, inlineStack)
                if(linked != null) {
                    current = linked.parent ?: break
                    continue
                }
            }
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
                    val p = when {
                        current is ParadoxScriptProperty -> current.propertyKey.text
                        current is ParadoxScriptFile -> current.name.substringBeforeLast('.')
                        else -> current.name
                    }
                    originalSubPaths.addFirst(p)
                }
                current is ParadoxScriptValue && current.isBlockValue() -> {
                    originalSubPaths.addFirst("-")
                }
            }
            current = current.parent ?: break
        }
        if(definition == null) return null //如果未找到所属的definition，则直接返回null
        return ParadoxElementPath.resolve(originalSubPaths) to definition
    }
}

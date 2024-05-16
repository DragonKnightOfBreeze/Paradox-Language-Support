package icu.windea.pls.lang.util

import com.intellij.lang.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.path.*
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
    fun get(element: PsiElement, maxDepth: Int = -1): ParadoxElementPath? {
        var current: PsiElement = element
        var depth = 0
        val originalSubPaths = LinkedList<String>()
        while(current !is PsiFile) {
            when {
                current is ParadoxScriptProperty -> {
                    val p = current.propertyKey.text
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
            val injectedElementPathPrefix = virtualFile?.getUserData(PlsKeys.injectedElementPathPrefix)
            if(injectedElementPathPrefix != null && injectedElementPathPrefix.isNotEmpty()) {
                originalSubPaths.addAll(0, injectedElementPathPrefix.subPaths.map { it.rawSubPath })
            }
        }
        return ParadoxElementPath.resolve(originalSubPaths)
    }
    
    /**
     * 解析指定定义相对于所属文件的属性路径。
     */
    fun get(node: LighterASTNode, tree: LighterAST, file: VirtualFile, maxDepth: Int = -1): ParadoxElementPath? {
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
            val injectedElementPathPrefix = virtualFile.getUserData(PlsKeys.injectedElementPathPrefix)
            if(injectedElementPathPrefix != null && injectedElementPathPrefix.isNotEmpty()) {
                originalSubPaths.addAll(0, injectedElementPathPrefix.subPaths.map { it.rawSubPath })
            }
        }
        return ParadoxElementPath.resolve(originalSubPaths)
    }
}

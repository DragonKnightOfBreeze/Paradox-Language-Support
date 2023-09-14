package icu.windea.pls.core.index.hierarchy

import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.index.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.model.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import java.util.*

/**
 * 用于索引定义声明中的定义引用、参数引用、本地化参数引用等。
 *
 * * 这个索引兼容需要内联的情况（此时使用懒加载的索引）。
 * * 这个索引可能不会记录数据在文件中的位置。
 * * 这个索引可能不会保存同一文件中的重复数据。
 */
abstract class ParadoxDefinitionHierarchyIndex<T> : ParadoxFileBasedIndex<List<T>>() {
    companion object {
        private val markKey = Key.create<Boolean>("paradox.definition.hierarchy.index.mark")
    }
    
    override fun indexData(file: PsiFile, fileData: MutableMap<String, List<T>>) {
        file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            private val definitionInfoStack = LinkedList<ParadoxDefinitionInfo>()
            
            override fun visitElement(element: PsiElement) {
                if(element is ParadoxScriptDefinitionElement) {
                    val definitionInfo = element.definitionInfo
                    if(definitionInfo != null) {
                        element.putUserData(markKey, true)
                        definitionInfoStack.addLast(definitionInfo)
                    }
                }
                
                if(definitionInfoStack.isNotEmpty()) {
                    //这里element作为定义的引用时也可能是ParadoxScriptInt，目前不需要考虑这种情况，因此忽略
                    if(element is ParadoxScriptStringExpressionElement && element.isExpression()) {
                        doIndexData(element, fileData)
                    }
                }
                if(element.isExpressionOrMemberContext()) super.visitElement(element)
            }
            
            override fun elementFinished(element: PsiElement) {
                if(element.getUserData(markKey) == true) {
                    element.putUserData(markKey, null)
                    definitionInfoStack.removeLast()
                }
            }
            
            private fun doIndexData(element: ParadoxScriptStringExpressionElement, fileData: MutableMap<String, List<T>>) {
                val matchOptions = CwtConfigMatcher.Options.SkipIndex or CwtConfigMatcher.Options.SkipScope
                val configs = CwtConfigHandler.getConfigs(element, matchOptions = matchOptions)
                if(configs.isEmpty()) return
                val definitionInfo = definitionInfoStack.peekLast() ?: return
                configs.forEachFast { config ->
                    indexData(element, config, definitionInfo, fileData)
                }
            }
        })
        
        afterIndexData(fileData)
    }
    
    protected abstract fun indexData(element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>, definitionInfo: ParadoxDefinitionInfo, fileData: MutableMap<String, List<T>>)
    
    protected open fun afterIndexData(fileData: MutableMap<String, List<T>>) {}
    
    override fun filterFile(file: VirtualFile): Boolean {
        val fileType = file.fileType
        if(fileType != ParadoxScriptFileType) return false
        if(file.fileInfo == null) return false
        return true
    }
    
    override fun useLazyIndex(file: VirtualFile): Boolean {
        if(ParadoxFileManager.isInjectedFile(file)) return true
        if(ParadoxInlineScriptHandler.getInlineScriptExpression(file) != null) return true
        return false
    }
}

package icu.windea.pls.core.index

import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.indexing.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expressionIndex.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.expression.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import java.io.*

private val NAME = ID.create<String, List<ParadoxExpressionInfo>>("paradox.expression.index")
private const val VERSION = 42 //1.1.12

private val markerKey = createKey<Boolean>("paradox.expression.index.marker")

/**
 * 用于基于文件层级索引各种表达式信息。
 *
 * * 这个索引兼容需要内联的情况（此时使用懒加载的索引）。
 * 
 * @see ParadoxExpressionInfo
 * @see ParadoxExpressionIndexSupport
 */
class ParadoxExpressionIndex : ParadoxFileBasedIndex<List<ParadoxExpressionInfo>>() {
    override fun getName() = NAME
    
    override fun getVersion() = VERSION
    
    override fun indexData(file: PsiFile, fileData: MutableMap<String, List<ParadoxExpressionInfo>>) {
        when(file) {
            is ParadoxScriptFile -> indexDataForScriptFile(file, fileData)
            is ParadoxLocalisationFile -> indexDataForLocalisationFile(file, fileData)
        }
        postIndexData(file, fileData)
        compressData(fileData)
    }
    
    private fun indexDataForScriptFile(file: ParadoxScriptFile, fileData: MutableMap<String, List<ParadoxExpressionInfo>>) {
        val extensionList = ParadoxExpressionIndexSupport.EP_NAME.extensionList
        val definitionInfoStack = ArrayDeque<ParadoxDefinitionInfo>()
        file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                extensionList.forEachFast { ep ->
                    ep.indexScriptElement(element, fileData)
                }
                
                if(element is ParadoxScriptDefinitionElement) {
                    val definitionInfo = element.definitionInfo
                    if(definitionInfo != null) {
                        element.putUserData(markerKey, true)
                        definitionInfoStack.addLast(definitionInfo)
                    }
                }
                
                run {
                    if(definitionInfoStack.isEmpty()) return@run
                    if(element is ParadoxScriptStringExpressionElement && element.isExpression()) {
                        ProgressManager.checkCanceled()
                        val matchOptions = CwtConfigMatcher.Options.SkipIndex or CwtConfigMatcher.Options.SkipScope
                        val configs = CwtConfigHandler.getConfigs(element, matchOptions = matchOptions)
                        if(configs.isEmpty()) return@run
                        val definitionInfo = definitionInfoStack.lastOrNull() ?: return@run
                        extensionList.forEachFast { ep ->
                            configs.forEachFast { config ->
                                ep.indexScriptExpression(element, config, definitionInfo, fileData)
                            }
                        }
                    }
                }
                
                super.visitElement(element)
            }
            
            override fun elementFinished(element: PsiElement) {
                if(element.getUserData(markerKey) == true) {
                    element.putUserData(markerKey, null)
                    definitionInfoStack.removeLastOrNull()
                }
            }
        })
    }
    
    private fun indexDataForLocalisationFile(file: ParadoxLocalisationFile, fileData: MutableMap<String, List<ParadoxExpressionInfo>>) {
        val extensionList = ParadoxExpressionIndexSupport.EP_NAME.extensionList
        file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if(element is ParadoxLocalisationCommandIdentifier) {
                    extensionList.forEachFast f@{ ep -> 
                        ep.indexLocalisationCommandIdentifier(element, fileData)
                    }
                }
                if(element.isRichTextContext()) super.visitElement(element)
            }
        })
    }
    
    private fun postIndexData(file: PsiFile, fileData: MutableMap<String, List<ParadoxExpressionInfo>>) {
        ParadoxExpressionIndexHandler.postIndexData(file, fileData)
    }
    
    private fun compressData(fileData: MutableMap<String, List<ParadoxExpressionInfo>>) {
        if(fileData.isEmpty()) return
        val extensionList = ParadoxExpressionIndexSupport.EP_NAME.extensionList
        fileData.mapValues { (k, v) ->
            val id = k.toByte()
            val support = extensionList.findFast { it.id() == id }
                ?.castOrNull<ParadoxExpressionIndexSupport<ParadoxExpressionInfo>>()
                ?: throw UnsupportedOperationException()
            support.compressData(v)
        }
    }
    
    override fun writeData(storage: DataOutput, value: List<ParadoxExpressionInfo>) {
        val size = value.size
        storage.writeIntFast(size)
        if(value.isEmpty()) return
        
        val type = value.first().javaClass
        val support = ParadoxExpressionIndexSupport.EP_NAME.extensionList.findFast { it.type() == type }
            ?.castOrNull<ParadoxExpressionIndexSupport<ParadoxExpressionInfo>>()
            ?: throw UnsupportedOperationException()
        storage.writeByte(support.id().toInt())
        val gameType = value.first().gameType
        storage.writeByte(gameType.toByte())
        var previousInfo: ParadoxExpressionInfo? = null
        value.forEachFast { info ->
            support.writeData(storage, info, previousInfo, gameType)
            previousInfo = info
        }
    }
    
    override fun readData(storage: DataInput): List<ParadoxExpressionInfo> {
        val size = storage.readIntFast()
        if(size == 0) return emptyList()
        
        val id = storage.readByte()
        val support = ParadoxExpressionIndexSupport.EP_NAME.extensionList.findFast { it.id() == id }
            ?.castOrNull<ParadoxExpressionIndexSupport<ParadoxExpressionInfo>>()
            ?: throw UnsupportedOperationException()
        val gameType = storage.readByte().toGameType()
        var previousInfo: ParadoxExpressionInfo? = null
        return MutableList(size) {
            support.readData(storage, previousInfo, gameType)
                .also { previousInfo = it }
        }
    }
    
    override fun filterFile(file: VirtualFile): Boolean {
        val fileType = file.fileType
        if(fileType != ParadoxScriptFileType && fileType != ParadoxLocalisationFileType) return false
        if(file.fileInfo == null) return false
        return true
    }
    
    override fun useLazyIndex(file: VirtualFile): Boolean {
        if(ParadoxFileManager.isInjectedFile(file)) return true
        if(ParadoxInlineScriptHandler.getInlineScriptExpression(file) != null) return true
        return false
    }
    
    fun <T: ParadoxExpressionInfo> getFileData(file: VirtualFile, project: Project, id: ParadoxExpressionIndexId<T>): List<T> {
        val allFileData = getFileData(file, project)
        return allFileData.get(id.id.toString())?.castOrNull<List<T>>().orEmpty()
    }
}
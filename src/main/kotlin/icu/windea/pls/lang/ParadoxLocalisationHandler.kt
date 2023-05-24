package icu.windea.pls.lang

import com.intellij.lang.*
import com.intellij.openapi.progress.*
import com.intellij.psi.stubs.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.localisation.psi.impl.*

/**
 * 用于处理本地化信息。
 */
@Suppress("unused", "UNUSED_PARAMETER")
object ParadoxLocalisationHandler {
    fun getInfo(element: ParadoxLocalisationProperty): ParadoxLocalisationInfo? {
        //快速判断
        if(runCatching { element.stub }.getOrNull()?.isValid() == false) return null
        //从缓存中获取
        return doGetInfoFromCache(element)
    }
    
    private fun doGetInfoFromCache(element: ParadoxLocalisationProperty): ParadoxLocalisationInfo? {
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedLocalisationInfoKey) {
            ProgressManager.checkCanceled()
            val value = doGetInfo(element)
            CachedValueProvider.Result.create(value, element)
        }
    }
    
    private fun doGetInfo(element: ParadoxLocalisationProperty): ParadoxLocalisationInfo? {
        //首先尝试直接基于stub进行解析
        getInfoFromStub(element)?.let { return it }
        
        val name = element.name
        val file = element.containingFile.originalFile.virtualFile ?: return null
        val category = ParadoxLocalisationCategory.resolve(file) ?: return null
        val gameType = selectGameType(file)
        return ParadoxLocalisationInfo(name, category, gameType)
    }
    
    //stub methods
    
    fun createStub(psi: ParadoxLocalisationProperty, parentStub: StubElement<*>): ParadoxLocalisationStub? {
        val file = selectFile(psi) ?: return null
        val gameType = selectGameType(file) ?: return null
        val name = psi.name
        val category = ParadoxLocalisationCategory.resolve(file) ?: return null
        val locale = selectLocale(file)?.id
        return ParadoxLocalisationStubImpl(parentStub, name, category, locale, gameType)
    }
    
    fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ParadoxLocalisationStub? {
        val psi = parentStub.psi
        val file = selectFile(psi) ?: return null
        val gameType = selectGameType(file) ?: return null
        val name = getNameFromNode(node, tree) ?: return null
        val category = ParadoxLocalisationCategory.resolve(file) ?: return null
        val locale = selectLocale(file)?.id
        return ParadoxLocalisationStubImpl(parentStub, name, category, locale, gameType)
    }
    
    private fun getNameFromNode(node: LighterASTNode, tree: LighterAST): String? {
        return node.firstChild(tree, PROPERTY_KEY)?.firstChild(tree, PROPERTY_KEY_TOKEN)?.internNode(tree)?.toString()
    }
    
    fun shouldCreateStub(node: ASTNode): Boolean {
        return true //just true
    }
    
    fun shouldCreateStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): Boolean {
        return true //just true
    }
    
    fun getInfoFromStub(element: ParadoxLocalisationProperty): ParadoxLocalisationInfo? {
        val stub = runCatching { element.stub }.getOrNull() ?: return null
        //if(!stub.isValid()) return null //这里不用再次判断
        val name = stub.name
        val category = stub.category
        val gameType = stub.gameType ?: return null
        return ParadoxLocalisationInfo(name, category, gameType)
    }
}
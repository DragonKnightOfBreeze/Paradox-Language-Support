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
        //首先尝试直接基于stub进行解析（得到的信息不合法时直接返回null）
        val infoFromStub = getInfoFromStub(element)
        if(infoFromStub != null) {
            return infoFromStub.takeIf { it.isValid() }
        }
        
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
        //这里得到信息可以不合法（即对应的PsiElement并不是一个本地化）
        val stub = runCatching { element.stub }.getOrNull() ?: return null
        val name = stub.name
        val category = stub.category
        val gameType = stub.gameType ?: return null
        return ParadoxLocalisationInfo(name, category, gameType)
    }
}
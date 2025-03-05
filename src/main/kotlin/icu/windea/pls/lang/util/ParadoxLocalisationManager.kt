package icu.windea.pls.lang.util

import com.intellij.lang.*
import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.psi.stubs.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.model.*

/**
 * 用于处理本地化信息。
 */
object ParadoxLocalisationManager {
    fun getInfo(element: ParadoxLocalisationProperty): ParadoxLocalisationInfo? {
        //从缓存中获取
        return doGetInfoFromCache(element)
    }

    private fun doGetInfoFromCache(element: ParadoxLocalisationProperty): ParadoxLocalisationInfo? {
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedLocalisationInfo) {
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
        val gameType = selectGameType(file) ?: return null
        return ParadoxLocalisationInfo(name, category, gameType)
    }

    //stub methods

    fun createStub(psi: ParadoxLocalisationProperty, parentStub: StubElement<*>): ParadoxLocalisationPropertyStub? {
        val file = selectFile(psi) ?: return null
        val gameType = selectGameType(file) ?: return null
        val category = ParadoxLocalisationCategory.resolve(file) ?: return null
        val name = psi.name
        val locale = selectLocale(file)?.id
        return ParadoxLocalisationPropertyStub.Impl(parentStub, name, category, locale, gameType)
    }

    fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ParadoxLocalisationPropertyStub? {
        val psi = parentStub.psi
        val file = selectFile(psi) ?: return null
        val gameType = selectGameType(file) ?: return null
        val category = ParadoxLocalisationCategory.resolve(file) ?: return null
        val name = getNameFromNode(node, tree) ?: return null
        val locale = selectLocale(file)?.id
        return ParadoxLocalisationPropertyStub.Impl(parentStub, name, category, locale, gameType)
    }

    private fun getNameFromNode(node: LighterASTNode, tree: LighterAST): String? {
        return node.firstChild(tree, PROPERTY_KEY)?.firstChild(tree, PROPERTY_KEY_TOKEN)?.internNode(tree)?.toString()
    }

    fun getInfoFromStub(element: ParadoxLocalisationProperty): ParadoxLocalisationInfo? {
        val stub = runReadAction { element.greenStub } ?: return null
        //if(!stub.isValid()) return null //这里不用再次判断
        val name = stub.name
        val category = stub.category
        val gameType = stub.gameType
        return ParadoxLocalisationInfo(name, category, gameType)
    }

    fun isSpecialLocalisation(element: ParadoxLocalisationProperty): Boolean {
        //存在一些特殊的本地化，不能直接用来渲染文本
        val file = element.containingFile ?: return false
        val fileName = file.name
        if (fileName.startsWith("name_system_")) return true //e.g., name_system_l_english.yml
        return false
    }
}

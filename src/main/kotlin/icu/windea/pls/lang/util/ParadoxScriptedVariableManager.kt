package icu.windea.pls.lang.util

import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.psi.stubs.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.renderers.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

/**
 * 用于处理封装变量。
 */
object ParadoxScriptedVariableManager {
    object Keys : KeyRegistry() {
        val localScriptedVariable by createKey<CachedValue<List<SmartPsiElementPointer<ParadoxScriptScriptedVariable>>>>(this)
    }

    fun getLocalScriptedVariables(file: ParadoxScriptFile): List<SmartPsiElementPointer<ParadoxScriptScriptedVariable>> {
        return CachedValuesManager.getCachedValue(file, Keys.localScriptedVariable) {
            val value = doGetLocalScriptedVariables(file)
            value.withDependencyItems(file)
        }
    }

    private fun doGetLocalScriptedVariables(file: ParadoxScriptFile): List<SmartPsiElementPointer<ParadoxScriptScriptedVariable>> {
        val result = mutableListOf<SmartPsiElementPointer<ParadoxScriptScriptedVariable>>()
        file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxScriptScriptedVariable) {
                    result.add(element.createPointer(file))
                }
                if (!ParadoxPsiManager.inMemberContext(element)) return //optimize
                super.visitElement(element)
            }
        })
        return result
    }

    //stub methods

    fun createStub(psi: ParadoxScriptScriptedVariable, parentStub: StubElement<*>): ParadoxScriptScriptedVariableStub? {
        val file = selectFile(psi) ?: return null
        val gameType = selectGameType(file) ?: return null
        val name = psi.name ?: return null
        return ParadoxScriptScriptedVariableStub.Impl(parentStub, name, gameType)
    }

    fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ParadoxScriptScriptedVariableStub? {
        val psi = parentStub.psi
        val file = selectFile(psi) ?: return null
        val gameType = selectGameType(file) ?: return null
        val name = getNameFromNode(node, tree) ?: return null
        return ParadoxScriptScriptedVariableStub.Impl(parentStub, name, gameType)
    }

    private fun getNameFromNode(node: LighterASTNode, tree: LighterAST): String? {
        //这里认为名字是不带参数的
        return node.firstChild(tree, SCRIPTED_VARIABLE_NAME)
            ?.children(tree)
            ?.takeIf { it.size == 2 && it.first().tokenType == AT }
            ?.last()
            ?.internNode(tree)?.toString()
    }

    fun getNameLocalisation(name: String, contextElement: PsiElement, locale: CwtLocaleConfig): ParadoxLocalisationProperty? {
        val selector = selector(contextElement.project, contextElement).localisation().contextSensitive()
            .preferLocale(locale)
        return ParadoxLocalisationSearch.search(name, selector).find()
    }

    fun getNameLocalisationFromExtendedConfig(name: String, contextElement: PsiElement): ParadoxLocalisationProperty? {
        val hint = getHintFromExtendedConfig(name, contextElement) //just use file as contextElement here
        if (hint.isNullOrEmpty()) return null
        val hintLocalisation = ParadoxLocalisationElementFactory.createProperty(contextElement.project, "hint", hint)
        //it's necessary to inject fileInfo here (so that gameType can be got later)
        hintLocalisation.containingFile.virtualFile.putUserData(PlsKeys.injectedFileInfo, contextElement.fileInfo)
        return hintLocalisation
    }

    fun getLocalizedName(element: ParadoxScriptScriptedVariable): String? {
        val name = element.name?.orNull() ?: return null
        val nameLocalisation = getNameLocalisation(name, element, ParadoxLocaleManager.getPreferredLocaleConfig()) ?: return null
        return ParadoxLocalisationTextRenderer().render(nameLocalisation).orNull()
    }

    fun getHintFromExtendedConfig(name: String, contextElement: PsiElement): String? {
        if (name.isNotEmpty()) return null
        val gameType = selectGameType(contextElement) ?: return null
        val configGroup = PlsFacade.getConfigGroup(contextElement.project, gameType)
        val config = configGroup.extendedScriptedVariables.findFromPattern(name, contextElement, configGroup) ?: return null
        return config.hint
    }
}

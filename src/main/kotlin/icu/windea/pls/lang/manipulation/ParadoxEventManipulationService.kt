package icu.windea.pls.lang.manipulation

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.siblings
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.filterIsInstance
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxEventManager.getName
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.model.constraints.ParadoxGameTypeConstraint
import icu.windea.pls.model.constraints.matchesBy
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptRootBlock
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.parentProperty
import icu.windea.pls.script.psi.propertyValue
import icu.windea.pls.script.psi.stringValue

object ParadoxEventManipulationService {
    fun getNamespaceFromEventNamespaceDeclaration(element: ParadoxDefinitionElement): String? {
        if (element !is ParadoxScriptProperty) return null // TODO [inline_definition]
        return element.propertyValue<ParadoxScriptString>()?.stringValue
    }

    fun getNamespaceFromEventDeclaration(element: ParadoxDefinitionElement): String? {
        if (element !is ParadoxScriptProperty) return null // TODO [inline_definition]
        return getName(element).substringBefore('.').orNull() // enough
    }

    fun getBoundNamespaceDeclarationsFromEventDeclaration(element: PsiElement, gameType: ParadoxGameType? = selectGameType(element)): List<ParadoxScriptProperty> {
        if (gameType == null || gameType == ParadoxGameType.Core) return emptyList()
        return when {
            // #334 CK3/VIC3/EU5
            gameType matchesBy ParadoxGameTypeConstraint.JominiBased -> getBoundNamespaceDeclarationsFromEventDeclarationByMixedForm(element)
            // other game types
            else -> getBoundNamespaceDeclarationsFromEventDeclarationBySequentialForm(element)
        }
    }

    private fun getBoundNamespaceDeclarationsFromEventDeclarationByMixedForm(element: PsiElement): List<ParadoxScriptProperty> {
        val file = element.parent?.parent?.castOrNull<ParadoxScriptFile>() ?: return emptyList()
        val result = file.properties(inline = true).filter { it.definitionInfo?.type == ParadoxDefinitionTypes.eventNamespace }
        return result.toList()
    }

    private fun getBoundNamespaceDeclarationsFromEventDeclarationBySequentialForm(element: PsiElement): List<ParadoxScriptProperty> {
        val namespaces = element.siblings(forward = false, withSelf = false).filterIsInstance<ParadoxScriptProperty> { it.definitionInfo?.type == ParadoxDefinitionTypes.eventNamespace }
        val result = namespaces.firstOrNull() ?: return emptyList()
        if (result.propertyValue !is ParadoxScriptString) return emptyList() // invalid
        return listOf(result)
    }

    fun getNamespaceRangeInFromEventId(element: ParadoxScriptString): TextRange? {
        val text = element.text
        val dotIndex = text.indexOf('.')
        if (dotIndex == -1) return null
        val range = TextRange.create(if (text.isLeftQuoted()) 1 else 0, dotIndex)
        if (range.isEmpty) return null
        return range
    }

    fun getEventDeclarationFromEventId(element: ParadoxScriptStringExpressionElement, gameType: ParadoxGameType? = selectGameType(element)): ParadoxScriptProperty? {
        // if (element.text.isParameterized()) return null // can be parameterized
        if (gameType == null || gameType == ParadoxGameType.Core) return null
        val eventConfig = PlsFacade.getConfigGroup(gameType).types[ParadoxDefinitionTypes.event] ?: return null
        return getEventDeclarationFromEventIdInternal(eventConfig, element)
    }

    fun getEventDeclarationCandidateFromEventId(element: ParadoxScriptStringExpressionElement, gameType: ParadoxGameType? = selectGameType(element)): ParadoxScriptMember? {
        // if (element.text.isParameterized()) return null // can be parameterized
        if (gameType == null || gameType == ParadoxGameType.Core) return null
        val eventConfig = PlsFacade.getConfigGroup(gameType).types[ParadoxDefinitionTypes.event] ?: return null
        if (eventConfig.nameField == null) {
            // for code completion
            if (element is ParadoxScriptString && element.parent is ParadoxScriptRootBlock) return element
        }
        return getEventDeclarationFromEventIdInternal(eventConfig, element)
    }

    private fun getEventDeclarationFromEventIdInternal(eventConfig: CwtTypeConfig, element: ParadoxScriptStringExpressionElement): ParadoxScriptProperty? {
        // NOTE 2.1.10 simple check
        return when {
            // from key
            eventConfig.nameField == null -> {
                if (element !is ParadoxScriptPropertyKey) return null
                val event = element.parentProperty ?: return null
                if (event.parent !is ParadoxScriptRootBlock) return null// NOTE 2.1.10 simple check
                if (event.definitionInfo?.type != ParadoxDefinitionTypes.event) return null
                event
            }
            // from id field
            else -> {
                if (element !is ParadoxScriptString) return null
                val event = selectScope { element.parentOfPath("id") } // 不处理内联的情况
                if (event !is ParadoxScriptProperty) return null
                if (event.parent !is ParadoxScriptRootBlock) return null// NOTE 2.1.10 simple check
                if (event.definitionInfo?.type != ParadoxDefinitionTypes.event) return null
                event
            }
        }
    }

    /**
     * 如果返回的映射的键为空字符串，则表达式那些事件声明不存在绑定的命名空间。
     */
    fun getBoundEventDeclarationsInFile(file: ParadoxScriptFile, gameType: ParadoxGameType? = selectGameType(file)): Map<String, List<ParadoxScriptProperty>> {
        if (gameType == null || gameType == ParadoxGameType.Core) return emptyMap()
        return when {
            // #334 CK3/VIC3/EU5
            gameType matchesBy ParadoxGameTypeConstraint.JominiBased -> getBoundEventDeclarationsInFileByMixedForm(file)
            // other game types
            else -> getBoundEventDeclarationsInFileBySequentialForm(file)
        }
    }

    private fun getBoundEventDeclarationsInFileByMixedForm(file: ParadoxScriptFile): Map<String, List<ParadoxScriptProperty>> {
        val result = mutableMapOf<String, MutableList<ParadoxScriptProperty>>()
        val properties = file.properties(inline = true)
        val existingNamespaces = mutableSetOf<String>()
        for (property in properties) {
            ProgressManager.checkCanceled()
            val definitionInfo = property.definitionInfo ?: continue
            if (definitionInfo.type == ParadoxDefinitionTypes.eventNamespace) {
                val namespace = getNamespaceFromEventNamespaceDeclaration(property).orEmpty()
                if (namespace.isNotEmpty()) existingNamespaces.add(namespace)
            } else if (definitionInfo.type == ParadoxDefinitionTypes.event) {
                val namespace = getNamespaceFromEventDeclaration(property).orEmpty()
                if (namespace.isNotEmpty()) result.getOrPut(namespace) { mutableListOf() }.add(property)
            }
        }
        val unboundEvents = mutableSetOf<ParadoxScriptProperty>()
        for (namespace in result.keys) {
            if (namespace !in existingNamespaces) unboundEvents.addAll(result.get(namespace).orEmpty())
        }
        for (namespace in existingNamespaces) {
            result.remove(namespace)
        }
        result.put("", unboundEvents.toMutableList())
        return result
    }

    private fun getBoundEventDeclarationsInFileBySequentialForm(file: ParadoxScriptFile): Map<String, List<ParadoxScriptProperty>> {
        val result = mutableMapOf<String, MutableList<ParadoxScriptProperty>>()
        val properties = file.properties(inline = true)
        var nextNamespace = ""
        for (property in properties) {
            ProgressManager.checkCanceled()
            val definitionInfo = property.definitionInfo ?: continue
            if (definitionInfo.type == ParadoxDefinitionTypes.eventNamespace) {
                // 如果值不是一个字符串，作为空字符串存到缓存中
                val namespace = getNamespaceFromEventNamespaceDeclaration(property).orEmpty()
                nextNamespace = namespace
                result.getOrPut(namespace) { mutableListOf() }
            } else if (definitionInfo.type == ParadoxDefinitionTypes.event) {
                result.getOrPut(nextNamespace) { mutableListOf() }.add(property)
            }
        }
        return result
    }

    fun isMissingEventNamespaceDeclarationInFile(file: ParadoxScriptFile): Boolean {
        return file.properties(inline = true).none { it.definitionInfo?.type == ParadoxDefinitionTypes.eventNamespace }
    }
}

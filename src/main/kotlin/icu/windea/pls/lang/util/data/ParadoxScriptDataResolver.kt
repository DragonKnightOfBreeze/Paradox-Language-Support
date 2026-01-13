package icu.windea.pls.lang.util.data

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.core.findChild
import icu.windea.pls.lang.psi.select.conditional
import icu.windea.pls.lang.psi.select.inline
import icu.windea.pls.lang.psi.select.members
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptBlockElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptRootBlock
import icu.windea.pls.script.psi.ParadoxScriptValue

class ParadoxScriptDataResolver(
    val forward: Boolean = true,
    val conditional: Boolean = false,
    val inline: Boolean = false,
) {
    fun resolve(element: PsiElement): ParadoxScriptData? {
        return when (element) {
            is ParadoxScriptFile -> resolveFile(element)
            is ParadoxScriptBlock -> resolveBlock(element)
            is ParadoxScriptValue -> resolveValue(element)
            is ParadoxScriptProperty -> resolveProperty(element)
            else -> null
        }
    }

    fun resolveFile(file: PsiFile): ParadoxScriptData? {
        if (file !is ParadoxScriptFile) return null
        val rootBlock = file.findChild<ParadoxScriptRootBlock>() ?: return null
        return resolveBlock(rootBlock)
    }

    fun resolveBlock(element: ParadoxScriptBlockElement): ParadoxScriptData {
        val value = element as? ParadoxScriptBlock
        val children: MutableList<ParadoxScriptData> = mutableListOf()
        element.members().options { conditional() + inline() }.forEach { e ->
            when {
                e is ParadoxScriptValue -> resolveValue(e).let { children.add(it) }
                e is ParadoxScriptProperty -> resolveProperty(e)?.let { children.add(it) }
            }
        }
        return ParadoxScriptDataImpl(null, value, children)
    }

    fun resolveValue(element: ParadoxScriptValue): ParadoxScriptData {
        if (element is ParadoxScriptBlock) return resolveBlock(element)
        return ParadoxScriptDataImpl(null, element, null)
    }

    fun resolveProperty(element: ParadoxScriptProperty): ParadoxScriptData? {
        val propertyKey = element.propertyKey
        val propertyValue = element.propertyValue
        if (propertyValue == null) return null // ignore

        val children = when {
            propertyValue is ParadoxScriptBlock -> resolveBlock(propertyValue).children
            else -> null
        }
        return ParadoxScriptDataImpl(propertyKey, propertyValue, children)
    }

    companion object {
        val DEFAULT = ParadoxScriptDataResolver()
        val INLINE = ParadoxScriptDataResolver(inline = true)
    }
}

package icu.windea.pls.config.util

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.startOffset
import com.jetbrains.rd.util.ThreadLocal
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtOptionConfig
import icu.windea.pls.config.config.CwtOptionMemberConfig
import icu.windea.pls.config.config.CwtOptionValueConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.collections.optimized
import icu.windea.pls.core.forEachChild
import icu.windea.pls.cwt.psi.CwtBlock
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.cwt.psi.CwtMember
import icu.windea.pls.cwt.psi.CwtOption
import icu.windea.pls.cwt.psi.CwtOptionComment
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtValue

object CwtConfigResolverUtil {
    private val currentLocation = ThreadLocal<String>()

    fun setLocation(filePath: String, configGroup: CwtConfigGroup) {
        currentLocation.set(getLocation(filePath, configGroup))
    }

    fun resetLocation() {
        currentLocation.remove()
    }

    private fun getLocation(filePath: String, configGroup: CwtConfigGroup): String {
        val gameTypeId = configGroup.gameType.id
        return "$gameTypeId@$filePath"
    }

    fun String.withLocationPrefix(element: PsiElement? = null): String {
        val location = currentLocation.get() ?: return this
        val file = element?.containingFile
        val lineNumber = file?.fileDocument?.getLineNumber(element.startOffset)
        val lineNumberString = lineNumber?.let { "#L$it" }.orEmpty()
        return "[$location$lineNumberString] $this"
    }

    fun String.withLocationPrefix(config: CwtConfig<*>): String {
        val element = config.pointer.element
        return withLocationPrefix(element)
    }

    fun getConfigs(element: CwtValue, file: CwtFile, configGroup: CwtConfigGroup): List<CwtMemberConfig<*>>? {
        if (element !is CwtBlock) return null
        val configs = mutableListOf<CwtMemberConfig<*>>()
        element.forEachChild { e ->
            when (e) {
                is CwtProperty -> CwtPropertyConfig.resolve(e, file, configGroup)?.let { configs += it }
                is CwtValue -> CwtValueConfig.resolve(e, file, configGroup).let { configs += it }
            }
        }
        return configs.optimized() // optimized to optimize memory
    }

    fun getOptionConfigs(element: CwtMember): List<CwtOptionMemberConfig<*>>? {
        var optionConfigs: MutableList<CwtOptionMemberConfig<*>>? = null
        var current: PsiElement = element
        while (true) {
            current = current.prevSibling ?: break
            when (current) {
                is CwtOptionComment -> {
                    val option = current.option
                    if (option != null) {
                        if (optionConfigs == null) optionConfigs = mutableListOf()
                        val resolved = CwtOptionConfig.resolve(option) ?: continue
                        optionConfigs.add(0, resolved)
                    } else {
                        val optionValue = current.optionValue ?: continue
                        if (optionConfigs == null) optionConfigs = mutableListOf()
                        val resolved = CwtOptionValueConfig.resolve(optionValue)
                        optionConfigs.add(0, resolved)
                    }
                }
                is PsiWhiteSpace, is PsiComment -> continue
                else -> break
            }
        }
        return optionConfigs?.optimized() // optimized to optimize memory
    }

    fun getOptionConfigsInOption(element: CwtValue): List<CwtOptionMemberConfig<*>>? {
        if (element !is CwtBlock) return null
        val optionConfigs = mutableListOf<CwtOptionMemberConfig<*>>()
        element.forEachChild { e ->
            when (e) {
                is CwtOption -> CwtOptionConfig.resolve(e)?.let { optionConfigs += it }
                is CwtValue -> CwtOptionValueConfig.resolve(e).let { optionConfigs += it }
            }
        }
        return optionConfigs.optimized() // optimized to optimize memory
    }
}

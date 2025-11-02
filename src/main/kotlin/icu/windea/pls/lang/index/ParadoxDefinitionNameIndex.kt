package icu.windea.pls.lang.index

import com.intellij.psi.stubs.StringStubIndexExtension
import icu.windea.pls.model.constraints.ParadoxIndexConstraint
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

/**
 * 定义声明的名字的索引。
 */
class ParadoxDefinitionNameIndex : StringStubIndexExtension<ParadoxScriptDefinitionElement>() {
    override fun getKey() = PlsIndexKeys.DefinitionName

    override fun getVersion() = PlsIndexVersions.ScriptStub

    override fun getCacheSize() = 12 * 1024 // CACHE SIZE - 38000+ in stellaris@3.6

    /**
     * @see ParadoxIndexConstraint.Definition
     */
    sealed class BaseIndex : StringStubIndexExtension<ParadoxScriptDefinitionElement>() {
        override fun getVersion() = PlsIndexVersions.ScriptStub
    }

    /**
     * @see ParadoxIndexConstraint.Definition.Resource
     */
    class ResourceIndex : BaseIndex() {
        override fun getKey() = PlsIndexKeys.DefinitionNameForResource
    }

    /**
     * @see ParadoxIndexConstraint.Definition.EconomicCategory
     */
    class EconomicCategoryIndex : BaseIndex() {
        override fun getKey() = PlsIndexKeys.DefinitionNameForEconomicCategory
    }

    /**
     * 用于快速索引文本图标的名字（其定义类型为 `text_icon`）。
     *
     * @see ParadoxIndexConstraint.Definition.TextIcon
     */
    class TextIconIndex : BaseIndex() {
        override fun getKey() = PlsIndexKeys.DefinitionNameForTextIcon
    }

    /**
     * 用于快速索引文本格式的名字（其定义类型为 `text_format`）。它们是忽略大小写的。
     *
     * @see ParadoxIndexConstraint.Definition.TextFormat
     */
    class TextFormatIndex : BaseIndex() {
        override fun getKey() = PlsIndexKeys.DefinitionNameForTextFormat
    }
}

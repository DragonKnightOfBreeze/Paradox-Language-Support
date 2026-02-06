package icu.windea.pls.lang.index

import com.intellij.psi.stubs.StringStubIndexExtension
import icu.windea.pls.model.constraints.ParadoxDefinitionIndexConstraint
import icu.windea.pls.script.psi.ParadoxDefinitionElement

/**
 * 定义声明的名字的索引。
 */
class ParadoxDefinitionNameIndex : StringStubIndexExtension<ParadoxDefinitionElement>() {
    override fun getKey() = PlsIndexKeys.DefinitionName

    override fun getVersion() = PlsIndexVersions.ScriptStub

    override fun getCacheSize() = 12 * 1024 // CACHE SIZE - 38000+ in stellaris@3.6

    /**
     * @see ParadoxDefinitionIndexConstraint
     */
    sealed class BaseIndex : StringStubIndexExtension<ParadoxDefinitionElement>() {
        override fun getVersion() = PlsIndexVersions.ScriptStub
    }

    /**
     * @see ParadoxDefinitionIndexConstraint.Resource
     */
    class ResourceIndex : BaseIndex() {
        override fun getKey() = PlsIndexKeys.DefinitionNameForResource
    }

    /**
     * @see ParadoxDefinitionIndexConstraint.EconomicCategory
     */
    class EconomicCategoryIndex : BaseIndex() {
        override fun getKey() = PlsIndexKeys.DefinitionNameForEconomicCategory
    }

    /**
     * @see ParadoxDefinitionIndexConstraint.GameConcept
     */
    class GameConceptIndex : BaseIndex() {
        override fun getKey() = PlsIndexKeys.DefinitionNameForGameConcept
    }

    /**
     * @see ParadoxDefinitionIndexConstraint.EventNamespace
     */
    class EventNamespaceIndex : BaseIndex() {
        override fun getKey() = PlsIndexKeys.DefinitionNameForEventNamespace
    }

    /**
     * @see ParadoxDefinitionIndexConstraint.Event
     */
    class EventIndex : BaseIndex() {
        override fun getKey() = PlsIndexKeys.DefinitionNameForEvent
    }

    /**
     * @see ParadoxDefinitionIndexConstraint.Sprite
     */
    class SpriteIndex : BaseIndex() {
        override fun getKey() = PlsIndexKeys.DefinitionNameForSprite
    }

    /**
     * 用于快速索引文本颜色的名字（其定义类型为 `text_color`）。
     *
     * @see ParadoxDefinitionIndexConstraint.TextColor
     */
    class TextColorIndex : BaseIndex() {
        override fun getKey() = PlsIndexKeys.DefinitionNameForTextColor
    }

    /**
     * 用于快速索引文本图标的名字（其定义类型为 `text_icon`）。
     *
     * @see ParadoxDefinitionIndexConstraint.TextIcon
     */
    class TextIconIndex : BaseIndex() {
        override fun getKey() = PlsIndexKeys.DefinitionNameForTextIcon
    }

    /**
     * 用于快速索引文本格式的名字（其定义类型为 `text_format`）。它们是忽略大小写的。
     *
     * @see ParadoxDefinitionIndexConstraint.TextFormat
     */
    class TextFormatIndex : BaseIndex() {
        override fun getKey() = PlsIndexKeys.DefinitionNameForTextFormat
    }
}

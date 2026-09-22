package icu.windea.pls.config.model

import icu.windea.pls.config.config.delegated.CwtTypeConfig
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet

/**
 * 定义类型的数据模型。
 *
 * 用于保存和获取符合特定条件的定义类型。
 */
interface CwtTypeModel {
    /** 基础类型到切换类型的映射。 */
    val base2Swapped: Map<String, String>
    /** 切换类型到基础类型的映射。 */
    val swapped2Base: Map<String, String>
    /** 支持作用域的定义类型。 */
    val supportScope: Set<String>
    /** 间接支持作用域的定义类型。 */
    val supportIndirectScope: Set<String>
    /** 支持作用域推断的定义类型。 */
    val supportScopeInference: Set<String>
    /** 不需要检查系统作用域切换的定义类型（应当是固定的，不允许在检查选项中配置）。 */
    val skipCheckSystemScope: Set<String>
    /** 支持参数的定义类型。 */
    val supportParameters: Set<String>
    /**
     * 可能有类型键前缀（typeKeyPrefix）的定义类型 - 按文件路径计算。
     *
     * @see CwtTypeConfig.typeKeyPrefix
     */
    val typeKeyPrefixAware: Set<String>
    /**
     * 可能作为本地化图标的解析目标的定义类型。
     *
     * @see icu.windea.pls.lang.references.localisation.ParadoxLocalisationIconPsiReference
     * @see icu.windea.pls.ep.resolve.localisation.ParadoxLocalisationIconSupport
     * @see icu.windea.pls.ep.resolve.localisation.ParadoxCompositeLocalisationIconSupport.fromDefinition
     * @see icu.windea.pls.lang.index.constraints.ParadoxDefinitionIndexConstraint.LocalisationIconResolvable
     */
    val localisationIconResolvable: Set<String>

    companion object {
        @JvmStatic
        fun create(): CwtTypeModelBase = CwtTypeModelBase()

        @JvmStatic
        fun createEmpty(): CwtTypeModel = EmptyCwtTypeModel
    }
}

class CwtTypeModelBase : CwtTypeModel {
    override val base2Swapped = Object2ObjectLinkedOpenHashMap<String, String>()
    override val swapped2Base = Object2ObjectLinkedOpenHashMap<String, String>()
    override val supportScope = ObjectLinkedOpenHashSet<String>()
    override val supportIndirectScope = ObjectLinkedOpenHashSet<String>()
    override val supportScopeInference = ObjectLinkedOpenHashSet<String>()
    override val skipCheckSystemScope = ObjectLinkedOpenHashSet<String>()
    override val supportParameters = ObjectLinkedOpenHashSet<String>()
    override val typeKeyPrefixAware = ObjectLinkedOpenHashSet<String>()
    override val localisationIconResolvable = ObjectLinkedOpenHashSet<String>()

    fun trim() {
        base2Swapped.trim()
        swapped2Base.trim()
        supportScope.trim()
        supportIndirectScope.trim()
        supportScopeInference.trim()
        skipCheckSystemScope.trim()
        supportParameters.trim()
        typeKeyPrefixAware.trim()
        localisationIconResolvable.trim()
    }
}

private object EmptyCwtTypeModel : CwtTypeModel {
    override val base2Swapped: Map<String, String> get() = emptyMap()
    override val swapped2Base: Map<String, String> get() = emptyMap()
    override val supportScope: Set<String> get() = emptySet()
    override val supportIndirectScope: Set<String> get() = emptySet()
    override val supportScopeInference: Set<String> get() = emptySet()
    override val skipCheckSystemScope: Set<String> get() = emptySet()
    override val supportParameters: Set<String> get() = emptySet()
    override val typeKeyPrefixAware: Set<String> get() = emptySet()
    override val localisationIconResolvable: Set<String> get() = emptySet()
}

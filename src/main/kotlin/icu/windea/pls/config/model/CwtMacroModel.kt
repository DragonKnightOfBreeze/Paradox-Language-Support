package icu.windea.pls.config.model

import icu.windea.pls.config.config.delegated.CwtMacroConfig
import it.unimi.dsi.fastutil.objects.ObjectArrayList

/** 宏规则的数据模型。用于保存和获取符合特定条件的宏规则。 */
interface CwtMacroModel {
    val forInlineScripts: List<CwtMacroConfig.InlineScript>
    val forDefinitionInjections: CwtMacroConfig.DefinitionInjection?

    companion object {
        @JvmStatic
        fun create(): CwtMacroModelBase = CwtMacroModelBase()

        @JvmStatic
        fun createEmpty(): CwtMacroModel = EmptyCwtMacroModel
    }
}

// region Implementations

class CwtMacroModelBase : CwtMacroModel {
    override val forInlineScripts = ObjectArrayList<CwtMacroConfig.InlineScript>()
    override var forDefinitionInjections: CwtMacroConfig.DefinitionInjection? = null

    fun trim() {
        forInlineScripts.trim()
    }
}

private object EmptyCwtMacroModel : CwtMacroModel {
    override val forInlineScripts: List<CwtMacroConfig.InlineScript> get() = emptyList()
    override val forDefinitionInjections: CwtMacroConfig.DefinitionInjection? get() = null
}

// endregion

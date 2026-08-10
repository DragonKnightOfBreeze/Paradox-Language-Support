package icu.windea.pls.model

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.core.getDefaultProject
import icu.windea.pls.core.util.ReadWriteAccess
import icu.windea.pls.core.util.values.LazyValue
import icu.windea.pls.ep.resolve.parameter.ParadoxParameterSupport
import icu.windea.pls.lang.psi.light.ParadoxParameterLightElement
import icu.windea.pls.script.psi.ParadoxConditionParameter
import icu.windea.pls.script.psi.ParadoxParameter
import javax.swing.Icon

/**
 * 参数信息。
 *
 * [contextKey] 用于判断参数是否拥有相同的上下文，格式如下：
 * - 对于定义的参数：`<typeExpression>@<definitionName>`
 * - 对于内联脚本的参数：`inline_script@<inline_script_expression>`
 *
 * @see ParadoxParameterLightElement
 * @see ParadoxParameter
 * @see ParadoxConditionParameter
 * @see ParadoxParameterSupport
 */
class ParadoxParameterInfo(
    val name: String,
    val contextName: String,
    val contextIcon: Icon?,
    val contextKey: String,
    val readWriteAccess: ReadWriteAccess,
    val project: Project,
    val gameType: ParadoxGameType,
) : UserDataHolderBase() {
    // 3.0.1 optimize: use memory-friendly lazy property
    val modificationTracker: ModificationTracker? // region by lazy { computeModificationTracker() }
        get() = LazyValue.ofNullable({ _modificationTracker }, { _modificationTracker = it }) { computeModificationTracker() }
    @Volatile private var _modificationTracker = LazyValue.UNINITIALIZED // endregion

    private fun computeModificationTracker() = support?.getModificationTracker(this)

    override fun toString(): String {
        return "ParadoxParameterInfo(name=$name, contextKey=$contextKey, readWriteAccess=$readWriteAccess, gameType=$gameType, project=$project)"
    }

    companion object {
        @JvmField val EMPTY = ParadoxParameterInfo("", "", null, "", ReadWriteAccess.ReadWrite, getDefaultProject(), ParadoxGameType.Core)
    }
}

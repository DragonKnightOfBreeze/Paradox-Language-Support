package icu.windea.pls.lang.injection

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.util.SmartList
import icu.windea.pls.base.settings.ChronicleSettings
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.expandConfigExpression
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.anyFast
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.isRightQuoted
import icu.windea.pls.core.orNull
import icu.windea.pls.core.unquote
import icu.windea.pls.core.util.ProcessorScope
import icu.windea.pls.lang.psi.ParadoxLanguageInjectionHost
import icu.windea.pls.lang.psi.light.ParadoxParameterLightElement
import icu.windea.pls.lang.resolve.ParadoxParameterService
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxLocalisationManager
import icu.windea.pls.lang.util.ParadoxNameValidators
import icu.windea.pls.lang.util.ParadoxParameterManager
import icu.windea.pls.model.ParadoxParameterContextReferenceInfo
import icu.windea.pls.model.injection.ParadoxLocalisationTextInjectionInfo
import icu.windea.pls.model.injection.ParadoxParameterValueInjectionInfo
import icu.windea.pls.model.type.ParadoxSeparatorType
import icu.windea.pls.script.psi.ParadoxScriptNormalParameterArgument
import icu.windea.pls.script.psi.ParadoxScriptParameter
import icu.windea.pls.script.psi.ParadoxScriptString

@Optimized
object ParadoxLanguageInjectionService {
    fun resolveAllParameterValueInjectionInfo(host: ParadoxLanguageInjectionHost): List<ParadoxParameterValueInjectionInfo> {
        if (host !is ParadoxScriptString && host !is ParadoxScriptNormalParameterArgument) return emptyList()
        val injectionInfos = SmartList<ParadoxParameterValueInjectionInfo>() // expect to be often empty or single

        ProgressManager.checkCanceled()
        applyParameterValueInjectionForArgumentValue(host, injectionInfos)
        ProgressManager.checkCanceled()
        applyParameterValueInjectionForParameterDefaultValue(host, injectionInfos)

        host.putUserData(ParadoxLanguageInjectionKeys.parameterValueInjectionInfos, injectionInfos.orNull())
        if (injectionInfos.isEmpty()) return emptyList()
        return injectionInfos
    }

    private fun applyParameterValueInjectionForArgumentValue(host: ParadoxLanguageInjectionHost, injectionInfos: MutableList<ParadoxParameterValueInjectionInfo>) {
        if (host !is ParadoxScriptString) return
        if (!ChronicleSettings.getInstance().state.inference.injectionForParameterValue) return

        // 这里先向上得到 `contextReferenceInfo`，接着获取传入值对应的 `textRange`，然后选用在 `host` 的 `textRange` 之内的那些
        val from = ParadoxParameterContextReferenceInfo.From.InContextReference
        val contextReferenceInfo = ParadoxParameterManager.getContextReferenceInfo(host, from) ?: return
        if (contextReferenceInfo.arguments.isEmpty()) return
        val hostRange = host.textRange
        contextReferenceInfo.arguments.forEachFast f@{ referenceInfo ->
            val argumentName = referenceInfo.argumentName.orNull() ?: return  // 排除参数名不存在或为空的情况
            if (!ParadoxNameValidators.checkParameterName(argumentName)) return  // 参数名必须合法
            val argumentValue = referenceInfo.argumentValue?.orNull() ?: return  // 排除参数值为空的情况
            if (!acceptParameterValueInjection(argumentValue)) return

            val argumentValueRangeInsideHost = referenceInfo.argumentValueRange
                ?.takeIf { it.startOffset >= hostRange.startOffset && it.endOffset <= hostRange.endOffset }
                ?.shiftLeft(hostRange.startOffset)
                ?: return@f
            val rangeInsideHost = argumentValueRangeInsideHost.unquote(argumentValue) // 这里需要去除参数值两边的双引号
            val parameterValueQuoted = rangeInsideHost.startOffset != argumentValueRangeInsideHost.startOffset && rangeInsideHost.endOffset != argumentValueRangeInsideHost.endOffset // 这里要求参数值两边都有双引号
            val parameterElementProvider = lazy { resolveParameterForArgumentValue(referenceInfo) }
            val injectionInfo = ParadoxParameterValueInjectionInfo(argumentValueRangeInsideHost, parameterValueQuoted, parameterElementProvider)
            injectionInfos += injectionInfo
        }
    }

    private fun resolveParameterForArgumentValue(info: ParadoxParameterContextReferenceInfo.Argument): ParadoxParameterLightElement? {
        val argumentNameElement = info.argumentNameElement ?: return null
        val argumentNameElementRange = argumentNameElement.textRange
        val argumentNameRangeInsideHost = info.argumentNameRange
            .takeIf { it.startOffset >= argumentNameElementRange.startOffset && it.endOffset <= argumentNameElementRange.endOffset }
            ?.shiftLeft(argumentNameElementRange.startOffset)
            ?: return null
        val references = argumentNameElement.references
        for (reference in references) {
            if (reference.rangeInElement != argumentNameRangeInsideHost) continue
            val resolved = reference.resolve()
            if (resolved is ParadoxParameterLightElement) return resolved
        }
        return null
    }

    private fun applyParameterValueInjectionForParameterDefaultValue(host: ParadoxLanguageInjectionHost, injectionInfos: MutableList<ParadoxParameterValueInjectionInfo>) {
        if (host !is ParadoxScriptNormalParameterArgument) return
        if (!ChronicleSettings.getInstance().state.inference.injectionForParameterValue) return

        val parameter = host.parent?.castOrNull<ParadoxScriptParameter>() ?: return
        val parameterName = parameter.name?.orNull() ?: return  // 排除参数名不存在或为空的情况
        if (!ParadoxNameValidators.checkParameterName(parameterName)) return  // 参数名必须合法
        val defaultValue = host.value?.orNull() ?: return  // 排除默认值不存在或为空的情况
        if (!acceptParameterValueInjection(defaultValue)) return

        val rangeInsideHost = TextRange.create(0, defaultValue.length)
        val parameterElementProvider = lazy { resolveParameterForParameterDefaultValue(parameter) }
        val injectionInfo = ParadoxParameterValueInjectionInfo(rangeInsideHost, false, parameterElementProvider)
        injectionInfos += injectionInfo
    }

    private fun resolveParameterForParameterDefaultValue(element: ParadoxScriptParameter): ParadoxParameterLightElement? {
        return ParadoxParameterService.resolveParameter(element)
    }

    private fun acceptParameterValueInjection(value: String): Boolean {
        val normalized = value.unquote().trim()
        // 要求看起来是脚本片段（这里仅进行一些启发式的判断）
        if (normalized.isEmpty()) return false // blank
        if (ParadoxSeparatorType.entries.anyFast { it.text == normalized }) return false // operator-like
        return true
    }

    fun resolveAllLocalisationTextInjectionInfo(host: ParadoxLanguageInjectionHost): List<ParadoxLocalisationTextInjectionInfo> {
        if (host !is ParadoxScriptString) return emptyList()
        val injectionInfos = SmartList<ParadoxLocalisationTextInjectionInfo>() // expect to be often empty or single

        ProgressManager.checkCanceled()
        applyLocalisationTextInjection(host, injectionInfos)

        if (injectionInfos.isEmpty()) return emptyList()
        return injectionInfos
    }

    private fun applyLocalisationTextInjection(host: ParadoxLanguageInjectionHost, injectionInfos: MutableList<ParadoxLocalisationTextInjectionInfo>) {
        if (host !is ParadoxScriptString) return
        if (!ChronicleSettings.getInstance().state.inference.injectionForLocalisationText) return

        val text = host.text
        if (!acceptLocalisationTextInjection(text)) return
        val configs = ParadoxConfigManager.getConfigs(host)
        if (!acceptLocalisationTextInjection(configs)) return

        val rangeInsideHost = TextRange.from(1, text.length - 1) // 不包含括起的双引号
        val injectionInfo = ParadoxLocalisationTextInjectionInfo(rangeInsideHost)
        injectionInfos += injectionInfo
    }

    private fun acceptLocalisationTextInjection(text: String): Boolean {
        // 要求用引号括起，且首尾引号都存在
        if (!text.isLeftQuoted() || !text.isRightQuoted()) return false
        val normalized = text.unquote()
        // 要求看起来像是富文本
        if (ParadoxLocalisationManager.isNormalLocalisationText(normalized)) return false
        return true
    }

    private fun acceptLocalisationTextInjection(configs: List<CwtMemberConfig<*>>): Boolean {
        return configs.anyFast { config -> acceptLocalisationTextInjection(config) }
    }

    private fun acceptLocalisationTextInjection(config: CwtMemberConfig<*>): Boolean {
        if (config !is CwtValueConfig) return false
        return ProcessorScope.anyFrom({ config.expandConfigExpression { process(it) } }) { acceptLocalisationTextInjection(it) }
    }

    private fun acceptLocalisationTextInjection(expression: CwtDataExpression): Boolean {
        // 要求匹配的规则表达式兼容字面量或普通本地化
        return expression.type == CwtDataTypes.Scalar || expression.type in CwtDataTypeSets.LocalisationAware
    }
}

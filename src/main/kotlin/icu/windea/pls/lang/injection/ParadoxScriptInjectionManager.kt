package icu.windea.pls.lang.injection

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiLanguageInjectionHost
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.isRightQuoted
import icu.windea.pls.core.orNull
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.core.unquote
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.ep.resolve.parameter.ParadoxParameterSupport
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement
import icu.windea.pls.lang.selectFile
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxLocalisationManager
import icu.windea.pls.lang.util.PlsFileManager
import icu.windea.pls.model.ParadoxParameterContextReferenceInfo
import icu.windea.pls.model.ParadoxSeparatorType
import icu.windea.pls.model.constants.PlsPatternConstants
import icu.windea.pls.model.injection.ParadoxLocalisationTextInjectionInfo
import icu.windea.pls.model.injection.ParadoxParameterValueInjectionInfo
import icu.windea.pls.script.psi.ParadoxParameter
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.propertyKey

object ParadoxScriptInjectionManager {
    object Keys : KeyRegistry() {
        val parameterValueInjectionInfos by createKey<List<ParadoxParameterValueInjectionInfo>>(Keys)
    }

    fun applyParameterValueInjection(host: PsiLanguageInjectionHost): List<ParadoxParameterValueInjectionInfo> {
        val injectionInfos = mutableListOf<ParadoxParameterValueInjectionInfo>()

        ProgressManager.checkCanceled()
        applyParameterValueInjectionForArgumentValue(host, injectionInfos)
        ProgressManager.checkCanceled()
        applyParameterValueInjectionForParameterDefaultValue(host, injectionInfos)

        host.putUserData(Keys.parameterValueInjectionInfos, injectionInfos.orNull())
        return injectionInfos
    }

    private fun applyParameterValueInjectionForArgumentValue(host: PsiLanguageInjectionHost, injectionInfos: MutableList<ParadoxParameterValueInjectionInfo>) {
        if (host !is ParadoxScriptString) return
        if (!PlsFacade.getSettings().state.inference.injectionForParameterValue) return

        val argumentName = host.propertyKey?.name?.orNull() ?: return  // 排除参数名不存在或为空的情况
        if (!PlsPatternConstants.argumentName.matches(argumentName)) return  // 参数名必须合法
        val argumentValue = host.text.orNull() ?: return  // 参数参数值为空的情况
        if (shouldApplyParameterValueInjection(argumentValue)) return

        // 这里先向上得到 `contextReferenceInfo`，接着获取传入值对应的 `textRange`，然后选用在 `host` 的 `textRange` 之内的那些
        val from = ParadoxParameterContextReferenceInfo.From.InContextReference
        val contextReferenceInfo = ParadoxParameterSupport.getContextReferenceInfo(host, from = from) ?: return
        if (contextReferenceInfo.arguments.isEmpty()) return
        val hostRange = host.textRange
        contextReferenceInfo.arguments.forEach f@{ referenceInfo ->
            // 这里需要特殊处理传入参数值被双引号括起的情况
            val rawRangeInsideHost = referenceInfo.argumentValueRange
                ?.takeIf { it.startOffset >= hostRange.startOffset && it.endOffset <= hostRange.endOffset }
                ?.shiftLeft(hostRange.startOffset)
                ?: return@f
            val rangeInsideHost = rawRangeInsideHost.unquote(rawRangeInsideHost.substring(argumentValue))
            // 这里要求参数值两边都有双引号
            val parameterValueQuoted = rawRangeInsideHost.startOffset != rangeInsideHost.startOffset && rawRangeInsideHost.endOffset != rangeInsideHost.endOffset
            val parameterElementProvider = lazy {
                val argumentNameElement = referenceInfo.argumentNameElement ?: return@lazy null
                val argumentNameElementRange = argumentNameElement.textRange
                val argumentNameRange = referenceInfo.argumentNameRange
                    .takeIf { it.startOffset >= argumentNameElementRange.startOffset && it.endOffset <= argumentNameElementRange.endOffset }
                    ?.shiftLeft(argumentNameElementRange.startOffset)
                    ?: return@lazy null
                argumentNameElement.references.firstNotNullOfOrNull t@{ reference ->
                    if (reference.rangeInElement != argumentNameRange) return@t null
                    reference.resolve()?.castOrNull<ParadoxParameterElement>()
                }
            }
            val injectionInfo = ParadoxParameterValueInjectionInfo(rangeInsideHost, parameterValueQuoted, parameterElementProvider)
            injectionInfos += injectionInfo
        }
    }

    private fun applyParameterValueInjectionForParameterDefaultValue(host: PsiLanguageInjectionHost, injectionInfos: MutableList<ParadoxParameterValueInjectionInfo>) {
        if (host !is ParadoxParameter) return
        if (!PlsFacade.getSettings().state.inference.injectionForParameterValue) return

        val parameterName = host.name?.orNull() ?: return  // 排除参数名不存在或为空的情况
        if (!PlsPatternConstants.parameterName.matches(parameterName)) return  // 参数名必须合法
        val defaultValue = host.defaultValue?.orNull() ?: return  // 排除默认值不存在或为空的情况
        if (!shouldApplyParameterValueInjection(defaultValue)) return

        val defaultValueIndex = host.text.indexOf(defaultValue)
        val rangeInsideHost = TextRange.from(defaultValueIndex, defaultValue.length)
        val parameterElementProvider = lazy { ParadoxParameterSupport.resolveParameter(host) }
        val injectionInfo = ParadoxParameterValueInjectionInfo(rangeInsideHost, false, parameterElementProvider)
        injectionInfos += injectionInfo
    }

    private fun shouldApplyParameterValueInjection(value: String): Boolean {
        val normalized = value.unquote().trim()
        if (ParadoxSeparatorType.entries.any { it.text == normalized }) return true // 为一些狡猾人行方便
        return false
    }

    fun applyLocalisationTextInjection(host: PsiLanguageInjectionHost): List<ParadoxLocalisationTextInjectionInfo> {
        val injectionInfos = mutableListOf<ParadoxLocalisationTextInjectionInfo>()

        ProgressManager.checkCanceled()
        applyLocalisationTextInjection(host, injectionInfos)

        return injectionInfos
    }

    private fun applyLocalisationTextInjection(host: PsiLanguageInjectionHost, injectionInfos: MutableList<ParadoxLocalisationTextInjectionInfo>) {
        if (host !is ParadoxScriptString) return
        if (!PlsFacade.getSettings().state.inference.injectionForLocalisationText) return

        val text = host.text
        if (!shouldApplyLocalisationTextInjection(text)) return
        val configs = ParadoxExpressionManager.getConfigs(host)
        if (!shouldApplyLocalisationTextInjection(configs)) return

        val rangeInsideHost = TextRange.from(1, text.length - 1) // 不包含括起的双引号
        val injectionInfo = ParadoxLocalisationTextInjectionInfo(rangeInsideHost)
        injectionInfos += injectionInfo
    }

    private fun shouldApplyLocalisationTextInjection(text: String): Boolean {
        // 要求用引号括起，且首尾引号都存在
        if (!text.isLeftQuoted() || !text.isRightQuoted()) return false
        // 要求看起来像是富文本
        return ParadoxLocalisationManager.isRichText(text.unquote())
    }

    private fun shouldApplyLocalisationTextInjection(configs: List<CwtConfig<*>>): Boolean {
        // 要求匹配的规则表达式兼容字面量或普通本地化
        return configs.any { config ->
            if (config !is CwtValueConfig) return@any false
            val dataType = config.configExpression.type
            dataType == CwtDataTypes.Scalar || dataType in CwtDataTypeGroups.LocalisationAware
        }
    }

    fun getParameterValueInjectionInfoFromInjectedFile(injectedFile: PsiFile): ParadoxParameterValueInjectionInfo? {
        val vFile = selectFile(injectedFile) ?: return null
        if (!PlsFileManager.isInjectedFile(vFile)) return null
        val host = InjectedLanguageManager.getInstance(injectedFile.project).getInjectionHost(injectedFile)
        if (host == null) return null

        val injectionInfos = host.getUserData(Keys.parameterValueInjectionInfos)
        if (injectionInfos.isNullOrEmpty()) return null
        val injectionInfo = when {
            host is ParadoxScriptStringExpressionElement -> {
                val file0 = vFile.toPsiFile(injectedFile.project) ?: injectedFile // actual PsiFile of VirtualFileWindow
                val shreds = PlsInjectionManager.getShreds(file0)
                val shred = shreds?.singleOrNull()
                val rangeInsideHost = shred?.rangeInsideHost ?: return null
                // it.rangeInsideHost may not equal to rangeInsideHost, but inside (e.g., there are escaped double quotes)
                injectionInfos.find { it.rangeInsideHost.startOffset in rangeInsideHost }
            }
            host is ParadoxParameter -> {
                // just use the only one
                injectionInfos.singleOrNull()
            }
            else -> null
        }
        return injectionInfo
    }
}

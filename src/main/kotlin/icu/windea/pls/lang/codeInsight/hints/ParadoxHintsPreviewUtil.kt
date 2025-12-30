package icu.windea.pls.lang.codeInsight.hints

import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import com.intellij.testFramework.LightVirtualFile
import icu.windea.pls.core.getCurrentStackTrace
import icu.windea.pls.core.toClasspathUrl
import icu.windea.pls.lang.psi.ParadoxBaseFile
import java.util.*

@Suppress("UnstableApiUsage")
object ParadoxHintsPreviewUtil {
    private val logger = thisLogger()
    private const val allDataFilePath = "/inlayProviders/preview.data.properties"
    private val allData by lazy { loadAllData() }

    private fun loadAllData(): Map<String, Map<Int, String>> {
        val properties = Properties()
        try {
            allDataFilePath.toClasspathUrl().openStream().use { properties.load(it) }
        } catch (e: Exception) {
            logger.warn("Cannot load preview data from file: $allDataFilePath", e)
            return emptyMap()
        }
        val map = mutableMapOf<String, MutableMap<Int, String>>()
        for ((key, value) in properties) {
            val k = key.toString()
            val providerId = k.substringBeforeLast('.')
            val offset = k.substringAfterLast('.').toInt()
            val v = value.toString()
            map.getOrPut(providerId) { mutableMapOf() }.getOrPut(offset) { v }
        }
        return map
    }

    fun detectPreview(file: ParadoxBaseFile, index: Int): Boolean {
        // 首先判断是否是内存文件，然后再根据堆栈来判断
        val vFile = file.viewProvider.virtualFile
        if (vFile !is LightVirtualFile) return false
        val currentStackTrace = getCurrentStackTrace()
        val toDetect = currentStackTrace.getOrNull(index) ?: return false
        if (toDetect.className != "com.intellij.codeInsight.hints.settings.language.NewInlayProviderSettingsModel") return false
        if (toDetect.methodName != "collectData") return false
        return true
    }

    context(provider: ParadoxHintsProvider, context: ParadoxHintsContext)
    fun fillData(element: PsiElement, sink: InlayHintsSink) {
        val data = allData[provider.key.id]
        if (data.isNullOrEmpty()) return logger.warn("Missing preview data for provider: ${provider.key.id}")
        val offset = element.endOffset
        val text = data[offset] ?: return
        sink.addInlinePresentation(offset) { text(text) }
    }
}

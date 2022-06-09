package icu.windea.pls.core

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import icu.windea.pls.*

/**
 * IDE启动时执行。
 */
class ParadoxPreloadActivity : PreloadingActivity() {
	override fun preload(indicator: ProgressIndicator) {
		setInternalSettings()
	}
	
	private fun setInternalSettings() {
		val internalSettings = getInternalSettings()
		PlsProperties.debug.getBooleanProperty()?.let { internalSettings.debug = it }
		PlsProperties.annotateUnresolvedKeyExpression.getBooleanProperty()?.let { internalSettings.annotateUnresolvedKeyExpression = it }
		PlsProperties.annotateUnresolvedValueExpression.getBooleanProperty()?.let { internalSettings.annotateUnresolvedValueExpression = it }
	}
	
	private fun String.getBooleanProperty(): Boolean? {
		return System.getProperty(this)?.toBoolean()
	}
}
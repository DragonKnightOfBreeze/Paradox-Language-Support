package com.windea.plugin.idea.paradox.core.settings

import com.intellij.ui.layout.*
import com.windea.plugin.idea.paradox.*
import javax.swing.*

class ParadoxSettingsComponent {
	companion object {
		private val _performanceTitle = message("paradox.settings.performance")
		private val _validateScriptName = message("paradox.settings.performance.validateScript")
		private val _validateScriptComment = message("paradox.settings.performance.validateScript.comment")
		private val _preferOverriddenName = message("paradox.settings.performance.preferOverridden")
		private val _preferOverriddenComment = message("paradox.settings.performance.preferOverridden.comment")
		private val _renderLineCommentText = message("paradox.settings.performance.renderLineCommentText")
		private val _renderLineCommentTextComment = message("paradox.settings.performance.renderLineCommentText.comment")
		private val _renderDefinitionText = message("paradox.settings.performance.renderDefinitionText")
		private val _renderDefinitionTextComment = message("paradox.settings.performance.renderDefinitionText.comment")
		private val _renderLocalisationText = message("paradox.settings.performance.renderLocalisationText")
		private val _renderLocalisationTextComment = message("paradox.settings.performance.renderLocalisationText.comment")
	}
	
	lateinit var validateScriptCheckBox: JCheckBox
	lateinit var preferOverriddenCheckBox: JCheckBox
	lateinit var renderLineCommentTextCheckBox: JCheckBox
	lateinit var renderDefinitionTextCheckBox: JCheckBox
	lateinit var renderLocalisationTextCheckBox: JCheckBox
	
	val panel = panel {
		titledRow(_performanceTitle) {
			row {
				checkBox(_validateScriptName, true, _validateScriptComment)
					.apply { validateScriptCheckBox = component }
			}
			row {
				checkBox(_preferOverriddenName, true, _preferOverriddenComment)
					.apply { preferOverriddenCheckBox = component }
			}
			row {
				checkBox(_renderLineCommentText, true, _renderLineCommentTextComment)
					.apply { renderLineCommentTextCheckBox = component }
			}
			row {
				checkBox(_renderDefinitionText, true, _renderDefinitionTextComment)
					.apply { renderDefinitionTextCheckBox = component }
			}
			row {
				checkBox(_renderLocalisationText, true, _renderLocalisationTextComment)
					.apply { renderLocalisationTextCheckBox = component }
			}
		}
	}
}
package com.windea.plugin.idea.pls.core.settings

import com.intellij.ui.layout.*
import com.windea.plugin.idea.pls.*
import javax.swing.*

class ParadoxSettingsComponent {
	companion object {
		private val _genericTitle = message("pls.settings.generic")
		private val _preferOverriddenName = message("pls.settings.performance.preferOverridden")
		private val _preferOverriddenComment = message("pls.settings.performance.preferOverridden.comment")
		private val _renderLineCommentText = message("pls.settings.performance.renderLineCommentText")
		private val _renderLineCommentTextComment = message("pls.settings.performance.renderLineCommentText.comment")
		private val _renderDefinitionText = message("pls.settings.performance.renderDefinitionText")
		private val _renderDefinitionTextComment = message("pls.settings.performance.renderDefinitionText.comment")
		private val _renderLocalisationText = message("pls.settings.performance.renderLocalisationText")
		private val _renderLocalisationTextComment = message("pls.settings.performance.renderLocalisationText.comment")
	}
	
	lateinit var preferOverriddenCheckBox: JCheckBox
	lateinit var renderLineCommentTextCheckBox: JCheckBox
	lateinit var renderDefinitionTextCheckBox: JCheckBox
	lateinit var renderLocalisationTextCheckBox: JCheckBox
	
	val panel = panel {
		titledRow(_genericTitle) {
			row {
				checkBox(_preferOverriddenName, true,_preferOverriddenComment)
					.applyToComponent { preferOverriddenCheckBox = this }
			}
			row {
				checkBox(_renderLineCommentText, true, _renderLineCommentTextComment)
					.applyToComponent { renderLineCommentTextCheckBox = this }
			}
			row {
				checkBox(_renderDefinitionText, true, _renderDefinitionTextComment)
					.applyToComponent { renderDefinitionTextCheckBox = this }
			}
			row {
				checkBox(_renderLocalisationText, true, _renderLocalisationTextComment)
					.applyToComponent { renderLocalisationTextCheckBox = this }
			}
		}
	}
}
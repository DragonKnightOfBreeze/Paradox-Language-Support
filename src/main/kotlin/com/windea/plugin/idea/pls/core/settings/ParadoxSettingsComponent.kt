package com.windea.plugin.idea.pls.core.settings

import com.intellij.ui.components.*
import com.intellij.ui.layout.*
import com.windea.plugin.idea.pls.*
import com.windea.plugin.idea.pls.model.*
import javax.swing.*

class ParadoxSettingsComponent {
	companion object {
		private val _genericTitle = message("pls.settings.generic")
		private val _preferOverriddenName = message("pls.settings.generic.preferOverridden")
		private val _preferOverriddenComment = message("pls.settings.generic.preferOverridden.comment")
		private val _renderLineCommentText = message("pls.settings.generic.renderLineCommentText")
		private val _renderLineCommentTextComment = message("pls.settings.generic.renderLineCommentText.comment")
		private val _renderDefinitionText = message("pls.settings.generic.renderDefinitionText")
		private val _renderDefinitionTextComment = message("pls.settings.generic.renderDefinitionText.comment")
		private val _renderLocalisationText = message("pls.settings.generic.renderLocalisationText")
		private val _renderLocalisationTextComment = message("pls.settings.generic.renderLocalisationText.comment")
		private val _defaultGameType = message("pls.settings.generic.defaultGameType")
		private val _defaultGameTypeComment = message("pls.settings.generic.defaultGameType.comment")
		private val _unsupportedGameType = message("unsupportedGameType")
	}
	
	lateinit var preferOverriddenCheckBox: JCheckBox
	lateinit var renderLineCommentTextCheckBox: JCheckBox
	lateinit var renderDefinitionTextCheckBox: JCheckBox
	lateinit var renderLocalisationTextCheckBox: JCheckBox
	lateinit var defaultGameTypeTextField :JBTextField
	
	val panel = panel {
		titledRow(_genericTitle) {
			row {
				checkBox(_preferOverriddenName, true).comment(_preferOverriddenComment)
					.applyToComponent { preferOverriddenCheckBox = this }
			}
			row {
				checkBox(_renderLineCommentText, true).comment(_renderLineCommentTextComment)
					.applyToComponent { renderLineCommentTextCheckBox = this }
			}
			row {
				checkBox(_renderDefinitionText, true).comment(_renderDefinitionTextComment)
					.applyToComponent { renderDefinitionTextCheckBox = this }
			}
			row {
				checkBox(_renderLocalisationText, true).comment(_renderLocalisationTextComment)
					.applyToComponent { renderLocalisationTextCheckBox = this }
			}
			row{
				textField(ParadoxSettingsState.getInstance()::defaultGameType).comment(_defaultGameTypeComment)
					.applyToComponent { defaultGameTypeTextField = this }
					.withErrorOnApplyIf(_unsupportedGameType) {ParadoxGameType.isValidKey(it.text) }
			}
		}
	}
}
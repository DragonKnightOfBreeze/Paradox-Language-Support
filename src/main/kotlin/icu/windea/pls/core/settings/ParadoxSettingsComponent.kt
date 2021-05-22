package icu.windea.pls.core.settings

import com.intellij.ui.layout.*
import icu.windea.pls.*
import icu.windea.pls.model.*
import javax.swing.*

class ParadoxSettingsComponent {
	companion object {
		private val _genericTitle = message("pls.settings.generic")
		private val _preferOverriddenName = message("pls.settings.generic.preferOverridden")
		private val _preferOverriddenTooltip = message("pls.settings.generic.preferOverridden.tooltip")
		private val _renderLineCommentText = message("pls.settings.generic.renderLineCommentText")
		private val _renderLineCommentTextTooltip = message("pls.settings.generic.renderLineCommentText.tooltip")
		private val _renderDefinitionText = message("pls.settings.generic.renderDefinitionText")
		private val _renderDefinitionTextTooltip = message("pls.settings.generic.renderDefinitionText.tooltip")
		private val _renderLocalisationText = message("pls.settings.generic.renderLocalisationText")
		private val _renderLocalisationTextTooltip = message("pls.settings.generic.renderLocalisationText.tooltip")
		private val _defaultGameType = message("pls.settings.generic.defaultGameType")
		private val _defaultGameTypeTooltip = message("pls.settings.generic.defaultGameType.tooltip")
		private val _unsupportedGameType = message("unsupportedGameType")
	}
	
	lateinit var preferOverriddenCheckBox: JCheckBox
	lateinit var renderLineCommentTextCheckBox: JCheckBox
	lateinit var renderDefinitionTextCheckBox: JCheckBox
	lateinit var renderLocalisationTextCheckBox: JCheckBox
	lateinit var defaultGameTypeComboBox :JComboBox<ParadoxGameType>
	
	val panel = panel {
		titledRow(_genericTitle) {
			row {
				checkBox(_preferOverriddenName, false).applyToComponent {
					toolTipText = _preferOverriddenTooltip 
					preferOverriddenCheckBox = this 
				}
			}
			row {
				checkBox(_renderLineCommentText, false).applyToComponent {
					toolTipText = _renderLineCommentTextTooltip
					renderLineCommentTextCheckBox = this
				}
			}
			row {
				checkBox(_renderDefinitionText, true).applyToComponent {  
					toolTipText = _renderDefinitionTextTooltip
					renderDefinitionTextCheckBox = this 
				}
			}
			row {
				checkBox(_renderLocalisationText, true).applyToComponent {
					toolTipText = _renderLocalisationTextTooltip
					renderLocalisationTextCheckBox = this
				}
			}
			row{
				label(_defaultGameType).applyToComponent { 
					toolTipText = _defaultGameTypeTooltip
				}
				comboBox(DefaultComboBoxModel(ParadoxGameType.values), getSettings()::defaultGameType).applyToComponent { 
					defaultGameTypeComboBox = this 
				}
			}
		}
	}
}
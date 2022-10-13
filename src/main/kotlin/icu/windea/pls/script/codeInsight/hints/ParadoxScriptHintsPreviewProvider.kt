package icu.windea.pls.script.codeInsight.hints

import com.intellij.psi.*
import icu.windea.pls.*

object ParadoxScriptHintsPreviewProvider {
	val civicPreview = """
		civic_shared_burden = {
			description = civic_tooltip_shared_burden_effects
			swap_type = {
				name = civic_communist_society
				description = "civic_tooltip_communist_society_effects"
				trigger = {
					has_ascension_perk = ap_future_society
				}
			}
			potential = {
				ethics = {
					NOT = {
						value = ethic_gestalt_consciousness
					}
				}
				authority = {
					NOT = {
						value = auth_corporate
					}
				}
				# ...
			}
			# ...
		}
	""".trimIndent()
	
	fun handleCivicPreviewFile(file: PsiFile){
		val injectedInfo = listOf(
			"" //TODO
		)
		file.putUserData(PlsKeys.injectedInfoKey, injectedInfo)
	}
}
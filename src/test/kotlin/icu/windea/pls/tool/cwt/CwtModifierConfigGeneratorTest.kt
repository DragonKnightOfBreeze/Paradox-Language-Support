package icu.windea.pls.tool.cwt

import com.intellij.openapi.application.*
import com.intellij.openapi.fileEditor.*
import com.intellij.testFramework.HeavyPlatformTestCase
import com.intellij.testFramework.builders.ModuleFixtureBuilder
import com.intellij.testFramework.fixtures.*
import icu.windea.pls.core.model.*
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.*

class CwtModifierConfigGeneratorTest : BasePlatformTestCase() {
	override fun isWriteActionRequired(): Boolean {
		return true
	}
	
	@Test
	fun testGenerateForCk3() {
		CwtModifierConfigGenerator(
			project,
			ParadoxGameType.Ck3,
			"cwt/cwtools-ck3-config/script-docs/modifiers.log",
			"cwt/cwtools-ck3-config/config/modifiers.cwt",
			"cwt/cwtools-ck3-config/config/modifier_categories.cwt"
		).generate()
		runInEdt { FileDocumentManager.getInstance().saveAllDocuments() }
	}
	
	@Test
	fun testGenerateForStellaris() {
		CwtModifierConfigGenerator(
			project,
			ParadoxGameType.Stellaris,
			"cwt/cwtools-stellaris-config/script-docs/modifiers.log",
			"cwt/cwtools-stellaris-config/config/modifiers.cwt",
			"cwt/cwtools-stellaris-config/config/modifier_categories.cwt"
		).generate()
		runInEdt { FileDocumentManager.getInstance().saveAllDocuments() }
	}
	
	@Test
	fun testGenerateForVic3() {
		CwtModifierConfigGenerator(
			project,
			ParadoxGameType.Vic3,
			"cwt/cwtools-vic3-config/script-docs/modifiers.log",
			"cwt/cwtools-vic3-config/config/modifiers.cwt",
			"cwt/cwtools-vic3-config/config/modifier_categories.cwt"
		).generate()
		runInEdt { FileDocumentManager.getInstance().saveAllDocuments() }
	}
}
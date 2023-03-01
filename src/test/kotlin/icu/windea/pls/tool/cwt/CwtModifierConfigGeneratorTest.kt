package icu.windea.pls.tool.cwt

import icu.windea.pls.dev.cwt.generator.*
import icu.windea.pls.lang.model.*
import org.junit.*

class CwtModifierConfigGeneratorTest{
	@Test
	fun testForCk3() {
		CwtModifierConfigGenerator(
			ParadoxGameType.Ck3,
			"cwt/cwtools-ck3-config/script-docs/modifiers.log",
			"cwt/cwtools-ck3-config/config/modifiers.gen.cwt",
			"cwt/cwtools-ck3-config/config/modifier_categories.cwt"
		).generate()
	}
	
	
	@Test
	fun testForStellaris() {
		CwtModifierConfigGenerator(
			ParadoxGameType.Stellaris,
			"cwt/cwtools-stellaris-config/script-docs/modifiers.log",
			"cwt/cwtools-stellaris-config/config/modifiers.gen.cwt",
			"cwt/cwtools-stellaris-config/config/modifier_categories.cwt"
		).generate()
	}
	
	@Test
	fun testForVic3() {
		CwtModifierConfigGenerator(
			ParadoxGameType.Vic3,
			"cwt/cwtools-vic3-config/script-docs/modifiers.log",
			"cwt/cwtools-vic3-config/config/modifiers.gen.cwt",
			"cwt/cwtools-vic3-config/config/modifier_categories.cwt"
		).generate()
	}
}
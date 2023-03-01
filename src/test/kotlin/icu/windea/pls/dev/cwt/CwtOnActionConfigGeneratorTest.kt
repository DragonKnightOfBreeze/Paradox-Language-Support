package icu.windea.pls.dev.cwt

import icu.windea.pls.lang.model.*
import org.junit.*

class CwtOnActionConfigGeneratorTest{
	@Test
	fun testForCk2() {
		CwtOnActionConfigGenerator(
            ParadoxGameType.Ck2,
            "cwt/cwtools-ck2-config/on_actions.csv",
            "cwt/cwtools-ck2-config/on_actions.cwt",
        ).generate()
	}
	
	@Test
	fun testForEu4() {
		CwtOnActionConfigGenerator(
            ParadoxGameType.Eu4,
            "cwt/cwtools-eu4-config/on_actions.csv",
            "cwt/cwtools-eu4-config/on_actions.cwt",
        ).generate()
	}
	
	@Test
	fun testForHoi4() {
		CwtOnActionConfigGenerator(
            ParadoxGameType.Hoi4,
            "cwt/cwtools-hoi4-config/Config/on_actions.csv",
            "cwt/cwtools-hoi4-config/Config/on_actions.cwt",
        ).generate()
	}
	
	@Test
	fun testForStellaris() {
		CwtOnActionConfigGenerator(
            ParadoxGameType.Stellaris,
            "cwt/cwtools-stellaris-config/config/on_actions.csv",
            "cwt/cwtools-stellaris-config/config/on_actions.cwt",
        ).generate()
	}
}
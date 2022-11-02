package icu.windea.pls

import com.intellij.openapi.util.*
import icu.windea.pls.core.*
import org.junit.*
import java.io.*

class MiscTest {
	@Test
	fun testIconFromAbsPath(){
		val path = "C:\\Users\\29442\\.pls\\images\\bd0383c4-83ee-328d-9a2e-8a937e8b218b\\gfx\\interface\\icons\\text_icons\\icon_text_pop.dds.png"
		val icon1 = IconLoader.findIcon(path, locationClass)
		println(icon1?.iconHeight)
		val icon2 = IconLoader.findIcon(File(path).toURI().toURL())
		println(icon2?.iconHeight) //OK
	}
}
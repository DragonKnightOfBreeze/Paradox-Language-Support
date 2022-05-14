package icu.windea.pls

import com.intellij.util.IconUtil
import javax.swing.Icon

fun Icon.resize(width:Int, height:Int): Icon {
	return IconUtil.toSize(this, width, height)
}
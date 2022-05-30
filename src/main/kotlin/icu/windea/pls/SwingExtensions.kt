package icu.windea.pls

import com.intellij.util.IconUtil
import java.awt.*
import javax.swing.Icon

fun Icon.resize(width:Int, height:Int): Icon {
	return IconUtil.toSize(this, width, height)
}

val Color.rgbString get() = buildString { 
	val r = red.toString(16)
	val g = green.toString(16)
	val b = blue.toString()
	append('#')
	if(r.length == 1) append('0')
	append(r)
	if(g.length == 1) append('0')
	append(g)
	if(b.length == 1) append('0')
	append(b)
}
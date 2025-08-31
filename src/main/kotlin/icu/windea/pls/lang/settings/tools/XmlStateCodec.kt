package icu.windea.pls.lang.settings.tools

import com.intellij.util.xmlb.XmlSerializer
import org.jdom.Element
import org.jdom.input.SAXBuilder
import org.jdom.output.Format
import org.jdom.output.XMLOutputter
import java.io.StringReader

/**
 * Codec to serialize/deserialize IntelliJ BaseState-like objects to/from XML strings.
 */
object XmlStateCodec {
    private val outputter = XMLOutputter(Format.getCompactFormat())

    fun <T : Any> serialize(value: T): String {
        val element: Element = XmlSerializer.serialize(value)
        return outputter.outputString(element)
    }

    fun <T : Any> deserialize(xml: String, clazz: Class<T>): T {
        val document = SAXBuilder().build(StringReader(xml))
        val element = document.rootElement
        return XmlSerializer.deserialize(element, clazz)
    }
}

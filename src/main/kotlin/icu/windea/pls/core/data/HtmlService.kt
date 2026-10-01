package icu.windea.pls.core.data

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

/**
 * 用于数据处理的 HTML 服务。
 */
object HtmlService {
    fun areEquivalent(html1: String, html2: String): Boolean {
        if(html1 == html2) return true

        // 使用 parseBodyFragment 避免自动补充完整的 html/head 结构干扰片段对比
        val doc1 = Jsoup.parseBodyFragment(html1)
        val doc2 = Jsoup.parseBodyFragment(html2)
        // 从 body 节点开始比对
        return areElementsEquivalent(doc1.body(), doc2.body())
    }

    private fun areElementsEquivalent(e1: Element, e2: Element): Boolean {
        // 1. 比较标签名（Jsoup 的 normalName 已经自动转为小写）
        if (e1.normalName() != e2.normalName()) {
            return false
        }

        // 2. 比较 class（class 顺序不同视为相同）
        val classes1 = e1.classNames()
        val classes2 = e2.classNames()
        if (classes1 != classes2) {
            return false
        }

        // 3. 比较其他属性（排除 class）
        if (!compareAttributes(e1, e2)) {
            return false
        }

        // 4. 比较子节点（过滤无意义的空白文本节点）
        val children1 = getSignificantChildren(e1)
        val children2 = getSignificantChildren(e2)

        if (children1.size != children2.size) {
            return false
        }

        for (i in children1.indices) {
            val n1: Node = children1.get(i)
            val n2: Node = children2.get(i)

            if (n1 is Element && n2 is Element) {
                if (!areElementsEquivalent(n1, n2)) {
                    return false
                }
            } else if (n1 is TextNode && n2 is TextNode) {
                // 压缩首尾和内部多余的空白字符后再比较
                val t1 = n1.text().trim()
                val t2 = n2.text().trim()
                if (t1 != t2) {
                    return false
                }
            } else {
                // 类型不同（一个是 Element，一个是 TextNode）
                return false
            }
        }

        return true
    }

    private fun compareAttributes(e1: Element, e2: Element): Boolean {
        var count1 = 0
        for (attr in e1.attributes()) {
            if ("class".equals(attr.key, ignoreCase = true)) continue
            count1++
            // 校验 e2 是否存在相同属性且值一致
            if (!e2.hasAttr(attr.key) || e2.attr(attr.key) != attr.value) {
                return false
            }
        }

        var count2 = 0
        for (attr in e2.attributes()) {
            if ("class".equals(attr.key, ignoreCase = true)) continue
            count2++
        }

        return count1 == count2
    }

    private fun getSignificantChildren(element: Element): MutableList<Node> {
        // 获取有意义的子节点，忽略纯空白的文本节点
        val significant: MutableList<Node> = ArrayList()
        for (child in element.childNodes()) {
            if (child is TextNode) {
                val textNode: TextNode = child
                // 如果是纯空白/换行文本，并且不是 pre/textarea 等特殊标签内，忽略它
                if (!textNode.isBlank || isPreserveWhitespace(element)) {
                    significant.add(child)
                }
            } else if (child is Element) {
                significant.add(child)
            }
            // 注释节点 (Comment) 默认忽略，如有需要也可在此加入
        }
        return significant
    }

    private fun isPreserveWhitespace(element: Element): Boolean {
        val tag = element.normalName()
        return "pre" == tag || "textarea" == tag
    }
}

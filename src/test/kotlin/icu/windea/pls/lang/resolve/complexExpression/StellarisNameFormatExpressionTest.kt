package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import com.intellij.testFramework.TestDataPath
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.core.emptyPointer
import icu.windea.pls.lang.resolve.complexExpression.dsl.ParadoxComplexExpressionDslBuilder.buildExpression
import icu.windea.pls.lang.resolve.complexExpression.dsl.expression
import icu.windea.pls.lang.resolve.complexExpression.dsl.node
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxBlankNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxErrorTokenNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxOperatorNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxPredefinedCommandFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxSystemCommandScopeNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.StellarisNameFormatClosureNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.StellarisNameFormatDefinitionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.StellarisNameFormatLocalisationNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.StellarisNamePartNode
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.model.ParadoxGameType
import org.junit.Assert

@TestDataPath("\$CONTENT_ROOT/testData")
class StellarisNameFormatExpressionTest : ParadoxComplexExpressionTest() {
    override fun getTestDataPath() = "src/test/testData"

    private fun parse(
        text: String,
        formatName: String,
        gameType: ParadoxGameType = ParadoxGameType.Stellaris,
        incomplete: Boolean = false,
    ): StellarisNameFormatExpression? {
        val g = initConfigGroup(gameType)
        if (incomplete) PlsCoreManager.incompleteComplexExpression.set(true) else PlsCoreManager.incompleteComplexExpression.remove()
        val cfg = CwtValueConfig.resolve(emptyPointer(), g, "stellaris_name_format[$formatName]")
        return StellarisNameFormatExpression.resolve(text, TextRange(0, text.length), g, cfg)
    }

    fun testBasic_empire1() {
        val s = "{<eater_adj> {<patron_noun>}}"
        val exp = parse(s, formatName = "empire")!!
        // println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>(s, 0..s.length) {
            node<StellarisNameFormatClosureNode>(s, 0..29) {
                node<ParadoxMarkerNode>("{", 0..1)
                node<StellarisNamePartNode>("<eater_adj>", 1..12) {
                    node<ParadoxMarkerNode>("<", 1..2)
                    node<StellarisNameFormatDefinitionNode>("eater_adj", 2..11)
                    node<ParadoxMarkerNode>(">", 11..12)
                }
                node<ParadoxBlankNode>(" ", 12..13)
                node<StellarisNameFormatClosureNode>("{<patron_noun>}", 13..28) {
                    node<ParadoxMarkerNode>("{", 13..14)
                    node<StellarisNamePartNode>("<patron_noun>", 14..27) {
                        node<ParadoxMarkerNode>("<", 14..15)
                        node<StellarisNameFormatDefinitionNode>("patron_noun", 15..26)
                        node<ParadoxMarkerNode>(">", 26..27)
                    }
                    node<ParadoxMarkerNode>("}", 27..28)
                }
                node<ParadoxMarkerNode>("}", 28..29)
            }
        }
        exp.check(dsl)
    }

    fun testBasic_empire2() {
        val s = "{AofB{<imperial_mil> [This.GetCapitalSystemNameOrRandom]}}"
        val exp = parse(s, formatName = "empire")!!
        // println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>(s, 0..s.length) {
            node<StellarisNameFormatClosureNode>(s, 0..58) {
                node<ParadoxMarkerNode>("{", 0..1)
                node<StellarisNameFormatLocalisationNode>("AofB", 1..5)
                node<StellarisNameFormatClosureNode>("{<imperial_mil> [This.GetCapitalSystemNameOrRandom]}", 5..57) {
                    node<ParadoxMarkerNode>("{", 5..6)
                    node<StellarisNamePartNode>("<imperial_mil>", 6..20) {
                        node<ParadoxMarkerNode>("<", 6..7)
                        node<StellarisNameFormatDefinitionNode>("imperial_mil", 7..19)
                        node<ParadoxMarkerNode>(">", 19..20)
                    }
                    node<ParadoxBlankNode>(" ", 20..21)
                    node<ParadoxMarkerNode>("[", 21..22)
                    expression<ParadoxCommandExpression>("This.GetCapitalSystemNameOrRandom", 22..55) {
                        node<ParadoxSystemCommandScopeNode>("This", 22..26)
                        node<ParadoxOperatorNode>(".", 26..27)
                        node<ParadoxPredefinedCommandFieldNode>("GetCapitalSystemNameOrRandom", 27..55)
                    }
                    node<ParadoxMarkerNode>("]", 55..56)
                    node<ParadoxMarkerNode>("}", 56..57)
                }
                node<ParadoxMarkerNode>("}", 57..58)
            }
        }
        exp.check(dsl)
    }

    fun testBasic_empire3() {
        val s = "{<home_planet> Fleet}"
        val exp = parse(s, formatName = "empire")!!
        // println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>("{<home_planet> Fleet}", 0..21) {
            node<StellarisNameFormatClosureNode>("{<home_planet> Fleet}", 0..21) {
                node<ParadoxMarkerNode>("{", 0..1)
                node<StellarisNamePartNode>("<home_planet>", 1..14) {
                    node<ParadoxMarkerNode>("<", 1..2)
                    node<StellarisNameFormatDefinitionNode>("home_planet", 2..13)
                    node<ParadoxMarkerNode>(">", 13..14)
                }
                node<ParadoxBlankNode>(" ", 14..15)
                node<StellarisNameFormatLocalisationNode>("Fleet", 15..20)
                node<ParadoxMarkerNode>("}", 20..21)
            }
        }
        exp.check(dsl)
    }

    fun testBasic_federation() {
        val s = "{<union_adj> Council}"
        val exp = parse(s, formatName = "federation")!!
        // println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>("{<union_adj> Council}", 0..21) {
            node<StellarisNameFormatClosureNode>("{<union_adj> Council}", 0..21) {
                node<ParadoxMarkerNode>("{", 0..1)
                node<StellarisNamePartNode>("<union_adj>", 1..12) {
                    node<ParadoxMarkerNode>("<", 1..2)
                    node<StellarisNameFormatDefinitionNode>("union_adj", 2..11)
                    node<ParadoxMarkerNode>(">", 11..12)
                }
                node<ParadoxBlankNode>(" ", 12..13)
                node<StellarisNameFormatLocalisationNode>("Council", 13..20)
                node<ParadoxMarkerNode>("}", 20..21)
            }
        }
        exp.check(dsl)
    }

    fun testEmpty_incompleteDiff() {
        Assert.assertNull(parse("", formatName = "empire", incomplete = false))
        val exp = parse("", formatName = "empire", incomplete = true)!!
        // println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>("", 0..0)
        exp.check(dsl)
    }

    // Strict DSL checks — ensure Marker vs ErrorToken are strictly distinguished

    fun testStrict_braces_empty() {
        val s = "{}"
        val exp = parse(s, formatName = "empire")!!
        // println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>(s, 0..s.length) {
            node<StellarisNameFormatClosureNode>("{}", 0..2) {
                node<ParadoxMarkerNode>("{", 0..1)
                node<ParadoxMarkerNode>("}", 1..2)
            }
        }
        exp.check(dsl)
    }

    fun testStrict_localisation_simple() {
        val s = "{alpha}"
        val exp = parse(s, formatName = "empire")!!
        // println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>(s, 0..s.length) {
            node<StellarisNameFormatClosureNode>(s, 0..7) {
                node<ParadoxMarkerNode>("{", 0..1)
                node<StellarisNameFormatLocalisationNode>("alpha", 1..6)
                node<ParadoxMarkerNode>("}", 6..7)
            }
        }
        exp.check(dsl)
    }

    fun testStrict_definition_simple() {
        val s = "{<x> y}"
        val exp = parse(s, formatName = "empire")!!
        // println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>(s, 0..s.length) {
            node<StellarisNameFormatClosureNode>(s, 0..7) {
                node<ParadoxMarkerNode>("{", 0..1)
                node<StellarisNamePartNode>("<x>", 1..4) {
                    node<ParadoxMarkerNode>("<", 1..2)
                    node<StellarisNameFormatDefinitionNode>("x", 2..3)
                    node<ParadoxMarkerNode>(">", 3..4)
                }
                node<ParadoxBlankNode>(" ", 4..5)
                node<StellarisNameFormatLocalisationNode>("y", 5..6)
                node<ParadoxMarkerNode>("}", 6..7)
            }
        }
        exp.check(dsl)
    }

    fun testStrict_command_simple() {
        val s = "{[Root.GetName]}"
        val exp = parse(s, formatName = "empire")!!
        // println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>(s, 0..s.length) {
            node<StellarisNameFormatClosureNode>(s, 0..16) {
                node<ParadoxMarkerNode>("{", 0..1)
                node<ParadoxMarkerNode>("[", 1..2)
                expression<ParadoxCommandExpression>("Root.GetName", 2..14) {
                    node<ParadoxSystemCommandScopeNode>("Root", 2..6)
                    node<ParadoxOperatorNode>(".", 6..7)
                    node<ParadoxPredefinedCommandFieldNode>("GetName", 7..14)
                }
                node<ParadoxMarkerNode>("]", 14..15)
                node<ParadoxMarkerNode>("}", 15..16)
            }
        }
        exp.check(dsl)
    }

    fun testStrict_nested_mixed() {
        val s = "{X{<Y> [Root.GetName]}}"
        val exp = parse(s, formatName = "empire")!!
        // println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>(s, 0..s.length) {
            node<StellarisNameFormatClosureNode>(s, 0..23) {
                node<ParadoxMarkerNode>("{", 0..1)
                node<StellarisNameFormatLocalisationNode>("X", 1..2)
                node<StellarisNameFormatClosureNode>("{<Y> [Root.GetName]}", 2..22) {
                    node<ParadoxMarkerNode>("{", 2..3)
                    node<StellarisNamePartNode>("<Y>", 3..6) {
                        node<ParadoxMarkerNode>("<", 3..4)
                        node<StellarisNameFormatDefinitionNode>("Y", 4..5)
                        node<ParadoxMarkerNode>(">", 5..6)
                    }
                    node<ParadoxBlankNode>(" ", 6..7)
                    node<ParadoxMarkerNode>("[", 7..8)
                    expression<ParadoxCommandExpression>("Root.GetName", 8..20) {
                        node<ParadoxSystemCommandScopeNode>("Root", 8..12)
                        node<ParadoxOperatorNode>(".", 12..13)
                        node<ParadoxPredefinedCommandFieldNode>("GetName", 13..20)
                    }
                    node<ParadoxMarkerNode>("]", 20..21)
                    node<ParadoxMarkerNode>("}", 21..22)
                }
                node<ParadoxMarkerNode>("}", 22..23)
            }
        }
        exp.check(dsl)
    }

    fun testStrict_error_unmatched_angle() {
        val s = "{<abc"
        val exp = parse(s, formatName = "empire")!!
        // println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>(s, 0..s.length) {
            node<StellarisNameFormatClosureNode>(s, 0..5) {
                node<ParadoxMarkerNode>("{", 0..1)
                node<StellarisNamePartNode>("<abc", 1..5) {
                    node<ParadoxMarkerNode>("<", 1..2)
                    node<ParadoxErrorTokenNode>("abc", 2..5)
                }
            }
        }
        exp.check(dsl)
    }

    fun testStrict_error_unmatched_bracket() {
        val s = "{[Root."
        val exp = parse(s, formatName = "empire")!!
        // println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>(s, 0..s.length) {
            node<StellarisNameFormatClosureNode>(s, 0..7) {
                node<ParadoxMarkerNode>("{", 0..1)
                node<ParadoxMarkerNode>("[", 1..2)
                node<ParadoxErrorTokenNode>("Root.", 2..7)
            }
        }
        exp.check(dsl)
    }

    fun testStrict_text_and_blanks() {
        val s = "{Alpha Beta}"
        val exp = parse(s, formatName = "empire")!!
        // println(exp.render())
        val dsl = buildExpression<StellarisNameFormatExpression>(s, 0..s.length) {
            node<StellarisNameFormatClosureNode>(s, 0..12) {
                node<ParadoxMarkerNode>("{", 0..1)
                node<StellarisNameFormatLocalisationNode>("Alpha", 1..6)
                node<ParadoxBlankNode>(" ", 6..7)
                node<StellarisNameFormatLocalisationNode>("Beta", 7..11)
                node<ParadoxMarkerNode>("}", 11..12)
            }
        }
        exp.check(dsl)
    }
}

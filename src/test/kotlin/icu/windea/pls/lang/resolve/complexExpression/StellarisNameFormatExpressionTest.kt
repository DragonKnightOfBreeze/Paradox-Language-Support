package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.testFramework.TestDataPath
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.resolve.complexExpression.dsl.*
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxBlankNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandFieldValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDataSourceNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicCommandFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxErrorTokenNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxOperatorNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxPredefinedCommandFieldNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxSystemCommandScopeNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.StellarisNameFormatClosureNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.StellarisNameFormatDefinitionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.StellarisNameFormatLocalisationNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.StellarisNamePartNode
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.initConfigGroups
import icu.windea.pls.test.markIntegrationTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class StellarisNameFormatExpressionTest : ParadoxComplexExpressionTest() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() {
        markIntegrationTest()
        initConfigGroups(project, ParadoxGameType.Stellaris)
    }

    private fun parse(
        text: String,
        formatName: String,
        gameType: ParadoxGameType = ParadoxGameType.Stellaris,
        incomplete: Boolean = false,
    ): StellarisNameFormatExpression? {
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        if (incomplete) PlsStates.incompleteComplexExpression.set(true) else PlsStates.incompleteComplexExpression.remove()
        val cfg = CwtValueConfig.createMock(configGroup, "stellaris_name_format[$formatName]")
        return StellarisNameFormatExpression.resolve(text, null, configGroup, cfg)
    }

    @Test
    fun testBasic_empire1() {
        val s = "{<eater_adj> {<patron_noun>}}"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildComplexExpression<StellarisNameFormatExpression>(s, 0 to s.length) {
            node<StellarisNameFormatClosureNode>(s, 0 to 29) {
                node<ParadoxMarkerNode>("{", 0 to 1)
                node<StellarisNamePartNode>("<eater_adj>", 1 to 12) {
                    node<ParadoxMarkerNode>("<", 1 to 2)
                    node<StellarisNameFormatDefinitionNode>("eater_adj", 2 to 11)
                    node<ParadoxMarkerNode>(">", 11 to 12)
                }
                node<ParadoxBlankNode>(" ", 12 to 13)
                node<StellarisNameFormatClosureNode>("{<patron_noun>}", 13 to 28) {
                    node<ParadoxMarkerNode>("{", 13 to 14)
                    node<StellarisNamePartNode>("<patron_noun>", 14 to 27) {
                        node<ParadoxMarkerNode>("<", 14 to 15)
                        node<StellarisNameFormatDefinitionNode>("patron_noun", 15 to 26)
                        node<ParadoxMarkerNode>(">", 26 to 27)
                    }
                    node<ParadoxMarkerNode>("}", 27 to 28)
                }
                node<ParadoxMarkerNode>("}", 28 to 29)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testBasic_empire2() {
        val s = "{AofB{<imperial_mil> [This.GetCapitalSystemNameOrRandom]}}"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildComplexExpression<StellarisNameFormatExpression>(s, 0 to s.length) {
            node<StellarisNameFormatClosureNode>(s, 0 to 58) {
                node<ParadoxMarkerNode>("{", 0 to 1)
                node<StellarisNameFormatLocalisationNode>("AofB", 1 to 5)
                node<StellarisNameFormatClosureNode>("{<imperial_mil> [This.GetCapitalSystemNameOrRandom]}", 5 to 57) {
                    node<ParadoxMarkerNode>("{", 5 to 6)
                    node<StellarisNamePartNode>("<imperial_mil>", 6 to 20) {
                        node<ParadoxMarkerNode>("<", 6 to 7)
                        node<StellarisNameFormatDefinitionNode>("imperial_mil", 7 to 19)
                        node<ParadoxMarkerNode>(">", 19 to 20)
                    }
                    node<ParadoxBlankNode>(" ", 20 to 21)
                    node<ParadoxCommandNode>("[This.GetCapitalSystemNameOrRandom]", 21 to 56) {
                        node<ParadoxMarkerNode>("[", 21 to 22)
                        expression<ParadoxCommandExpression>("This.GetCapitalSystemNameOrRandom", 22 to 55) {
                            node<ParadoxSystemCommandScopeNode>("This", 22 to 26)
                            node<ParadoxOperatorNode>(".", 26 to 27)
                            node<ParadoxPredefinedCommandFieldNode>("GetCapitalSystemNameOrRandom", 27 to 55)
                        }
                        node<ParadoxMarkerNode>("]", 55 to 56)
                    }
                    node<ParadoxMarkerNode>("}", 56 to 57)
                }
                node<ParadoxMarkerNode>("}", 57 to 58)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testBasic_empire3() {
        val s = "{<home_planet> Fleet}"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildComplexExpression<StellarisNameFormatExpression>("{<home_planet> Fleet}", 0 to 21) {
            node<StellarisNameFormatClosureNode>("{<home_planet> Fleet}", 0 to 21) {
                node<ParadoxMarkerNode>("{", 0 to 1)
                node<StellarisNamePartNode>("<home_planet>", 1 to 14) {
                    node<ParadoxMarkerNode>("<", 1 to 2)
                    node<StellarisNameFormatDefinitionNode>("home_planet", 2 to 13)
                    node<ParadoxMarkerNode>(">", 13 to 14)
                }
                node<ParadoxBlankNode>(" ", 14 to 15)
                node<StellarisNameFormatLocalisationNode>("Fleet", 15 to 20)
                node<ParadoxMarkerNode>("}", 20 to 21)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testBasic_federation() {
        val s = "{<union_adj> Council}"
        val exp = parse(s, formatName = "federation")!!
        println(exp.render())
        val dsl = buildComplexExpression<StellarisNameFormatExpression>("{<union_adj> Council}", 0 to 21) {
            node<StellarisNameFormatClosureNode>("{<union_adj> Council}", 0 to 21) {
                node<ParadoxMarkerNode>("{", 0 to 1)
                node<StellarisNamePartNode>("<union_adj>", 1 to 12) {
                    node<ParadoxMarkerNode>("<", 1 to 2)
                    node<StellarisNameFormatDefinitionNode>("union_adj", 2 to 11)
                    node<ParadoxMarkerNode>(">", 11 to 12)
                }
                node<ParadoxBlankNode>(" ", 12 to 13)
                node<StellarisNameFormatLocalisationNode>("Council", 13 to 20)
                node<ParadoxMarkerNode>("}", 20 to 21)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testEmpty_incompleteDiff() {
        Assert.assertNull(parse("", formatName = "empire", incomplete = false))
        val exp = parse("", formatName = "empire", incomplete = true)!!
        println(exp.render())
        val dsl = buildComplexExpression<StellarisNameFormatExpression>("", 0 to 0) { }
        exp.check(dsl)
    }

    // Strict DSL checks — ensure Marker vs ErrorToken are strictly distinguished

    @Test
    fun testStrict_braces_empty() {
        val s = "{}"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildComplexExpression<StellarisNameFormatExpression>(s, 0 to s.length) {
            node<StellarisNameFormatClosureNode>("{}", 0 to 2) {
                node<ParadoxMarkerNode>("{", 0 to 1)
                node<ParadoxMarkerNode>("}", 1 to 2)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_localisation_simple() {
        val s = "{alpha}"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildComplexExpression<StellarisNameFormatExpression>(s, 0 to s.length) {
            node<StellarisNameFormatClosureNode>(s, 0 to 7) {
                node<ParadoxMarkerNode>("{", 0 to 1)
                node<StellarisNameFormatLocalisationNode>("alpha", 1 to 6)
                node<ParadoxMarkerNode>("}", 6 to 7)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_definition_simple() {
        val s = "{<x> y}"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildComplexExpression<StellarisNameFormatExpression>(s, 0 to s.length) {
            node<StellarisNameFormatClosureNode>(s, 0 to 7) {
                node<ParadoxMarkerNode>("{", 0 to 1)
                node<StellarisNamePartNode>("<x>", 1 to 4) {
                    node<ParadoxMarkerNode>("<", 1 to 2)
                    node<StellarisNameFormatDefinitionNode>("x", 2 to 3)
                    node<ParadoxMarkerNode>(">", 3 to 4)
                }
                node<ParadoxBlankNode>(" ", 4 to 5)
                node<StellarisNameFormatLocalisationNode>("y", 5 to 6)
                node<ParadoxMarkerNode>("}", 6 to 7)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_command_simple() {
        val s = "{[Root.GetName]}"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildComplexExpression<StellarisNameFormatExpression>(s, 0 to s.length) {
            node<StellarisNameFormatClosureNode>(s, 0 to 16) {
                node<ParadoxMarkerNode>("{", 0 to 1)
                node<ParadoxCommandNode>("[Root.GetName]", 1 to 15) {
                    node<ParadoxMarkerNode>("[", 1 to 2)
                    expression<ParadoxCommandExpression>("Root.GetName", 2 to 14) {
                        node<ParadoxSystemCommandScopeNode>("Root", 2 to 6)
                        node<ParadoxOperatorNode>(".", 6 to 7)
                        node<ParadoxPredefinedCommandFieldNode>("GetName", 7 to 14)
                    }
                    node<ParadoxMarkerNode>("]", 14 to 15)
                }
                node<ParadoxMarkerNode>("}", 15 to 16)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_nested_mixed() {
        val s = "{X{<Y> [Root.GetName]}}"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildComplexExpression<StellarisNameFormatExpression>(s, 0 to s.length) {
            node<StellarisNameFormatClosureNode>(s, 0 to 23) {
                node<ParadoxMarkerNode>("{", 0 to 1)
                node<StellarisNameFormatLocalisationNode>("X", 1 to 2)
                node<StellarisNameFormatClosureNode>("{<Y> [Root.GetName]}", 2 to 22) {
                    node<ParadoxMarkerNode>("{", 2 to 3)
                    node<StellarisNamePartNode>("<Y>", 3 to 6) {
                        node<ParadoxMarkerNode>("<", 3 to 4)
                        node<StellarisNameFormatDefinitionNode>("Y", 4 to 5)
                        node<ParadoxMarkerNode>(">", 5 to 6)
                    }
                    node<ParadoxBlankNode>(" ", 6 to 7)
                    node<ParadoxCommandNode>("[Root.GetName]", 7 to 21) {
                        node<ParadoxMarkerNode>("[", 7 to 8)
                        expression<ParadoxCommandExpression>("Root.GetName", 8 to 20) {
                            node<ParadoxSystemCommandScopeNode>("Root", 8 to 12)
                            node<ParadoxOperatorNode>(".", 12 to 13)
                            node<ParadoxPredefinedCommandFieldNode>("GetName", 13 to 20)
                        }
                        node<ParadoxMarkerNode>("]", 20 to 21)
                    }
                    node<ParadoxMarkerNode>("}", 21 to 22)
                }
                node<ParadoxMarkerNode>("}", 22 to 23)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_error_unmatched_angle() {
        // NOTE 顶层与闭包层的“空错误节点”逻辑是幂等的：若子层已在当前层末位放置了错误节点（例如 [ 未闭合导致的当前层末尾空错误），则闭包层不会再重复添加。

        val s = "{<abc"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildComplexExpression<StellarisNameFormatExpression>(s, 0 to s.length) {
            node<StellarisNameFormatClosureNode>(s, 0 to 5) {
                node<ParadoxMarkerNode>("{", 0 to 1)
                node<StellarisNamePartNode>("<abc", 1 to 5) {
                    node<ParadoxMarkerNode>("<", 1 to 2)
                    node<StellarisNameFormatDefinitionNode>("abc", 2 to 5)
                    node<ParadoxErrorTokenNode>("", 5 to 5)
                }
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_error_unmatched_bracket() {
        // NOTE 顶层与闭包层的“空错误节点”逻辑是幂等的：若子层已在当前层末位放置了错误节点（例如 [ 未闭合导致的当前层末尾空错误），则闭包层不会再重复添加。

        val s = "{[Root."
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildComplexExpression<StellarisNameFormatExpression>(s, 0 to s.length) {
            node<StellarisNameFormatClosureNode>(s, 0 to 7) {
                node<ParadoxMarkerNode>("{", 0 to 1)
                node<ParadoxCommandNode>("[Root.", 1 to 7) {
                    node<ParadoxMarkerNode>("[", 1 to 2)
                    expression<ParadoxCommandExpression>("Root.", 2 to 7) {
                        node<ParadoxSystemCommandScopeNode>("Root", 2 to 6)
                        node<ParadoxOperatorNode>(".", 6 to 7)
                        node<ParadoxDynamicCommandFieldNode>("", 7 to 7) {
                            node<ParadoxCommandFieldValueNode>("", 7 to 7) {
                                node<ParadoxDataSourceNode>("", 7 to 7)
                            }
                        }
                    }
                    node<ParadoxErrorTokenNode>("", 7 to 7)
                }
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_text_and_blanks() {
        val s = "{Alpha Beta}"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildComplexExpression<StellarisNameFormatExpression>(s, 0 to s.length) {
            node<StellarisNameFormatClosureNode>(s, 0 to 12) {
                node<ParadoxMarkerNode>("{", 0 to 1)
                node<StellarisNameFormatLocalisationNode>("Alpha", 1 to 6)
                node<ParadoxBlankNode>(" ", 6 to 7)
                node<StellarisNameFormatLocalisationNode>("Beta", 7 to 11)
                node<ParadoxMarkerNode>("}", 11 to 12)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_top_level_blanks_and_closure() {
        val s = "   { <x> y  }   "
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildComplexExpression<StellarisNameFormatExpression>(s, 0 to s.length) {
            node<ParadoxBlankNode>("   ", 0 to 3)
            node<StellarisNameFormatClosureNode>("{ <x> y  }", 3 to 13) {
                node<ParadoxMarkerNode>("{", 3 to 4)
                node<ParadoxBlankNode>(" ", 4 to 5)
                node<StellarisNamePartNode>("<x>", 5 to 8) {
                    node<ParadoxMarkerNode>("<", 5 to 6)
                    node<StellarisNameFormatDefinitionNode>("x", 6 to 7)
                    node<ParadoxMarkerNode>(">", 7 to 8)
                }
                node<ParadoxBlankNode>(" ", 8 to 9)
                node<StellarisNameFormatLocalisationNode>("y", 9 to 10)
                node<ParadoxBlankNode>("  ", 10 to 12)
                node<ParadoxMarkerNode>("}", 12 to 13)
            }
            node<ParadoxBlankNode>("   ", 13 to 16)
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_top_level_errors_and_closure() {
        val s = "foo { <x> y  } <bar> "
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildComplexExpression<StellarisNameFormatExpression>(s, 0 to s.length) {
            node<ParadoxErrorTokenNode>("foo", 0 to 3)
            node<ParadoxBlankNode>(" ", 3 to 4)
            node<StellarisNameFormatClosureNode>("{ <x> y  }", 4 to 14) {
                node<ParadoxMarkerNode>("{", 4 to 5)
                node<ParadoxBlankNode>(" ", 5 to 6)
                node<StellarisNamePartNode>("<x>", 6 to 9) {
                    node<ParadoxMarkerNode>("<", 6 to 7)
                    node<StellarisNameFormatDefinitionNode>("x", 7 to 8)
                    node<ParadoxMarkerNode>(">", 8 to 9)
                }
                node<ParadoxBlankNode>(" ", 9 to 10)
                node<StellarisNameFormatLocalisationNode>("y", 10 to 11)
                node<ParadoxBlankNode>("  ", 11 to 13)
                node<ParadoxMarkerNode>("}", 13 to 14)
            }
            node<ParadoxBlankNode>(" ", 14 to 15)
            node<ParadoxErrorTokenNode>("<bar>", 15 to 20)
            node<ParadoxBlankNode>(" ", 20 to 21)
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_no_open_brace_whole_error() {
        val s = "<x> y} "
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildComplexExpression<StellarisNameFormatExpression>(s, 0 to s.length) {
            node<ParadoxErrorTokenNode>("<x> y}", 0 to 6)
            node<ParadoxBlankNode>(" ", 6 to 7)
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_stray_close_in_closure() {
        val s = "{x> y}"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildComplexExpression<StellarisNameFormatExpression>(s, 0 to s.length) {
            node<StellarisNameFormatClosureNode>(s, 0 to 6) {
                node<ParadoxMarkerNode>("{", 0 to 1)
                node<StellarisNameFormatLocalisationNode>("x", 1 to 2)
                node<ParadoxErrorTokenNode>(">", 2 to 3)
                node<ParadoxBlankNode>(" ", 3 to 4)
                node<StellarisNameFormatLocalisationNode>("y", 4 to 5)
                node<ParadoxMarkerNode>("}", 5 to 6)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_extra_close_top_level() {
        val s = "{<x>> y}}"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildComplexExpression<StellarisNameFormatExpression>(s, 0 to s.length) {
            node<StellarisNameFormatClosureNode>("{<x>> y}", 0 to 8) {
                node<ParadoxMarkerNode>("{", 0 to 1)
                node<StellarisNamePartNode>("<x>", 1 to 4) {
                    node<ParadoxMarkerNode>("<", 1 to 2)
                    node<StellarisNameFormatDefinitionNode>("x", 2 to 3)
                    node<ParadoxMarkerNode>(">", 3 to 4)
                }
                node<ParadoxErrorTokenNode>(">", 4 to 5)
                node<ParadoxBlankNode>(" ", 5 to 6)
                node<StellarisNameFormatLocalisationNode>("y", 6 to 7)
                node<ParadoxMarkerNode>("}", 7 to 8)
            }
            node<ParadoxErrorTokenNode>("}", 8 to 9)
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_empty_command_in_closure() {
        val s = "{[]}"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildComplexExpression<StellarisNameFormatExpression>(s, 0 to s.length) {
            node<StellarisNameFormatClosureNode>(s, 0 to 4) {
                node<ParadoxMarkerNode>("{", 0 to 1)
                node<ParadoxCommandNode>("[]", 1 to 3) {
                    node<ParadoxMarkerNode>("[", 1 to 2)
                    node<ParadoxErrorTokenNode>("", 2 to 2)
                    node<ParadoxMarkerNode>("]", 2 to 3)
                }
                node<ParadoxMarkerNode>("}", 3 to 4)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_empty_definition_in_closure() {
        val s = "{<>}"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildComplexExpression<StellarisNameFormatExpression>(s, 0 to s.length) {
            node<StellarisNameFormatClosureNode>(s, 0 to 4) {
                node<ParadoxMarkerNode>("{", 0 to 1)
                node<StellarisNamePartNode>("<>", 1 to 3) {
                    node<ParadoxMarkerNode>("<", 1 to 2)
                    node<ParadoxErrorTokenNode>("", 2 to 2)
                    node<ParadoxMarkerNode>(">", 2 to 3)
                }
                node<ParadoxMarkerNode>("}", 3 to 4)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_unmatched_angle_stop_at_space() {
        val s = "{<abc y}"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildComplexExpression<StellarisNameFormatExpression>(s, 0 to s.length) {
            node<StellarisNameFormatClosureNode>(s, 0 to 8) {
                node<ParadoxMarkerNode>("{", 0 to 1)
                node<StellarisNamePartNode>("<abc", 1 to 5) {
                    node<ParadoxMarkerNode>("<", 1 to 2)
                    node<StellarisNameFormatDefinitionNode>("abc", 2 to 5)
                    node<ParadoxErrorTokenNode>("", 5 to 5)
                }
                node<ParadoxBlankNode>(" ", 5 to 6)
                node<StellarisNameFormatLocalisationNode>("y", 6 to 7)
                node<ParadoxMarkerNode>("}", 7 to 8)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_unmatched_bracket_stop_at_space() {
        val s = "{[Root. y}"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildComplexExpression<StellarisNameFormatExpression>(s, 0 to s.length) {
            node<StellarisNameFormatClosureNode>("{[Root. y}", 0 to 10) {
                node<ParadoxMarkerNode>("{", 0 to 1)
                node<ParadoxCommandNode>("[Root.", 1 to 7) {
                    node<ParadoxMarkerNode>("[", 1 to 2)
                    expression<ParadoxCommandExpression>("Root.", 2 to 7) {
                        node<ParadoxSystemCommandScopeNode>("Root", 2 to 6)
                        node<ParadoxOperatorNode>(".", 6 to 7)
                        node<ParadoxDynamicCommandFieldNode>("", 7 to 7) {
                            node<ParadoxCommandFieldValueNode>("", 7 to 7) {
                                node<ParadoxDataSourceNode>("", 7 to 7)
                            }
                        }
                    }
                    node<ParadoxErrorTokenNode>("", 7 to 7)
                }
                node<ParadoxBlankNode>(" ", 7 to 8)
                node<StellarisNameFormatLocalisationNode>("y", 8 to 9)
                node<ParadoxMarkerNode>("}", 9 to 10)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_stray_close_bracket_in_closure() {
        val s = "{x] y}"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildComplexExpression<StellarisNameFormatExpression>(s, 0 to s.length) {
            node<StellarisNameFormatClosureNode>(s, 0 to 6) {
                node<ParadoxMarkerNode>("{", 0 to 1)
                node<StellarisNameFormatLocalisationNode>("x", 1 to 2)
                node<ParadoxErrorTokenNode>("]", 2 to 3)
                node<ParadoxBlankNode>(" ", 3 to 4)
                node<StellarisNameFormatLocalisationNode>("y", 4 to 5)
                node<ParadoxMarkerNode>("}", 5 to 6)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_top_level_after_closure_text() {
        val s = "{x}y"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildComplexExpression<StellarisNameFormatExpression>(s, 0 to s.length) {
            node<StellarisNameFormatClosureNode>("{x}", 0 to 3) {
                node<ParadoxMarkerNode>("{", 0 to 1)
                node<StellarisNameFormatLocalisationNode>("x", 1 to 2)
                node<ParadoxMarkerNode>("}", 2 to 3)
            }
            node<ParadoxErrorTokenNode>("y", 3 to 4)
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_braces_blank_inside() {
        val s = "{ }"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildComplexExpression<StellarisNameFormatExpression>(s, 0 to s.length) {
            node<StellarisNameFormatClosureNode>(s, 0 to 3) {
                node<ParadoxMarkerNode>("{", 0 to 1)
                node<ParadoxBlankNode>(" ", 1 to 2)
                node<ParadoxMarkerNode>("}", 2 to 3)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun testStrict_command_adjacent_no_space() {
        val s = "{x[Root.GetName]}"
        val exp = parse(s, formatName = "empire")!!
        println(exp.render())
        val dsl = buildComplexExpression<StellarisNameFormatExpression>(s, 0 to s.length) {
            node<StellarisNameFormatClosureNode>(s, 0 to 17) {
                node<ParadoxMarkerNode>("{", 0 to 1)
                node<StellarisNameFormatLocalisationNode>("x", 1 to 2)
                node<ParadoxCommandNode>("[Root.GetName]", 2 to 16) {
                    node<ParadoxMarkerNode>("[", 2 to 3)
                    expression<ParadoxCommandExpression>("Root.GetName", 3 to 15) {
                        node<ParadoxSystemCommandScopeNode>("Root", 3 to 7)
                        node<ParadoxOperatorNode>(".", 7 to 8)
                        node<ParadoxPredefinedCommandFieldNode>("GetName", 8 to 15)
                    }
                    node<ParadoxMarkerNode>("]", 15 to 16)
                }
                node<ParadoxMarkerNode>("}", 16 to 17)
            }
        }
        exp.check(dsl)
    }
}

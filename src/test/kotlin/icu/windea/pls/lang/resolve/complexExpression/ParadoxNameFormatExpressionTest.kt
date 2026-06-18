package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.testFramework.TestDataPath
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.base.context.ChronicleThreadContext
import icu.windea.pls.lang.resolve.complexExpression.dsl.*
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.initConfigGroups
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxNameFormatExpressionTest : ParadoxComplexExpressionTest() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        initConfigGroups(project, ParadoxGameType.Stellaris)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    private fun resolve(text: String, formatName: String, gameType: ParadoxGameType = ParadoxGameType.Stellaris, incomplete: Boolean = false): ParadoxNameFormatExpression? {
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        if (incomplete) ChronicleThreadContext.incompleteComplexExpression.set(true) else ChronicleThreadContext.incompleteComplexExpression.remove()
        val cfg = CwtValueConfig.createMock(configGroup, "name_format[$formatName]")
        return ParadoxNameFormatExpression.resolve(text, null, configGroup, cfg)
    }

    @Test
    fun test_basic_empire1() {
        val s = "{<eater_adj> {<patron_noun>}}"
        val exp = resolve(s, formatName = "empire")!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxNameFormatExpression>(s, 0, s.length) {
            node<ParadoxNameFormatClosureNode>(s, 0, 29) {
                node<ParadoxMarkerNode>("{", 0, 1)
                node<ParadoxNamePartNode>("<eater_adj>", 1, 12) {
                    node<ParadoxMarkerNode>("<", 1, 2)
                    node<ParadoxNameFormatDefinitionNode>("eater_adj", 2, 11)
                    node<ParadoxMarkerNode>(">", 11, 12)
                }
                node<ParadoxBlankNode>(" ", 12, 13)
                node<ParadoxNameFormatClosureNode>("{<patron_noun>}", 13, 28) {
                    node<ParadoxMarkerNode>("{", 13, 14)
                    node<ParadoxNamePartNode>("<patron_noun>", 14, 27) {
                        node<ParadoxMarkerNode>("<", 14, 15)
                        node<ParadoxNameFormatDefinitionNode>("patron_noun", 15, 26)
                        node<ParadoxMarkerNode>(">", 26, 27)
                    }
                    node<ParadoxMarkerNode>("}", 27, 28)
                }
                node<ParadoxMarkerNode>("}", 28, 29)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_basic_empire2() {
        val s = "{AofB{<imperial_mil> [This.GetCapitalSystemNameOrRandom]}}"
        val exp = resolve(s, formatName = "empire")!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxNameFormatExpression>(s, 0, s.length) {
            node<ParadoxNameFormatClosureNode>(s, 0, 58) {
                node<ParadoxMarkerNode>("{", 0, 1)
                node<ParadoxNameFormatLocalisationNode>("AofB", 1, 5)
                node<ParadoxNameFormatClosureNode>("{<imperial_mil> [This.GetCapitalSystemNameOrRandom]}", 5, 57) {
                    node<ParadoxMarkerNode>("{", 5, 6)
                    node<ParadoxNamePartNode>("<imperial_mil>", 6, 20) {
                        node<ParadoxMarkerNode>("<", 6, 7)
                        node<ParadoxNameFormatDefinitionNode>("imperial_mil", 7, 19)
                        node<ParadoxMarkerNode>(">", 19, 20)
                    }
                    node<ParadoxBlankNode>(" ", 20, 21)
                    node<ParadoxCommandNode>("[This.GetCapitalSystemNameOrRandom]", 21, 56) {
                        node<ParadoxMarkerNode>("[", 21, 22)
                        node<ParadoxCommandExpression>("This.GetCapitalSystemNameOrRandom", 22, 55) {
                            node<ParadoxSystemCommandScopeNode>("This", 22, 26)
                            node<ParadoxOperatorNode>(".", 26, 27)
                            node<ParadoxStaticCommandFieldNode>("GetCapitalSystemNameOrRandom", 27, 55)
                        }
                        node<ParadoxMarkerNode>("]", 55, 56)
                    }
                    node<ParadoxMarkerNode>("}", 56, 57)
                }
                node<ParadoxMarkerNode>("}", 57, 58)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_basic_empire3() {
        val s = "{<home_planet> Fleet}"
        val exp = resolve(s, formatName = "empire")!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxNameFormatExpression>("{<home_planet> Fleet}", 0, 21) {
            node<ParadoxNameFormatClosureNode>("{<home_planet> Fleet}", 0, 21) {
                node<ParadoxMarkerNode>("{", 0, 1)
                node<ParadoxNamePartNode>("<home_planet>", 1, 14) {
                    node<ParadoxMarkerNode>("<", 1, 2)
                    node<ParadoxNameFormatDefinitionNode>("home_planet", 2, 13)
                    node<ParadoxMarkerNode>(">", 13, 14)
                }
                node<ParadoxBlankNode>(" ", 14, 15)
                node<ParadoxNameFormatLocalisationNode>("Fleet", 15, 20)
                node<ParadoxMarkerNode>("}", 20, 21)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_basic_federation() {
        val s = "{<union_adj> Council}"
        val exp = resolve(s, formatName = "federation")!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxNameFormatExpression>("{<union_adj> Council}", 0, 21) {
            node<ParadoxNameFormatClosureNode>("{<union_adj> Council}", 0, 21) {
                node<ParadoxMarkerNode>("{", 0, 1)
                node<ParadoxNamePartNode>("<union_adj>", 1, 12) {
                    node<ParadoxMarkerNode>("<", 1, 2)
                    node<ParadoxNameFormatDefinitionNode>("union_adj", 2, 11)
                    node<ParadoxMarkerNode>(">", 11, 12)
                }
                node<ParadoxBlankNode>(" ", 12, 13)
                node<ParadoxNameFormatLocalisationNode>("Council", 13, 20)
                node<ParadoxMarkerNode>("}", 20, 21)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_empty_incompleteDiff() {
        Assert.assertNull(resolve("", formatName = "empire", incomplete = false))
        val exp = resolve("", formatName = "empire", incomplete = true)!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxNameFormatExpression>("", 0, 0) { }
        exp.check(dsl)
    }

    // Strict DSL checks — ensure Marker vs ErrorToken are strictly distinguished

    @Test
    fun test_strict_braces_empty() {
        val s = "{}"
        val exp = resolve(s, formatName = "empire")!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxNameFormatExpression>(s, 0, s.length) {
            node<ParadoxNameFormatClosureNode>("{}", 0, 2) {
                node<ParadoxMarkerNode>("{", 0, 1)
                node<ParadoxMarkerNode>("}", 1, 2)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_strict_localisation_simple() {
        val s = "{alpha}"
        val exp = resolve(s, formatName = "empire")!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxNameFormatExpression>(s, 0, s.length) {
            node<ParadoxNameFormatClosureNode>(s, 0, 7) {
                node<ParadoxMarkerNode>("{", 0, 1)
                node<ParadoxNameFormatLocalisationNode>("alpha", 1, 6)
                node<ParadoxMarkerNode>("}", 6, 7)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_strict_definition_simple() {
        val s = "{<x> y}"
        val exp = resolve(s, formatName = "empire")!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxNameFormatExpression>(s, 0, s.length) {
            node<ParadoxNameFormatClosureNode>(s, 0, 7) {
                node<ParadoxMarkerNode>("{", 0, 1)
                node<ParadoxNamePartNode>("<x>", 1, 4) {
                    node<ParadoxMarkerNode>("<", 1, 2)
                    node<ParadoxNameFormatDefinitionNode>("x", 2, 3)
                    node<ParadoxMarkerNode>(">", 3, 4)
                }
                node<ParadoxBlankNode>(" ", 4, 5)
                node<ParadoxNameFormatLocalisationNode>("y", 5, 6)
                node<ParadoxMarkerNode>("}", 6, 7)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_strict_command_simple() {
        val s = "{[Root.GetName]}"
        val exp = resolve(s, formatName = "empire")!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxNameFormatExpression>(s, 0, s.length) {
            node<ParadoxNameFormatClosureNode>(s, 0, 16) {
                node<ParadoxMarkerNode>("{", 0, 1)
                node<ParadoxCommandNode>("[Root.GetName]", 1, 15) {
                    node<ParadoxMarkerNode>("[", 1, 2)
                    node<ParadoxCommandExpression>("Root.GetName", 2, 14) {
                        node<ParadoxSystemCommandScopeNode>("Root", 2, 6)
                        node<ParadoxOperatorNode>(".", 6, 7)
                        node<ParadoxStaticCommandFieldNode>("GetName", 7, 14)
                    }
                    node<ParadoxMarkerNode>("]", 14, 15)
                }
                node<ParadoxMarkerNode>("}", 15, 16)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_strict_nested_mixed() {
        val s = "{X{<Y> [Root.GetName]}}"
        val exp = resolve(s, formatName = "empire")!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxNameFormatExpression>(s, 0, s.length) {
            node<ParadoxNameFormatClosureNode>(s, 0, 23) {
                node<ParadoxMarkerNode>("{", 0, 1)
                node<ParadoxNameFormatLocalisationNode>("X", 1, 2)
                node<ParadoxNameFormatClosureNode>("{<Y> [Root.GetName]}", 2, 22) {
                    node<ParadoxMarkerNode>("{", 2, 3)
                    node<ParadoxNamePartNode>("<Y>", 3, 6) {
                        node<ParadoxMarkerNode>("<", 3, 4)
                        node<ParadoxNameFormatDefinitionNode>("Y", 4, 5)
                        node<ParadoxMarkerNode>(">", 5, 6)
                    }
                    node<ParadoxBlankNode>(" ", 6, 7)
                    node<ParadoxCommandNode>("[Root.GetName]", 7, 21) {
                        node<ParadoxMarkerNode>("[", 7, 8)
                        node<ParadoxCommandExpression>("Root.GetName", 8, 20) {
                            node<ParadoxSystemCommandScopeNode>("Root", 8, 12)
                            node<ParadoxOperatorNode>(".", 12, 13)
                            node<ParadoxStaticCommandFieldNode>("GetName", 13, 20)
                        }
                        node<ParadoxMarkerNode>("]", 20, 21)
                    }
                    node<ParadoxMarkerNode>("}", 21, 22)
                }
                node<ParadoxMarkerNode>("}", 22, 23)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_strict_error_unmatched_angle() {
        // NOTE 顶层与闭包层的“空错误节点”逻辑是幂等的：若子层已在当前层末位放置了错误节点（例如 [ 未闭合导致的当前层末尾空错误），则闭包层不会再重复添加。

        val s = "{<abc"
        val exp = resolve(s, formatName = "empire")!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxNameFormatExpression>(s, 0, s.length) {
            node<ParadoxNameFormatClosureNode>(s, 0, 5) {
                node<ParadoxMarkerNode>("{", 0, 1)
                node<ParadoxNamePartNode>("<abc", 1, 5) {
                    node<ParadoxMarkerNode>("<", 1, 2)
                    node<ParadoxNameFormatDefinitionNode>("abc", 2, 5)
                    node<ParadoxErrorTokenNode>("", 5, 5)
                }
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_strict_error_unmatched_bracket() {
        // NOTE 顶层与闭包层的“空错误节点”逻辑是幂等的：若子层已在当前层末位放置了错误节点（例如 [ 未闭合导致的当前层末尾空错误），则闭包层不会再重复添加。

        val s = "{[Root."
        val exp = resolve(s, formatName = "empire")!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxNameFormatExpression>(s, 0, s.length) {
            node<ParadoxNameFormatClosureNode>(s, 0, 7) {
                node<ParadoxMarkerNode>("{", 0, 1)
                node<ParadoxCommandNode>("[Root.", 1, 7) {
                    node<ParadoxMarkerNode>("[", 1, 2)
                    node<ParadoxCommandExpression>("Root.", 2, 7) {
                        node<ParadoxSystemCommandScopeNode>("Root", 2, 6)
                        node<ParadoxOperatorNode>(".", 6, 7)
                        node<ParadoxDynamicCommandFieldNode>("", 7, 7) {
                            node<ParadoxCommandFieldValueNode>("", 7, 7) {
                                node<ParadoxDataSourceNode>("", 7, 7)
                            }
                        }
                    }
                    node<ParadoxErrorTokenNode>("", 7, 7)
                }
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_strict_text_and_blanks() {
        val s = "{Alpha Beta}"
        val exp = resolve(s, formatName = "empire")!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxNameFormatExpression>(s, 0, s.length) {
            node<ParadoxNameFormatClosureNode>(s, 0, 12) {
                node<ParadoxMarkerNode>("{", 0, 1)
                node<ParadoxNameFormatLocalisationNode>("Alpha", 1, 6)
                node<ParadoxBlankNode>(" ", 6, 7)
                node<ParadoxNameFormatLocalisationNode>("Beta", 7, 11)
                node<ParadoxMarkerNode>("}", 11, 12)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_strict_top_level_blanks_and_closure() {
        val s = "   { <x> y  }   "
        val exp = resolve(s, formatName = "empire")!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxNameFormatExpression>(s, 0, s.length) {
            node<ParadoxBlankNode>("   ", 0, 3)
            node<ParadoxNameFormatClosureNode>("{ <x> y  }", 3, 13) {
                node<ParadoxMarkerNode>("{", 3, 4)
                node<ParadoxBlankNode>(" ", 4, 5)
                node<ParadoxNamePartNode>("<x>", 5, 8) {
                    node<ParadoxMarkerNode>("<", 5, 6)
                    node<ParadoxNameFormatDefinitionNode>("x", 6, 7)
                    node<ParadoxMarkerNode>(">", 7, 8)
                }
                node<ParadoxBlankNode>(" ", 8, 9)
                node<ParadoxNameFormatLocalisationNode>("y", 9, 10)
                node<ParadoxBlankNode>("  ", 10, 12)
                node<ParadoxMarkerNode>("}", 12, 13)
            }
            node<ParadoxBlankNode>("   ", 13, 16)
        }
        exp.check(dsl)
    }

    @Test
    fun test_strict_top_level_errors_and_closure() {
        val s = "foo { <x> y  } <bar> "
        val exp = resolve(s, formatName = "empire")!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxNameFormatExpression>(s, 0, s.length) {
            node<ParadoxErrorTokenNode>("foo", 0, 3)
            node<ParadoxBlankNode>(" ", 3, 4)
            node<ParadoxNameFormatClosureNode>("{ <x> y  }", 4, 14) {
                node<ParadoxMarkerNode>("{", 4, 5)
                node<ParadoxBlankNode>(" ", 5, 6)
                node<ParadoxNamePartNode>("<x>", 6, 9) {
                    node<ParadoxMarkerNode>("<", 6, 7)
                    node<ParadoxNameFormatDefinitionNode>("x", 7, 8)
                    node<ParadoxMarkerNode>(">", 8, 9)
                }
                node<ParadoxBlankNode>(" ", 9, 10)
                node<ParadoxNameFormatLocalisationNode>("y", 10, 11)
                node<ParadoxBlankNode>("  ", 11, 13)
                node<ParadoxMarkerNode>("}", 13, 14)
            }
            node<ParadoxBlankNode>(" ", 14, 15)
            node<ParadoxErrorTokenNode>("<bar>", 15, 20)
            node<ParadoxBlankNode>(" ", 20, 21)
        }
        exp.check(dsl)
    }

    @Test
    fun test_strict_no_open_brace_whole_error() {
        val s = "<x> y} "
        val exp = resolve(s, formatName = "empire")!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxNameFormatExpression>(s, 0, s.length) {
            node<ParadoxErrorTokenNode>("<x> y}", 0, 6)
            node<ParadoxBlankNode>(" ", 6, 7)
        }
        exp.check(dsl)
    }

    @Test
    fun test_strict_stray_close_in_closure() {
        val s = "{x> y}"
        val exp = resolve(s, formatName = "empire")!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxNameFormatExpression>(s, 0, s.length) {
            node<ParadoxNameFormatClosureNode>(s, 0, 6) {
                node<ParadoxMarkerNode>("{", 0, 1)
                node<ParadoxNameFormatLocalisationNode>("x", 1, 2)
                node<ParadoxErrorTokenNode>(">", 2, 3)
                node<ParadoxBlankNode>(" ", 3, 4)
                node<ParadoxNameFormatLocalisationNode>("y", 4, 5)
                node<ParadoxMarkerNode>("}", 5, 6)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_strict_extra_close_top_level() {
        val s = "{<x>> y}}"
        val exp = resolve(s, formatName = "empire")!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxNameFormatExpression>(s, 0, s.length) {
            node<ParadoxNameFormatClosureNode>("{<x>> y}", 0, 8) {
                node<ParadoxMarkerNode>("{", 0, 1)
                node<ParadoxNamePartNode>("<x>", 1, 4) {
                    node<ParadoxMarkerNode>("<", 1, 2)
                    node<ParadoxNameFormatDefinitionNode>("x", 2, 3)
                    node<ParadoxMarkerNode>(">", 3, 4)
                }
                node<ParadoxErrorTokenNode>(">", 4, 5)
                node<ParadoxBlankNode>(" ", 5, 6)
                node<ParadoxNameFormatLocalisationNode>("y", 6, 7)
                node<ParadoxMarkerNode>("}", 7, 8)
            }
            node<ParadoxErrorTokenNode>("}", 8, 9)
        }
        exp.check(dsl)
    }

    @Test
    fun test_strict_empty_command_in_closure() {
        val s = "{[]}"
        val exp = resolve(s, formatName = "empire")!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxNameFormatExpression>(s, 0, s.length) {
            node<ParadoxNameFormatClosureNode>(s, 0, 4) {
                node<ParadoxMarkerNode>("{", 0, 1)
                node<ParadoxCommandNode>("[]", 1, 3) {
                    node<ParadoxMarkerNode>("[", 1, 2)
                    node<ParadoxErrorTokenNode>("", 2, 2)
                    node<ParadoxMarkerNode>("]", 2, 3)
                }
                node<ParadoxMarkerNode>("}", 3, 4)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_strict_empty_definition_in_closure() {
        val s = "{<>}"
        val exp = resolve(s, formatName = "empire")!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxNameFormatExpression>(s, 0, s.length) {
            node<ParadoxNameFormatClosureNode>(s, 0, 4) {
                node<ParadoxMarkerNode>("{", 0, 1)
                node<ParadoxNamePartNode>("<>", 1, 3) {
                    node<ParadoxMarkerNode>("<", 1, 2)
                    node<ParadoxErrorTokenNode>("", 2, 2)
                    node<ParadoxMarkerNode>(">", 2, 3)
                }
                node<ParadoxMarkerNode>("}", 3, 4)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_strict_unmatched_angle_stop_at_space() {
        val s = "{<abc y}"
        val exp = resolve(s, formatName = "empire")!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxNameFormatExpression>(s, 0, s.length) {
            node<ParadoxNameFormatClosureNode>(s, 0, 8) {
                node<ParadoxMarkerNode>("{", 0, 1)
                node<ParadoxNamePartNode>("<abc", 1, 5) {
                    node<ParadoxMarkerNode>("<", 1, 2)
                    node<ParadoxNameFormatDefinitionNode>("abc", 2, 5)
                    node<ParadoxErrorTokenNode>("", 5, 5)
                }
                node<ParadoxBlankNode>(" ", 5, 6)
                node<ParadoxNameFormatLocalisationNode>("y", 6, 7)
                node<ParadoxMarkerNode>("}", 7, 8)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_strict_unmatched_bracket_stop_at_space() {
        val s = "{[Root. y}"
        val exp = resolve(s, formatName = "empire")!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxNameFormatExpression>(s, 0, s.length) {
            node<ParadoxNameFormatClosureNode>("{[Root. y}", 0, 10) {
                node<ParadoxMarkerNode>("{", 0, 1)
                node<ParadoxCommandNode>("[Root.", 1, 7) {
                    node<ParadoxMarkerNode>("[", 1, 2)
                    node<ParadoxCommandExpression>("Root.", 2, 7) {
                        node<ParadoxSystemCommandScopeNode>("Root", 2, 6)
                        node<ParadoxOperatorNode>(".", 6, 7)
                        node<ParadoxDynamicCommandFieldNode>("", 7, 7) {
                            node<ParadoxCommandFieldValueNode>("", 7, 7) {
                                node<ParadoxDataSourceNode>("", 7, 7)
                            }
                        }
                    }
                    node<ParadoxErrorTokenNode>("", 7, 7)
                }
                node<ParadoxBlankNode>(" ", 7, 8)
                node<ParadoxNameFormatLocalisationNode>("y", 8, 9)
                node<ParadoxMarkerNode>("}", 9, 10)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_strict_stray_close_bracket_in_closure() {
        val s = "{x] y}"
        val exp = resolve(s, formatName = "empire")!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxNameFormatExpression>(s, 0, s.length) {
            node<ParadoxNameFormatClosureNode>(s, 0, 6) {
                node<ParadoxMarkerNode>("{", 0, 1)
                node<ParadoxNameFormatLocalisationNode>("x", 1, 2)
                node<ParadoxErrorTokenNode>("]", 2, 3)
                node<ParadoxBlankNode>(" ", 3, 4)
                node<ParadoxNameFormatLocalisationNode>("y", 4, 5)
                node<ParadoxMarkerNode>("}", 5, 6)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_strict_top_level_after_closure_text() {
        val s = "{x}y"
        val exp = resolve(s, formatName = "empire")!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxNameFormatExpression>(s, 0, s.length) {
            node<ParadoxNameFormatClosureNode>("{x}", 0, 3) {
                node<ParadoxMarkerNode>("{", 0, 1)
                node<ParadoxNameFormatLocalisationNode>("x", 1, 2)
                node<ParadoxMarkerNode>("}", 2, 3)
            }
            node<ParadoxErrorTokenNode>("y", 3, 4)
        }
        exp.check(dsl)
    }

    @Test
    fun test_strict_braces_blank_inside() {
        val s = "{ }"
        val exp = resolve(s, formatName = "empire")!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxNameFormatExpression>(s, 0, s.length) {
            node<ParadoxNameFormatClosureNode>(s, 0, 3) {
                node<ParadoxMarkerNode>("{", 0, 1)
                node<ParadoxBlankNode>(" ", 1, 2)
                node<ParadoxMarkerNode>("}", 2, 3)
            }
        }
        exp.check(dsl)
    }

    @Test
    fun test_strict_command_adjacent_no_space() {
        val s = "{x[Root.GetName]}"
        val exp = resolve(s, formatName = "empire")!!
        exp.renderAndPrintln()
        val dsl = buildComplexExpression<ParadoxNameFormatExpression>(s, 0, s.length) {
            node<ParadoxNameFormatClosureNode>(s, 0, 17) {
                node<ParadoxMarkerNode>("{", 0, 1)
                node<ParadoxNameFormatLocalisationNode>("x", 1, 2)
                node<ParadoxCommandNode>("[Root.GetName]", 2, 16) {
                    node<ParadoxMarkerNode>("[", 2, 3)
                    node<ParadoxCommandExpression>("Root.GetName", 3, 15) {
                        node<ParadoxSystemCommandScopeNode>("Root", 3, 7)
                        node<ParadoxOperatorNode>(".", 7, 8)
                        node<ParadoxStaticCommandFieldNode>("GetName", 8, 15)
                    }
                    node<ParadoxMarkerNode>("]", 15, 16)
                }
                node<ParadoxMarkerNode>("}", 16, 17)
            }
        }
        exp.check(dsl)
    }
}

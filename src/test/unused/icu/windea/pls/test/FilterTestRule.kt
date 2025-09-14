package icu.windea.pls.test

import org.junit.Assume
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

open class FilterTestRule(
    private val message: String,
    private val predicate: () -> Boolean
) : TestRule {
    override fun apply(base: Statement, description: Description?): Statement {
        return object : Statement() {
            override fun evaluate() {
                Assume.assumeTrue(message, predicate())
                base.evaluate()
            }
        }
    }
}

package icu.windea.pls.test

import icu.windea.pls.ChronicleCapabilities
import org.junit.Assume

object ChronicleAssume {
    fun includeBenchmark() {
        val v = ChronicleCapabilities.Test.includeAll || ChronicleCapabilities.Test.includeBenchmark
        Assume.assumeTrue("Benchmarks are not included", v)
    }

    fun includeAi() {
        val v = ChronicleCapabilities.Test.includeAll || ChronicleCapabilities.Test.includeAi
        Assume.assumeTrue("AI tests are not included", v)
    }

    fun includeRemote() {
        val v = ChronicleCapabilities.Test.includeAll || ChronicleCapabilities.Test.includeRemote
        Assume.assumeTrue("Remote network accessed tests are not included", v)
    }

    fun includeLocalEnv() {
        val v = ChronicleCapabilities.Test.includeAll || ChronicleCapabilities.Test.includeLocalEnv
        Assume.assumeTrue("Local environment only tests are not included", v)
    }

    fun includeConfigGenerator() {
        val v = ChronicleCapabilities.Test.includeAll || ChronicleCapabilities.Test.includeConfigGenerator
        Assume.assumeTrue("Config generator tests are not included", v)
    }
}

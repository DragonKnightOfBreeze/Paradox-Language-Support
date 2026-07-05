package icu.windea.pls.test

import org.junit.Assume

object ChronicleAssume {
    fun includeBenchmark() {
        val v = ChronicleTestCapacities.includeAll() || ChronicleTestCapacities.includeBenchmark()
        Assume.assumeTrue("Benchmarks are not included", v)
    }

    fun includeAi() {
        val v = ChronicleTestCapacities.includeAll() || ChronicleTestCapacities.includeAi()
        Assume.assumeTrue("AI tests are not included", v)
    }

    fun includeLocalEnv() {
        val v = ChronicleTestCapacities.includeAll() || ChronicleTestCapacities.includeLocalDev()
        Assume.assumeTrue("Local environment only tests are not included", v)
    }

    fun includeConfigGenerator() {
        val v = ChronicleTestCapacities.includeAll() || ChronicleTestCapacities.includeConfigGenerator()
        Assume.assumeTrue("Config generator tests are not included", v)
    }
}


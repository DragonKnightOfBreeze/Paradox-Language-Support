package icu.windea.pls.test

/**
 * 用于运行 JMH 基准测试的描述符对象。
 */
data class ChronicleBenchmarkDescriptor(
    val className: String,
    val methods: List<String>,
)

package icu.windea.pls.util.net

import com.intellij.util.net.ProxyConfiguration
import com.intellij.util.net.ProxySettings
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.ProxySelector
import java.net.URI

/**
 * 代理解析服务。
 *
 * 解析优先级：IDE 静态代理、环境变量、默认 [ProxySelector]、JVM 系统属性。
 */
object ProxyService {
    fun resolve(
        uri: URI,
        environment: Map<String, String> = System.getenv(),
        systemProperties: Map<String, String> = systemProperties(),
        includePlatformSettings: Boolean = true,
    ): Proxy? {
        if (includePlatformSettings) {
            val platformProxy = runCatching { ProxySettings.getInstance().getProxyConfiguration() }.getOrNull()
            if (platformProxy is ProxyConfiguration.StaticProxyConfiguration) {
                return createProxy(platformProxy.host, platformProxy.port, platformProxy.protocol.name)
            }
        }

        resolveFromEnvironment(uri, environment)?.let { return it }
        runCatching {
            ProxySelector.getDefault()?.select(uri)?.firstOrNull { it.type() != Proxy.Type.DIRECT }
        }.getOrNull()?.let { return it }
        resolveFromSystemProperties(uri, systemProperties)?.let { return it }
        return null
    }

    fun parse(value: String): Proxy? {
        val normalized = if (value.contains("://")) value else "http://$value"
        return runCatching {
            val uri = URI(normalized)
            val host = uri.host ?: return null
            val port = if (uri.port > 0) uri.port else defaultPort(uri.scheme)
            val type = if (uri.scheme?.startsWith("socks", ignoreCase = true) == true) Proxy.Type.SOCKS else Proxy.Type.HTTP
            Proxy(type, InetSocketAddress(host, port))
        }.getOrNull()
    }

    fun describe(proxy: Proxy?): String {
        if (proxy == null) return "direct"
        val address = proxy.address() as? InetSocketAddress ?: return proxy.toString()
        return "${proxy.type().name.lowercase()}://${address.hostString}:${address.port}"
    }

    fun isExcluded(host: String?, noProxy: String?): Boolean {
        if (host.isNullOrEmpty() || noProxy.isNullOrBlank()) return false
        return noProxy.split(',').any { pattern ->
            val p = pattern.trim()
            if (p.isEmpty()) return@any false
            when {
                p == "*" -> true
                p.contains('*') -> host.matches(globToRegex(p))
                p.startsWith(".") -> host.endsWith(p, ignoreCase = true)
                else -> host.equals(p, ignoreCase = true) || host.endsWith(".$p", ignoreCase = true)
            }
        }
    }

    private fun resolveFromEnvironment(uri: URI, environment: Map<String, String>): Proxy? {
        val scheme = uri.scheme.lowercase()
        val value = when (scheme) {
            "https" -> getEnvProxy(environment, "HTTPS_PROXY") ?: getEnvProxy(environment, "HTTP_PROXY") ?: getEnvProxy(environment, "ALL_PROXY")
            else -> getEnvProxy(environment, "HTTP_PROXY") ?: getEnvProxy(environment, "ALL_PROXY")
        } ?: return null
        if (isExcluded(uri.host, getEnvProxy(environment, "NO_PROXY"))) return null
        return parse(value)
    }

    private fun resolveFromSystemProperties(uri: URI, systemProperties: Map<String, String>): Proxy? {
        val scheme = uri.scheme.lowercase()
        val host = systemProperties["$scheme.proxyHost"] ?: systemProperties["http.proxyHost"] ?: return null
        val port = (systemProperties["$scheme.proxyPort"] ?: systemProperties["http.proxyPort"])?.toIntOrNull() ?: 80
        return createProxy(host, port, "HTTP")
    }

    private fun getEnvProxy(environment: Map<String, String>, name: String): String? {
        return environment[name] ?: environment[name.lowercase()]
    }

    private fun createProxy(host: String, port: Int, protocol: String): Proxy {
        val type = if (protocol.equals("SOCKS", ignoreCase = true)) Proxy.Type.SOCKS else Proxy.Type.HTTP
        return Proxy(type, InetSocketAddress(host, port))
    }

    private fun defaultPort(scheme: String?): Int {
        return if (scheme?.startsWith("socks", ignoreCase = true) == true) 1080 else 80
    }

    private fun globToRegex(glob: String): Regex {
        val regex = glob
            .replace(".", "\\.")
            .replace("*", ".*")
            .replace("?", ".")
        return Regex("^$regex$", RegexOption.IGNORE_CASE)
    }

    private fun systemProperties(): Map<String, String> {
        return System.getProperties().stringPropertyNames().associateWith { System.getProperty(it).orEmpty() }
    }
}

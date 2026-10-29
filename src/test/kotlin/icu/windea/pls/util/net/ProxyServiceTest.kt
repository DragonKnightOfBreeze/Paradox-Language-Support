package icu.windea.pls.util.net

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URI

/**
 * @see ProxyService
 */
@RunWith(JUnit4::class)
class ProxyServiceTest {
    @Test
    fun parse_test() {
        val http = ProxyService.parse("127.0.0.1:7890")
        Assert.assertNotNull(http)
        Assert.assertEquals(Proxy.Type.HTTP, http!!.type())
        Assert.assertEquals(InetSocketAddress("127.0.0.1", 7890), http.address())

        val socks = ProxyService.parse("socks5://127.0.0.1:1080")
        Assert.assertNotNull(socks)
        Assert.assertEquals(Proxy.Type.SOCKS, socks!!.type())
        Assert.assertEquals(InetSocketAddress("127.0.0.1", 1080), socks.address())
    }

    @Test
    fun describe_test() {
        Assert.assertEquals("direct", ProxyService.describe(null))
        Assert.assertEquals("http://127.0.0.1:7890", ProxyService.describe(ProxyService.parse("http://127.0.0.1:7890")))
    }

    @Test
    fun isExcluded_test() {
        Assert.assertTrue(ProxyService.isExcluded("localhost", "localhost"))
        Assert.assertTrue(ProxyService.isExcluded("foo.example.com", "*.example.com"))
        Assert.assertTrue(ProxyService.isExcluded("foo.example.com", ".example.com"))
        Assert.assertFalse(ProxyService.isExcluded("example.org", "example.com,*.example.com"))
    }

    @Test
    fun resolveFromEnvironment_test() {
        val proxy = ProxyService.resolve(
            URI("https://example.com"),
            environment = mapOf("HTTPS_PROXY" to "http://127.0.0.1:7890"),
            includePlatformSettings = false,
        )
        Assert.assertNotNull(proxy)
        Assert.assertEquals(Proxy.Type.HTTP, proxy!!.type())
        Assert.assertEquals(InetSocketAddress("127.0.0.1", 7890), proxy.address())
    }

    @Test
    fun resolveWithNoProxy_test() {
        val proxy = ProxyService.resolve(
            URI("https://localhost"),
            environment = mapOf(
                "HTTPS_PROXY" to "http://127.0.0.1:7890",
                "NO_PROXY" to "localhost",
            ),
            includePlatformSettings = false,
        )
        Assert.assertNull(proxy)
    }
}

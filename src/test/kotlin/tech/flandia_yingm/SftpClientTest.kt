package tech.flandia_yingm

import org.junit.jupiter.api.Assertions
import java.io.File
import java.net.InetSocketAddress

internal class SftpClientTest {

    private val testPath = "./TEST"

    private val testContent = "TEST"

    @org.junit.jupiter.api.Test
    fun download() {
        try {
            val sftpClient = SftpClient(
                InetSocketAddress("149.28.146.81", 22),
                "gregortech",
                "gregortech"
            )
            sftpClient.download(testPath, "./")
            Assertions.assertEquals(testContent, File("TEST").readText())
        } finally {
            File("TEST").delete()
        }
    }

    @org.junit.jupiter.api.Test
    fun list() {
        val sftpClient = SftpClient(
            InetSocketAddress("149.28.146.81", 22),
            "gregortech",
            "gregortech"
        )
        val files = sftpClient.list(testPath)
        val testFile = files.singleOrNull { str -> str.endsWith("TEST") }
        Assertions.assertNotNull(testFile)
    }

}
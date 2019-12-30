package tech.flandia_yingm

import org.junit.jupiter.api.*
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.TestInstance.Lifecycle
import java.io.File
import java.net.InetSocketAddress

@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation::class)
internal class SftpClientTest {

    private val localTestPath = File("TEST")

    private val localTestDir = File(".")

    private val remoteTestPath = "TEST"

    private val remoteTestDir = "."

    private val testContent = "TEST"

    private var sftpClient: SftpClient? = null

    @BeforeAll
    fun createClient() {
        sftpClient = SftpClient(
            InetSocketAddress("149.28.146.81", 22),
            "gregortech",
            "gregortech"
        )
    }

    @BeforeEach
    fun createTestFile() {
        localTestPath.createNewFile()
        localTestPath.writeText(testContent)
    }

    @AfterAll
    fun closeClient() {
        sftpClient!!.close()
    }

    @AfterEach
    fun deleteTestFile() {
        localTestPath.delete()
    }

    @Test
    @Order(0)
    fun testDownloadAndUpload() {
        sftpClient!!.upload(localTestPath.toString(), remoteTestDir)
        localTestPath.delete()

        sftpClient!!.download(remoteTestPath, localTestDir.toString())
        Assertions.assertEquals(testContent, localTestPath.readText())
    }

    @org.junit.jupiter.api.Test
    @Order(1)
    fun testExistsAndRemove() {
        sftpClient!!.upload(localTestPath.toString(), remoteTestDir)
        Assertions.assertTrue(sftpClient!!.exists(remoteTestPath))

        sftpClient!!.remove(remoteTestPath)
        Assertions.assertFalse(sftpClient!!.exists(remoteTestPath))
    }

    @org.junit.jupiter.api.Test
    @Order(1)
    fun testList() {
        sftpClient!!.upload(localTestPath.toString(), remoteTestDir)
        Assertions.assertTrue(sftpClient!!.list(remoteTestPath).contains(SftpUtils.getFilename(remoteTestPath)))
    }

}
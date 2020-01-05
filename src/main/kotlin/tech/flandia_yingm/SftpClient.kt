package tech.flandia_yingm

import com.jcraft.jsch.*
import mu.KotlinLogging
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.TimeUnit.SECONDS


class SftpClient
@Throws(JSchException::class)
constructor(
    private val address: InetSocketAddress,
    username: String,
    password: String
) : Closeable {

    private val log = KotlinLogging.logger {}

    private val session: Session

    private val channel: ChannelSftp

    init {
        log.info("Opening a new SFTP client with the address $address")

        val jsch = JSch()
        val config = Properties()
        config["StrictHostKeyChecking"] = "no"
        config["PreferredAuthentications"] = "password"

        session = jsch.getSession(username, address.hostString, address.port)
        session.setConfig(config)
        session.setPassword(password)
        session.serverAliveInterval = SECONDS.toMillis(3).toInt()
        session.timeout = SECONDS.toMillis(15).toInt()
        session.connect(SECONDS.toMillis(15).toInt())
        channel = session.openChannel("sftp") as ChannelSftp
        channel.connect(SECONDS.toMillis(15).toInt())

        log.info("Opened a new SFTP client with the address $address")
    }

    @Synchronized
    @Throws(SftpException::class, IOException::class)
    fun upload(localFile: String, remoteDir: String) {
        log.info { "Upload the local file '$localFile' to the remote dir '$remoteDir'" }

        mkdirs(remoteDir)

        val remoteFile = "$remoteDir/${SftpUtils.getFilename(localFile)}"
        channel.put(localFile, remoteFile)

        val localFileSize = Files.size(Paths.get(localFile))
        val remoteFileSize = channel.stat(remoteFile).size
        if (localFileSize != remoteFileSize) {
            throw IOException("The local file size $localFileSize != the remote file size $remoteFileSize")
        }
    }

    @Synchronized
    @Throws(SftpException::class, IOException::class)
    fun download(remoteFile: String, localDir: String) {
        log.info { "Download the remote file '$remoteFile' to the local dir '$localDir'" }

        File(localDir).mkdirs()

        val localFile = "$localDir/${SftpUtils.getFilename(remoteFile)}"
        channel.get(remoteFile, localFile)

        val remoteFileSize = channel.stat(remoteFile).size
        val localFileSize = Files.size(Paths.get(localFile))
        if (remoteFileSize != localFileSize) {
            throw IOException("The remote file size $remoteFileSize != the local file size $localFileSize")
        }
    }

    @Synchronized
    @Throws(SftpException::class)
    fun remove(remoteFile: String) {
        log.info { "Remove the remote file '$remoteFile'" }
        if (exists(remoteFile)) {
            channel.rm(remoteFile)
        }
    }

    @Synchronized
    @Throws(SftpException::class)
    fun list(remoteDir: String): List<String> {
        log.info { "List all files in the remote dir '$remoteDir'" }
        return lsRec(remoteDir)
    }

    @Synchronized
    @Throws(SftpException::class)
    fun exists(remotePath: String): Boolean {
        return try {
            channel.stat(remotePath)
            true
        } catch (e: SftpException) {
            false
        }
    }

    @Synchronized
    @Throws(SftpException::class)
    private fun mkdirs(remoteDir: String) {
        val root = SftpUtils.getRoot(remoteDir)
        val rest = remoteDir.substring(root.length)

        if (!exists(remoteDir)) {
            channel.mkdir(root)
        }
        if (rest != "/" && rest != "") {
            mkdirs(rest)
        }
    }

    private fun lsRec(remoteDir: String): List<String> {
        return channel.ls(remoteDir)
            .map { it as ChannelSftp.LsEntry }
            .flatMap {
                val file = "$remoteDir/${it.filename}"
                log.info { "Searching $file" }
                try {
                    val fileAttr = it.attrs
                    if (!file.endsWith(".")) {
                        when {
                            fileAttr.isDir -> lsRec(file)
                            fileAttr.isReg -> listOf(file)
                            else -> listOf<String>()
                        }
                    } else {
                        listOf<String>()
                    }
                } catch (e: SftpException) {
                    log.warn { "An SFTP error occurs while searching $file: $e" }
                    listOf<String>()
                }
            }
    }

    @Synchronized
    override fun close() {
        log.info { "Closing the SFTP client $this" }

        channel.disconnect()
        session.disconnect()
    }

    @Synchronized
    override fun toString(): String {
        return "SftpClient(address=$address)"
    }

}
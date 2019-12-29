package tech.flandia_yingm

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.TimeUnit.SECONDS

class SftpClient(
    address: InetSocketAddress,
    username: String,
    password: String
) {

    private val channel: ChannelSftp;

    init {
        val jsch = JSch()
        val session = jsch.getSession(username, address.hostString, address.port)
        val config = Properties()
        config["StrictHostKeyChecking"] = "no"
        config["PreferredAuthentications"] = "password"
        session.setConfig(config)
        session.setPassword(password)
        session.connect(SECONDS.toMillis(60).toInt())
        channel = session.openChannel("sftp") as ChannelSftp
        channel.connect(SECONDS.toMillis(60).toInt())
    }

    fun download(remoteFile: String, localDir: String) {
        val localFile = getFilename(remoteFile)
        channel.get(remoteFile, "$localDir/$localFile")

        val remoteFileSize = channel.lstat(remoteFile).size
        val localFileSize = Files.size(Paths.get(localFile))
        if (remoteFileSize != localFileSize) {
            throw IOException("The remote file size $remoteFileSize != the local file size $localFileSize")
        }
    }

    fun remove(remoteFile: String) {
        channel.rm(remoteFile)
    }

    fun list(remoteDir: String): List<String> {
        return channel.ls(remoteDir).map {
            val lsEntry = it as ChannelSftp.LsEntry
            lsEntry.longname
        }
    }

}
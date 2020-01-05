package tech.flandia_yingm

import tech.flandia_yingm.SftpUtils.getParent
import tech.flandia_yingm.SftpUtils.getRelative
import java.io.File
import java.net.InetSocketAddress
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

class SftpFileMover(
    private val localDir: File,
    private val remoteDir: String,
    private val address: InetSocketAddress,
    private val username: String,
    private val password: String,
    private val searchInterval: Long,
    private val reconnectInterval: Long,
    private val workerThreadNumber: Int
) {

    private var searcherThread: Thread? = null
    private val workerThreadList: MutableList<Thread> = mutableListOf()
    private val fileQueue: BlockingQueue<String> = LinkedBlockingQueue()

    fun start() {
        searcherThread = thread { runSearch() }
        repeat(workerThreadNumber) {
            workerThreadList.add(thread { runWork() })
        }
    }

    fun stop() {
        searcherThread?.interrupt()
        workerThreadList.forEach {
            it.interrupt()
        }
    }

    private fun runSearch() {
        while (!Thread.currentThread().isInterrupted) {
            try {
                openSftpClient().use {
                    while (!Thread.currentThread().isInterrupted) {
                        it.list(remoteDir)
                            .sorted()
                            .forEach { file -> fileQueue.add(file) }
                        do {
                            Thread.sleep(searchInterval)
                        } while (fileQueue.size > 0)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Thread.sleep(reconnectInterval)
        }
    }

    private fun runWork() {
        while (!Thread.currentThread().isInterrupted) {
            try {
                openSftpClient().use {
                    while (!Thread.currentThread().isInterrupted) {
                        val file = fileQueue.take()
                        if (it.exists(file)) {
                            it.download(file, localDir.resolve(getParent(getRelative(remoteDir, file))).path)
                            it.remove(file)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Thread.sleep(reconnectInterval)
        }
    }

    private fun openSftpClient(): SftpClient {
        return SftpClient(address, username, password)
    }

}
package beppo2k.ftp.server.datatransfer

import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SelectionKey
import java.nio.channels.SelectableChannel
import beppo2k.ftp.server.DataTransferSession
import beppo2k.ftp.util.Log

import scala.collection.JavaConversions._
import java.nio.channels.SocketChannel
import java.net.InetSocketAddress
import java.util.concurrent.locks.ReentrantReadWriteLock
import akka.actor.Actor
import java.net.ServerSocket

class FtpDataTransfer extends Thread {

    val selector: Selector = Selector.open()

    var isRunning = false

    val lock = new ReentrantReadWriteLock(true)
    val readLock = lock.readLock()
    val writeLock = lock.writeLock()

    def add(channel: SelectableChannel, selectionKey: Int): Unit = {
        try {
            writeLock.lock()
            channel.register(this.selector, selectionKey)
        } finally {
            writeLock.unlock()
        }

    }

    override def run(): Unit = {
        isRunning = true
        while (isRunning) {
            try {
                readLock.lock()
                selector.select(10L)
            } finally {
                readLock.unlock()
            }
            val selectedKeys = selector.selectedKeys()
            while (selectedKeys.size() > 0) {
                val it = selector.selectedKeys().iterator()
                while (it.hasNext()) {
                    val key = it.next()
                    it.remove()
                    key match {
                        case k if k.isAcceptable() => {
                            val server = k.channel().asInstanceOf[ServerSocketChannel]
                            val socket = server.accept()
                            socket.configureBlocking(false)
                            val sourceAddr = server.socket().getLocalSocketAddress().toString()
                            val connectAddr = socket.getRemoteAddress().toString()

                            Log.info("host[%s] remote[%s]data connection accept", sourceAddr, connectAddr)
                            DataTransferSession.moveSourceSessionToConnectedSession(sourceAddr, connectAddr) match {
                                case Some(session) => {
                                    session.connectedChannel = socket
                                }
                            }
                            socket.register(
                                selector,
                                SelectionKey.OP_READ | SelectionKey.OP_WRITE, connectAddr)

                        }
                        case k if k.isConnectable() => {
                            val socket = k.channel().asInstanceOf[SocketChannel]
                            if (socket.isConnectionPending()) {
                                socket.finishConnect()
                                val sourceAddr = socket.getLocalAddress().toString()
                                val connectAddr = socket.getRemoteAddress().toString()

                                Log.info("host[%s] remote[%s]data connection accept", connectAddr, sourceAddr)
                                DataTransferSession.moveSourceSessionToConnectedSession(connectAddr, sourceAddr) match {
                                    case Some(session) => {
                                        session.connectedChannel = socket
                                    }
                                }
                                socket.register(
                                    selector,
                                    SelectionKey.OP_READ | SelectionKey.OP_WRITE, sourceAddr)

                            }
                        }
                        case k if k.isReadable() || k.isWritable() => {
                            Log.info("channel is readable[%s] writable[%s]", k.isReadable().toString(), k.isWritable().toString())
                            //val addr = k.channel().asInstanceOf[SocketChannel].getLocalAddress().toString()
                            val addr = k.attachment().asInstanceOf[String]
                            DataTransferSession.getConnectSession(addr) match {
                                case Some(session) => {
                                    Log.info("session find [%s]", addr)
                                    if (session.canTransfer()) {
                                        session.executeTransfer(selector, key)
                                        if (!session.isContinue()) {
                                            session.finishTransfer()
                                        } else {
                                            k.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE)
                                        }
                                    } else {
                                        // not ready
                                        Log.warn("command after pasv isn't receive")
                                    }
                                }
                                case None => {
                                    // unexpected condition
                                    Log.error("unexpected condition")
                                }
                            }
                        }
                        case _ => {
                            key.channel().close()
                        }
                    }
                }
            }
        }
    }

    def close() = {
        try {
            for (key <- selector.keys()) {
                if (key.channel().isOpen()) {
                    key.channel().close()
                }
            }
            if (selector.isOpen()) {
                selector.close()
            }
        } catch {
            case e: Exception => {
                Log.error("[%s]", e.getMessage())
            }
        }
    }

    def shutdown() = {
        this.isRunning = false
    }
}

case class Start()
case class Add(channel: SelectableChannel, opKey: Int)
case class Stop()

class FtpDataTransferActor extends Actor {
    val dataTransferServer = new FtpDataTransfer()

    def receive: Receive = {
        case Start() => {
            dataTransferServer.start()
        }
        case Add(channel, opKey) => {
            dataTransferServer.add(channel, opKey)
        }
        case Stop() => {
            dataTransferServer.shutdown()
        }
    }
}

object FtpDataTransfer {

    def createServerSocket(): ServerSocketChannel = {

        new ServerSocket()
        val server = ServerSocketChannel.open()
        server.socket().setReuseAddress(true)
        server.configureBlocking(false)
        server.bind(null)
        return server
    }

    def createClientSocket(ip: String, port: Int): SocketChannel = {
        val client = SocketChannel.open()
        client.configureBlocking(false)
        client.connect(new InetSocketAddress(ip, port))
        return client
    }

}
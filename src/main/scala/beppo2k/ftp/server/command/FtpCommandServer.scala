package beppo2k.ftp.server.command

import java.nio.channels.ServerSocketChannel
import java.net.InetSocketAddress
import java.nio.channels.Selector
import java.nio.channels.SelectionKey
import beppo2k.ftp.server.command.handler.{ResponseHandler, AcceptHandler, RequestHandler}
import beppo2k.ftp.server.handler.Handler
import beppo2k.ftp.util.Log
import collection.JavaConversions._

class FtpCommandServer(port: Int) {

    val server: ServerSocketChannel = ServerSocketChannel.open()
    server.configureBlocking(false)
    server.socket().setReuseAddress(true)
    server.bind(new InetSocketAddress(this.port))
    val selector: Selector = Selector.open()
    server.register(selector, SelectionKey.OP_ACCEPT, new AcceptHandler())

    var isRunning: Boolean = _

    def start() = {
        isRunning = true
        try {
            Log.info("start")
            while (isRunning) {
                if (0 < selector.select(10L)) {
                    val selectedKeys = selector.selectedKeys()
                    if (selectedKeys.size() > 0) {
                        Log.info("select key size [%d]", selectedKeys.size())
                        val it = selector.selectedKeys.iterator()
                        while (it.hasNext()) {
                            val key = it.next()
                            it.remove()
                            val handler: Handler = key.attachment().asInstanceOf[Handler]

                            if (key.isAcceptable() && handler.isInstanceOf[AcceptHandler]) {
                                Log.debug("accept")
                                handler.handle(selector, key)
                            } else if (key.isReadable() && handler.isInstanceOf[RequestHandler]) {
                                Log.debug("readable")
                                handler.handle(selector, key)
                            } else if (key.isWritable() && handler.isInstanceOf[ResponseHandler]) {
                                Log.debug("writable")
                                handler.handle(selector, key)
                            } else {
                                if(!key.channel().isOpen()){
                                    key.channel().close()
                                }
                                Log.debug("none")
                            }
                        }
                    } else {
                        Log.debug("selected key zero")
                    }
                } else {
                    Log.debug("select count zero")
                }
            }
            close()
        } catch {
            case e: Exception => {
                Log.error("%s", e.getMessage())
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
            if (server.isOpen()) {
                server.close()
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

    def stop() = {
        isRunning = false
    }
}
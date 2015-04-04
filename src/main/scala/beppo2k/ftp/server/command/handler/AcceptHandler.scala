package beppo2k.ftp.server.command.handler

import java.nio.channels.Selector
import java.nio.channels.SelectionKey
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import beppo2k.ftp.server.command.FtpConnectionEstablish
import beppo2k.ftp.server.handler.Handler

class AcceptHandler extends Handler {

    override def handle(selector:Selector , key:SelectionKey) = {
        val server = key.channel().asInstanceOf[ServerSocketChannel]
        server.configureBlocking(false)
        val socket = server.accept().asInstanceOf[SocketChannel]
        socket.socket().setKeepAlive(true)
        socket.socket().setSoTimeout(50000)
        socket.configureBlocking(false)
        socket.register(selector,
                SelectionKey.OP_WRITE,
                new ResponseHandler(new FtpConnectionEstablish()))
    }
}
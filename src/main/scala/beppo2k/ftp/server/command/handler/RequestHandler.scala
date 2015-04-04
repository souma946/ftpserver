package beppo2k.ftp.server.command.handler

import java.nio.channels.Selector
import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel
import beppo2k.ftp.server.command.{FtpConnectionEstablish, FtpCommandParser}
import beppo2k.ftp.server.handler.Handler
import beppo2k.ftp.util.Log

class RequestHandler(isAccept:Boolean) extends Handler {

    override def handle(selector:Selector , key:SelectionKey) = {

        val channel:SocketChannel  = key.channel().asInstanceOf[SocketChannel]

        val command = this.isAccept match {
            case true => new FtpConnectionEstablish(null);
            case _ => FtpCommandParser.parseCommand(channel)
        }

        if(command != null){
            key.interestOps(SelectionKey.OP_WRITE);
            key.attach(new ResponseHandler(command));
        }else{
            channel.close()
            Log.warn("command not found")
        }
    }
}
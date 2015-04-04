package beppo2k.ftp.server.command.handler

import java.nio.channels.Selector
import java.nio.channels.SelectionKey
import beppo2k.ftp.server.command.FtpCommand
import beppo2k.ftp.server.handler.Handler

class ResponseHandler(command:FtpCommand) extends Handler {

    override def handle(selector:Selector , key:SelectionKey) = {
        this.command.execute(selector , key)

        if(key.channel().isOpen()){
            key.interestOps(SelectionKey.OP_READ)
            key.attach(new RequestHandler(false))
        }
    }
}
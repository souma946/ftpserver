package beppo2k.ftp.server.datatransfer.handler

import beppo2k.ftp.server.command.FtpDataConnectionFinishCommand
import beppo2k.ftp.server.command.handler.ResponseHandler
import beppo2k.ftp.server.handler.Handler
import beppo2k.ftp.util.Log
import java.nio.channels.SelectionKey
import java.nio.channels.Selector

abstract class PassiveCommandHandler extends Handler {

    var commandConnectionKey:SelectionKey = _

    var comanndSelector:Selector = _

    var isContinue:Boolean = _

    override def handle(selector:Selector , key:SelectionKey) = {
        handle(key)
    }

    def handle(key:SelectionKey) :Unit

    def finishCommand(msg:String) = {
        Log.info("finish command start")
        isContinue = false
        val command = new FtpDataConnectionFinishCommand(
                this.comanndSelector ,
                this.commandConnectionKey ,
                msg);
        commandConnectionKey.interestOps(SelectionKey.OP_WRITE)
        commandConnectionKey.attach(new ResponseHandler(command))
        Log.info("finish command end")
    }
}
package beppo2k.ftp.server.datatransfer.handler

import java.nio.channels.SelectionKey
import java.io.File
import java.nio.channels.SocketChannel
import java.nio.ByteBuffer
import beppo2k.ftp.util.{FileUtil, Log}

class NlstHandler(filePath:String) extends PassiveCommandHandler {

    override def handle(key:SelectionKey) :Unit = {
        Log.info("NlstHandler start")
        val file = new File(this.filePath)
        val channel = key.channel().asInstanceOf[SocketChannel]

        if(!file.exists()){
            return
        }

        val list = FileUtil.list(file)
        val bb = ByteBuffer.wrap(list.getBytes())
        channel.write(bb)
        super.finishCommand("Directory send OK")
        Log.info("NlstHandler end")
    }
}
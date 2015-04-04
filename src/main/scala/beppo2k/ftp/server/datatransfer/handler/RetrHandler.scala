package beppo2k.ftp.server.datatransfer.handler

import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import beppo2k.ftp.util.Log

class RetrHandler(filePath:String) extends PassiveCommandHandler {

    var fileLength:Long = _

    private var fileInputStream:FileInputStream = _

    override def handle(key:SelectionKey) {
        try{
            val channel = key.channel().asInstanceOf[SocketChannel]
            Log.info("RetrHandler start [%s]" , channel.getRemoteAddress().toString())
            val file = new File(this.filePath)

            if(fileLength == 0 && fileInputStream == null){
                fileInputStream = new FileInputStream(file)
            }

            val byteChannel = fileInputStream.getChannel()

            byteChannel.position(fileLength)
            val bb = ByteBuffer.allocate(1024 * 8)
            byteChannel.read(bb)
            bb.flip()

            this.fileLength += channel.write(bb)

            Log.info("[%s] transffered [%d]" , file.getName , fileLength)
            this.fileLength match {
                case l if l == file.length() => {
                    this.isContinue = false
                    this.fileInputStream.close()
                    this.finishCommand("Transfer complete")
                }
                case _ => {
                    this.isContinue = true
                }
            }
            Log.info("RetrHandler end [%s]" , channel.getRemoteAddress().toString())
        }catch{
            case e:Exception => {
                Log.info("RetrHandler error [%s]" , e.getMessage())
                this.isContinue = false
                if(fileInputStream != null){
                    try{
                         fileInputStream.close()
                    }catch{
                        case e:Exception =>{}
                    }
                }
            }
        }finally{

        }

    }
}
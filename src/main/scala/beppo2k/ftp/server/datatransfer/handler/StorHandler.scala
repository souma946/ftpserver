package beppo2k.ftp.server.datatransfer.handler

import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import beppo2k.ftp.util.Log

class StorHandler(filePath:String) extends PassiveCommandHandler {

    private var fileLength:Long = _

    override def handle(key:SelectionKey) {
        Log.info("start StorHandler")
        val channel = key.channel().asInstanceOf[SocketChannel]
        val file = new File(this.filePath)

        val byteChannel = file.exists() match {
            case false => {
                new FileOutputStream(file).getChannel()
            }
            case true => {
                val append = (fileLength == 0) match {
                    case true => true
                    case false => false
                }
                new FileOutputStream(file , append).getChannel()
            }
        }
        val bb = ByteBuffer.allocate(1024 * 8)
        val readedSize = channel.read(bb)


        Log.info("read size[%s]" , readedSize)
        readedSize match {
            case r if r > 0 => {
                bb.flip()
                val writeLength = byteChannel.write(bb)
                Log.info("write size[%s]" , writeLength)
                this.fileLength += writeLength
                this.isContinue = true
            }
            case r if r == 0 => {
                this.isContinue = true
                Log.debug("uploading connection may be pending")
            }
            case r if r < 0 => {
                this.isContinue = false
                finishCommand("Transfer complete")
            }
        }
        byteChannel.close()
        Log.info("end StorHandler");
    }
}
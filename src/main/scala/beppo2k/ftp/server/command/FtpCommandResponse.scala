package beppo2k.ftp.server.command

import java.nio.ByteBuffer

class FtpCommandResponse(code:FtpReturnCode , msg:String) {

    def toBytes() :ByteBuffer = {

        val bb = ByteBuffer.allocate(1024)

        bb.put(this.code.getCodeAsBytes());
        bb.put(' '.asInstanceOf[Byte]);
        bb.put(this.msg.getBytes());
        bb.put('\r'.asInstanceOf[Byte]);
        bb.put('\n'.asInstanceOf[Byte]);
        bb.limit(bb.position());
        bb.rewind()
        return bb
    }
}
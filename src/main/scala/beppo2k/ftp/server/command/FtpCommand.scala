package beppo2k.ftp.server.command

import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import beppo2k.ftp.server.datatransfer.handler.{StorHandler, RetrHandler, NlstHandler}
import beppo2k.ftp.server.{FtpUserSession, DataTransferSession, DataTransferActor}
import beppo2k.ftp.server.datatransfer.Add
import beppo2k.ftp.util.{Log, FileUtil, NetworkUtil}
import java.io.File

abstract class FtpCommand(argument:String) {
   def execute(selector:Selector , key:SelectionKey)
}

class User(argument: String) extends FtpCommand(argument: String) {

    override def execute(selector: Selector, key: SelectionKey) {
        Log.info("User start")

        val channel = key.channel().asInstanceOf[SocketChannel];

        val addr = channel.socket().getRemoteSocketAddress().toString()
        val session = FtpUserSession.get(addr, selector, key)
        session.username = this.argument
        val res = new FtpCommandResponse(
                FtpReturnCode.USER_NAME_OKAY,
                "Please specify the password")
        val bb = res.toBytes()
        channel.write(bb)
        Log.info("User end")
    }
}

class Pass(argument: String) extends FtpCommand(argument: String) {
    override def execute(selector: Selector, key: SelectionKey) {
        Log.info("Pass start")
        val channel = key.channel().asInstanceOf[SocketChannel]
        val addr = channel.socket().getRemoteSocketAddress().toString()
        val session = FtpUserSession.get(addr, selector, key)
        session.password = this.argument

        val bb = session.login() match {
            case true => {
                new FtpCommandResponse(
                FtpReturnCode.USER_LOGGED_IN,
                "Login successful").toBytes()
            }
            case false => {
                new FtpCommandResponse(
                FtpReturnCode.NOT_LOGGED_IN,
                "Login incorrect").toBytes()
            }
        }

        channel.write(bb)
        Log.info("Pass end")
    }
}

class Cwd(argument: String) extends FtpCommand(argument: String) {
    override def execute(selector: Selector, key: SelectionKey) {
        Log.info("Cwd start")
        val channel = key.channel().asInstanceOf[SocketChannel]
        val addr = channel.socket().getRemoteSocketAddress().toString()
        val session = FtpUserSession.get(addr, selector, key)
        val path = argument

        val bb = session.changeCurrentDir(path) match {
            case true => {
                val res = new FtpCommandResponse(
                        FtpReturnCode.REQUESTED_FILE_ACTION_OKAY ,
                        "Directory successfully changed")
                res.toBytes()
            }
            case _ => {
                val res =  new FtpCommandResponse(
                        FtpReturnCode.REQUESTED_ACTION_NOT_TAKEN_PERMISSION_OR_SYSTEM,
                        "Failed to change directory")
                res.toBytes()
            }
        }
        channel.write(bb)
        Log.info("Cwd end")
    }
}

class Quit(argument: String) extends FtpCommand(argument: String) {
    override def execute(selector: Selector, key: SelectionKey) {
        Log.info("Quit start")

        val channel = key.channel().asInstanceOf[SocketChannel];
        val addr = channel.socket().getRemoteSocketAddress().toString()
        FtpUserSession.clear(addr)
        channel.close()
        Log.info("Quit end")
    }
}

class Pasv(argument: String) extends FtpCommand(argument: String) {
    override def execute(selector: Selector, key: SelectionKey) {
        Log.info("Pasv start")

        val channel = key.channel().asInstanceOf[SocketChannel];

        val addr = channel.socket().getRemoteSocketAddress().toString()
        val session = FtpUserSession.get(addr , selector , key)
        val actRef = DataTransferActor.get
        val dataTransferSession = new DataTransferSession(session , None)
        actRef ! Add(dataTransferSession.sourceChannel , SelectionKey.OP_ACCEPT)

        val msg = String.format(
                "Entering Passive Mode (%s)",
                createAddr(dataTransferSession.port))
        val res = new FtpCommandResponse(
                FtpReturnCode.ENTERING_PASSIVE_MODE,
                msg)
        val bb = res.toBytes()
        channel.write(bb)
        Log.info("Pasv end")
    }

    private def createAddr(port:Int) :String = {
        val first:Int = (port & 0xff00) >>> 8
        val second:Int = port & 0x00ff

        NetworkUtil.getLocalIpv4Address match {
            case Some(ip) => {
                return ip.toString().replaceFirst("/","").replaceAll("\\.",",") + "," + first + "," + second
            }
            case None => {
                throw new Exception()
            }
        }
    }
}

class Port(argument: String) extends FtpCommand(argument: String) {
    override def execute(selector: Selector, key: SelectionKey) {

        try{
            Log.info("Port start")

            val channel = key.channel().asInstanceOf[SocketChannel];
            val addr = channel.socket().getRemoteSocketAddress().toString()
            val session = FtpUserSession.get(addr , selector , key)
            val (ip,port) = parsePortCommand(argument)

            val dataTransferSession = new DataTransferSession(session , Some((ip,port)))
            val actRef = DataTransferActor.get
            actRef ! Add(dataTransferSession.sourceChannel , SelectionKey.OP_CONNECT)

            val res = new FtpCommandResponse(
                    FtpReturnCode.COMMAND_OKAY,
                    "PORT command successful")
            val bb = res.toBytes()
            channel.write(bb)

            Log.info("Port end")
        }catch{
            case e:Exception => {
                e.printStackTrace()
                Log.error("[%s]" , e.getMessage())
            }
        }
    }

    private def parsePortCommand(arg:String) :(String,Int) = {

        val array = arg.split(",")

        val ipAddr = array.slice(0,4)
        val portFirst = array(4)
        val portSecond = array(5)

        val portStr = portFirst.toInt.formatted("%02x") + portSecond.toInt.formatted("%02x")
        val port = Integer.parseInt(portStr , 16)
        return (ipAddr.mkString(".") , port)
    }
}

class Stor(argument: String) extends FtpCommand(argument: String) {
    override def execute(selector: Selector, key: SelectionKey) {
        Log.info("Stor start")
        val channel = key.channel().asInstanceOf[SocketChannel];
        val addr = channel.socket().getRemoteSocketAddress().toString()

        val session = FtpUserSession.get(addr , selector , key)

        val filePath:String = argument match {
            case f if f.startsWith("/") || f.startsWith("C:" + File.separator) => {
                val newPath = f
                newPath
            }
            case f => {
                val newPath = session.currentDir + File.separator + f
                newPath
            }
        }

        val handler = new StorHandler(filePath)
        session.handler = handler
        handler.comanndSelector = selector
        handler.commandConnectionKey = key
        val res = new FtpCommandResponse(
                FtpReturnCode.FILE_STATUS_OKAY ,
                "Ok to send data")
        val bb = res.toBytes()
        channel.write(bb)
        Log.info("Stor end")
        session.canTransfer = true
    }
}

class Retr(argument: String) extends FtpCommand(argument: String) {
    override def execute(selector: Selector, key: SelectionKey) {
        Log.info("Retr start")
        val channel = key.channel().asInstanceOf[SocketChannel];
        val addr = channel.socket().getRemoteSocketAddress().toString()

        val session = FtpUserSession.get(addr , selector , key)

        val filePath:String = argument match {
            case f if f.startsWith("/") || f.startsWith("C:" + File.separator) => {
                val newPath = f
                newPath
            }
            case f => {
                val newPath = session.currentDir + File.separator + f
                newPath
            }
        }

        val handler = new RetrHandler(filePath)
        session.handler = handler
        handler.comanndSelector = selector
        handler.commandConnectionKey = key
        val res = new FtpCommandResponse(
                FtpReturnCode.FILE_STATUS_OKAY ,
                "Opening BINARY mode data connection for ")
        val bb = res.toBytes()
        channel.write(bb)
        Log.info("Retr end")
        session.canTransfer = true
    }
}

class Rmd(argument: String) extends FtpCommand(argument: String) {
    override def execute(selector: Selector, key: SelectionKey) {
        Log.info("Rmd start")
        val channel = key.channel().asInstanceOf[SocketChannel]
        val addr = channel.socket().getRemoteSocketAddress().toString()

        val session = FtpUserSession.get(addr , selector , key);

        val dir = this.argument match {
            case d if d.startsWith("/") || d.startsWith("C:" + File.separator) => {
                this.argument
            }
            case _ => {
                session.currentDir + File.separator + this.argument
            }
        }

        val bb = FileUtil.deleteDir(new File(dir)) match {
            case true => {
                new FtpCommandResponse(
                        FtpReturnCode.FILE_STATUS_OKAY,
                        "Succeed to delete directory").toBytes()
            }
            case false => {
                new FtpCommandResponse(
                        FtpReturnCode.REQUESTED_ACTION_NOT_TAKEN_PERMISSION_OR_SYSTEM,
                        "Failed to delete directory").toBytes()
            }

        }
        channel.write(bb);
        Log.info("Rmd end")
    }

    private def delete(file:File) :Unit = {
        file.exists() match {
            case true => {
                file.isDirectory() match {
                    case true => {
                        for(f <- file.listFiles()){
                            delete(f)
                        }
                    }
                    case false => {
                        file.delete()
                    }
                }
            }
            case false => {
                // none
            }
        }
    }
}

class Mkd(argument: String) extends FtpCommand(argument: String) {
    override def execute(selector: Selector, key: SelectionKey) {
        Log.info("Mkd start")
        val channel = key.channel().asInstanceOf[SocketChannel]
        val addr = channel.socket().getRemoteSocketAddress().toString()

        val session = FtpUserSession.get(addr , selector , key);

        val dir = this.argument match {
            case d if d.startsWith("/") || d.startsWith("C:" + File.separator) => {
                this.argument
            }
            case _ => {
                session.currentDir + File.separator + this.argument
            }
        }

        val bb = new File(dir).mkdirs() match {
            case true => {
                new FtpCommandResponse(
                        FtpReturnCode.FILE_STATUS_OKAY,
                        "Succeed to create directory").toBytes()
            }
            case false => {
                new FtpCommandResponse(
                        FtpReturnCode.REQUESTED_ACTION_NOT_TAKEN_PERMISSION_OR_SYSTEM,
                        "Failed to create directory").toBytes()
            }
        }
        channel.write(bb);
        Log.info("Mkd end")
    }
}

class Pwd(argument: String) extends FtpCommand(argument: String) {
    override def execute(selector: Selector, key: SelectionKey) {
        Log.info("Pwd start")

        val channel = key.channel().asInstanceOf[SocketChannel];
        val addr = channel.socket().getRemoteSocketAddress().toString()

        val session = FtpUserSession.get(addr , selector , key)

        val path = session.getCurrentDir()
        val res = new FtpCommandResponse(
                FtpReturnCode.PATHNAME_CREATED ,
                path)
        val bb = res.toBytes()
        channel.write(bb)
        Log.info("Pwd end")
    }
}

class List(argument: String) extends FtpCommand(argument: String) {
    override def execute(selector: Selector, key: SelectionKey) {
        Log.info("List start")
        val channel = key.channel().asInstanceOf[SocketChannel];
        val addr = channel.socket().getRemoteSocketAddress().toString()

        val session = FtpUserSession.get(addr , selector , key)
        val isDetail = (argument != null && !argument.equals(""))
        val handler = new NlstHandler(session.currentDir , isDetail)
        session.handler = handler
        handler.comanndSelector = selector
        handler.commandConnectionKey = key
        val res = new FtpCommandResponse(
                FtpReturnCode.FILE_STATUS_OKAY ,
                "Here comes the directory listing")
        val bb = res.toBytes()
        channel.write(bb)
        Log.info("List end")
        session.canTransfer = true
    }
}

class Syst(argument: String) extends FtpCommand(argument: String) {
    override def execute(selector: Selector, key: SelectionKey) {
        Log.info("Syst start")
        val channel = key.channel().asInstanceOf[SocketChannel];
        val res = new FtpCommandResponse(
                FtpReturnCode.NAME_SYSTEM_TYPE ,
                "teset server")
        val bb = res.toBytes()
        channel.write(bb)
        Log.info("Syst end")
    }
}

class Stat(argument: String) extends FtpCommand(argument: String) {
    override def execute(selector: Selector, key: SelectionKey) {
    }
}

class Help(argument: String) extends FtpCommand(argument: String) {
    override def execute(selector: Selector, key: SelectionKey) {
    }
}

class Noop(argument: String) extends FtpCommand(argument: String) {
    override def execute(selector: Selector, key: SelectionKey) {
    }
}

class Acct(argument: String) extends FtpCommand(argument: String) {
    override def execute(selector: Selector, key: SelectionKey) {
    }
}

class Cdup(argument: String) extends FtpCommand(argument: String) {
    override def execute(selector: Selector, key: SelectionKey) {
    }
}

class Smnt(argument: String) extends FtpCommand(argument: String) {
    override def execute(selector: Selector, key: SelectionKey) {
    }
}

class Rein(argument: String) extends FtpCommand(argument: String) {
    override def execute(selector: Selector, key: SelectionKey) {
    }
}

class Stru(argument: String) extends FtpCommand(argument: String) {
    override def execute(selector: Selector, key: SelectionKey) {
    }
}

class Mode(argument: String) extends FtpCommand(argument: String) {
    override def execute(selector: Selector, key: SelectionKey) {
    }
}

class Stou(argument: String) extends FtpCommand(argument: String) {
    override def execute(selector: Selector, key: SelectionKey) {
    }
}

class Appe(argument: String) extends FtpCommand(argument: String) {
    override def execute(selector: Selector, key: SelectionKey) {
    }
}

class Allo(argument: String) extends FtpCommand(argument: String) {
    override def execute(selector: Selector, key: SelectionKey) {
    }
}

class Rest(argument: String) extends FtpCommand(argument: String) {
    override def execute(selector: Selector, key: SelectionKey) {
    }
}

class Rnfr(argument: String) extends FtpCommand(argument: String) {
    override def execute(selector: Selector, key: SelectionKey) {
    }
}

class Rnto(argument: String) extends FtpCommand(argument: String) {
    override def execute(selector: Selector, key: SelectionKey) {
    }
}

class Abor(argument: String) extends FtpCommand(argument: String) {
    override def execute(selector: Selector, key: SelectionKey) {
    }
}

class Dele(argument: String) extends FtpCommand(argument: String) {
    override def execute(selector: Selector, key: SelectionKey) {
    }
}

class Site(argument: String) extends FtpCommand(argument: String) {
    override def execute(selector: Selector, key: SelectionKey) {
    }
}

class Nlst(argument: String) extends FtpCommand(argument: String) {
    override def execute(selector: Selector, key: SelectionKey) {
    }
}

class FtpConnectionEstablish(argument: String = null) extends FtpCommand(argument: String) {
    override def execute(selector: Selector, key: SelectionKey) {
        Log.info("FtpConnectionEstablish start")
        val channel = key.channel().asInstanceOf[SocketChannel];
        val res = new FtpCommandResponse(
                FtpReturnCode.SERVICE_READY_FOR_NEW_USER ,
                "test server")
        val bb = res.toBytes()
        channel.write(bb)
        Log.info("FtpConnectionEstablish end")
    }
}

class FtpCommandNotFound(argument: String = null) extends FtpCommand(argument: String) {
    override def execute(selector: Selector, key: SelectionKey) {
        Log.info("FtpCommandNotFound start")
        val channel = key.channel().asInstanceOf[SocketChannel]
        val res = new FtpCommandResponse(
                FtpReturnCode.COMMAND_NOT_IMPLEMENTED,
                "Invalid Command")
        val bb = res.toBytes()
        channel.write(bb)
        Log.info("FtpCommandNotFound end")
    }
}

class FtpDataConnectionFinishCommand(commandSelector: Selector, commandKey: SelectionKey , argument: String) extends FtpCommand(argument: String) {
    override def execute(selector: Selector, key: SelectionKey) {
        Log.info("FtpDataConnectionFinishCommand start")
        val channel = commandKey.channel().asInstanceOf[SocketChannel]
        val res = new FtpCommandResponse(
                FtpReturnCode.CLOSING_DATA_CONNECTION,
                argument)
        val bb = res.toBytes()
        channel.write(bb)
        Log.info("FtpDataConnectionFinishCommand end")
    }

}
package beppo2k.ftp.server

import beppo2k.ftp.server.datatransfer.{FtpDataTransfer}
import beppo2k.ftp.util.Log

import scala.collection.mutable
import java.nio.channels.SocketChannel
import java.nio.channels.Selector
import java.nio.channels.SelectionKey
import scala.collection.mutable.Map
import java.nio.channels.SelectableChannel

class DataTransferSession(commandSession:FtpUserSession , active:Option[(String , Int)]) {

    val (sourceChannel , addr , port) = active match {
        case Some((ip,port)) => {
            val socket:SocketChannel = FtpDataTransfer.createClientSocket(ip , port)
            (socket,
            socket.getRemoteAddress().toString(),
            port)
        }
        case _ => {
            val socket = FtpDataTransfer.createServerSocket()
            (socket,
            socket.socket().getLocalSocketAddress().toString() ,
            socket.socket().getLocalPort())
        }
    }

    var connectedChannel:SocketChannel = _

    Log.info("create data transfer session [%d]" , port)

    DataTransferSession.putSourceSession(addr , this)

    def canTransfer() :Boolean = {
        return this.commandSession.canTransfer
    }

    def isContinue() :Boolean = {
        return this.commandSession.handler.isContinue
    }

    def executeTransfer(selector:Selector , key:SelectionKey) :Unit = {
        this.commandSession.handler.handle(selector , key)
    }

    def finishTransfer() = {
        // clean up
        Log.info("host[%s] remote[%s]data connection close",
            this.addr,
            this.connectedChannel.getRemoteAddress().toString())
        commandSession.canTransfer = false
        sourceChannel.close()
        this.connectedChannel.close()
    }
}

object DataTransferSession{

    val sourceSession:mutable.Map[String, DataTransferSession] = mutable.Map()

    val connectedSession:mutable.Map[String, DataTransferSession] = mutable.Map()

    def putSourceSession(sourceAddr:String , data:DataTransferSession)  = {
        sourceSession.put(sourceAddr , data)
    }

    def getSourceSession(sourceAddr:String) :Option[DataTransferSession] = {
        return sourceSession.get(sourceAddr)
    }

    def moveSourceSessionToConnectedSession(sourceAddr:String , connectedAddr:String) :Option[DataTransferSession] = {
        sourceSession.get(sourceAddr) match {
            case Some(session) => {
                connectedSession.put(connectedAddr , session)
                sourceSession.remove(sourceAddr)
                return Some(session)
            }
            case None => {
                // unexpected situation..
                Log.error("source session doesn't exist")
                return None
            }
        }
    }

    def getConnectSession(connectedAddr:String) :Option[DataTransferSession] = {
        return connectedSession.get(connectedAddr)
    }
}
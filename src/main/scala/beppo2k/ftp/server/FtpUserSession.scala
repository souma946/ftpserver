package beppo2k.ftp.server

import java.nio.channels.Selector
import java.nio.channels.SelectionKey
import java.io.File
import java.nio.file.Path
import beppo2k.ftp.server.datatransfer.handler.PassiveCommandHandler
import beppo2k.ftp.util.Log

import scala.collection.mutable.HashMap

class FtpUserSession(var selector:Selector , var key:SelectionKey) {

    var currentDir = new File("./").toPath().toRealPath().toString()

    var handler:PassiveCommandHandler = _

    @volatile
    var canTransfer:Boolean = _

    def changeCurrentDir(dir:String) :Boolean = {

        val path:Path = new File(this.currentDir + File.separator + dir).toPath()

        val ret = path match {
            case p if p.toFile().isDirectory() => {
                this.currentDir = path.toRealPath().toAbsolutePath().toString()
                return true
            }
            case _ => {
                return false
            }
        }
        return ret
    }
}

object FtpUserSession {

    private val sessions = new HashMap[String,FtpUserSession]()

    def get(ipPort:String , selector:Selector , key:SelectionKey) :FtpUserSession = {
        return sessions.get(ipPort) match {
            case Some(s) => {
                Log.info("sessoin exists")
                s
            }
            case None => {
                Log.info("sessoin create")
                val s = new FtpUserSession(selector , key)
                sessions.put(ipPort , s)
                s
            }
        }
    }

    def clear(ipPort:String) = {
        sessions.remove(ipPort)
    }
}
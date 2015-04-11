package beppo2k.ftp.server

import java.nio.channels.Selector
import java.nio.channels.SelectionKey
import java.io.File
import java.nio.file.Path

import beppo2k.ftp.auth.{UnixFtpAuth, FtpAuth, WinFtpAuth}
import beppo2k.ftp.server.datatransfer.handler.PassiveCommandHandler
import beppo2k.ftp.util.Log
import com.sun.jna.{Platform, Native, Library}
import sun.plugin2.ipc.unix.UnixIPCFactory

import scala.collection.mutable.HashMap

class FtpUserSession(var selector:Selector , var key:SelectionKey) {

    var currentDir:String = _

    var handler:PassiveCommandHandler = _

    var username:String = _

    var password:String = _

    var isLogin:Boolean = _

    var auth:FtpAuth = _

    @volatile
    var canTransfer:Boolean = _

    def login() :Boolean = {

        val auth = Platform.isWindows match {
            case true => {new WinFtpAuth()}
            case false => {new UnixFtpAuth()}
        }
        this.isLogin = auth.login(username,password)
        this.auth = auth
        if(isLogin){
            this.currentDir = auth.getHomeDir(username)
        }
        return this.isLogin
    }

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
                Log.info("session exists")
                s
            }
            case None => {
                Log.info("session create")
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
package beppo2k.ftp.server

import java.nio.channels.Selector
import java.nio.channels.SelectionKey
import java.io.File
import java.nio.file.Path

import beppo2k.ftp.auth.{UnixFtpAuth, FtpAuth, WinFtpAuth}
import beppo2k.ftp.server.datatransfer.handler.PassiveCommandHandler
import beppo2k.ftp.util.{FileUtil, Log}
import com.sun.jna.{Platform, Native, Library}

import scala.collection.mutable.HashMap

class FtpUserSession(var selector:Selector , var key:SelectionKey) {

    var currentDir:String = _

    var homeDir:String = _

    var handler:PassiveCommandHandler = _

    var username:String = _

    var password:String = _

    var isLogin:Boolean = _

    @volatile
    var canTransfer:Boolean = _

    def login() :Boolean = {

        val auth = Platform.isWindows match {
            case true  => new WinFtpAuth()
            case false => new UnixFtpAuth()
        }

        this.isLogin = auth.login(username,password)
        if(isLogin){
            this.homeDir = auth.getHomeDir(username)
            this.currentDir = this.homeDir
        }
        return this.isLogin
    }

    def getCurrentDir() :String = {
        return FileUtil.normalizePath(this.homeDir , this.currentDir) match {
            case Some(path) => return path
            case None => return ""
        }

    }
    def changeCurrentDir(dir:String) :Boolean = {
/*
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
*/
        FileUtil.changeCurrentDir(this.homeDir , this.currentDir , dir) match {
            case None => return false
            case Some(newCurrentPath) => {
                this.currentDir = newCurrentPath
                return true
            }
        }
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

package beppo2k.ftp.util

import java.text.SimpleDateFormat
import java.util.Date
import java.io.PrintStream

trait LogLevel{
    val level:Int
    val levelStr:String

    override def toString() :String = {
        return levelStr
    }
}
case class Debug(level:Int = 0 , levelStr:String = "DEBUG") extends LogLevel
case class Info(level:Int = 1 , levelStr:String = "INFO") extends LogLevel
case class Warn(level:Int = 2 , levelStr:String = "WARN") extends LogLevel
case class Error(level:Int = 3 , levelStr:String = "ERROR") extends LogLevel

object Log {

    var logLevel:LogLevel = Warn()

    val dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    def debug(format:String , param:Any*) = {
        writeLog(System.out, Debug() , format , param: _*)
    }

    def info(format:String , param:Any*) = {
        writeLog(System.out, Info() , format , param: _*)
    }

    def warn(format:String , param:Any*) = {
        writeLog(System.out, Warn() , format , param: _*)
    }

    def error(format:String , param:Any*) = {
        writeLog(System.err, Error() , format , param: _*)
    }

    private def writeLog(stream:PrintStream , level:LogLevel , format:String , param:Any*) = {

        if(logLevel.level <= level.level){
            val f = dateFormatter.format(new Date()) + " [" + level.toString() +"] " + format
            try{
                val s = f.format(param: _*)
                stream.println(s)
            }catch{
                case e:Exception => {
                    e.printStackTrace()
                }
            }
        }
   }
}
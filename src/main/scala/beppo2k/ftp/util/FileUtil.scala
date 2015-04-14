package beppo2k.ftp.util

import java.io.File
import java.nio.file.attribute._
import java.nio.file.{Path, Files}
import java.text.SimpleDateFormat

import com.sun.jna.{Platform}

object FileUtil {

    private def deleteFiles(file:File) :Boolean = {
        file.exists() match {
            case true => {
                file.isFile() match {
                    case true => {
                        return deleteFile(file)
                    }
                    case false => {
                        file.listFiles().map(deleteDir(_)).find(_ == false) match {
                            case Some(ret) => {
                                return ret
                            }
                            case None => {
                                return true
                            }
                        }
                    }
                }
            }
            case false => {
                return false
            }
        }
    }

    def deleteDir(file:File) :Boolean = {

        if(!(file.exists() && file.isDirectory())){
            return false
        }

        deleteFiles(file) match {
            case true => {
                return file.delete()
            }
            case false => {
                return false
            }
        }
    }

    def deleteFile(file:File) :Boolean = {
        file.exists() && file.isFile() match {
            case false => {
                return false
            }
            case true => {
                return file.delete()
            }
        }
    }

    def list(file:File , isDetail:Boolean = false) :String = {
        val sb = new StringBuilder()
        file match {
            case d if d.isDirectory() => {
                for(f <- d.listFiles()){
                    if(isDetail) sb.append(detail(f))
                    sb.append(f.getName).append("\r\n")
                }
            }
            case f if f.isFile() => {
                if(isDetail) sb.append(detail(f))
                sb.append(f.getName).append("\r\n")
            }
            case _ => {
                sb.append("\r\n")
            }
        }
        return sb.toString()
    }

    val dfmt = new SimpleDateFormat("yyyy/MM/dd  hh:mm")
    def detail(file:File) :String =  {
        Platform.isLinux match {
            case true => {
                val attr = Files.getFileAttributeView(file.toPath , classOf[PosixFileAttributeView]).readAttributes()
                return "%s %s ".format(attr.owner().getName() , PosixFilePermissions.toString(attr.permissions()))
            }
            case _ => {
                val attr = Files.getFileAttributeView(file.toPath , classOf[DosFileAttributeView]).readAttributes()
                val strDir = file.isDirectory() match {
                    case true => "<DIR>"
                    case _ => "     "
                }
                return "%s %s ".format(dfmt.format(new java.util.Date(file.lastModified())) , strDir)
            }
        }
    }
}
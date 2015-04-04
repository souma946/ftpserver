package beppo2k.ftp.util

import java.io.File
import java.nio.file.attribute.PosixFilePermissions
import java.nio.file.Files

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

    def list(file:File) :String = {
        val sb = new StringBuilder()
        file match {
            case d if d.isDirectory() => {
                for(f <- d.listFiles()){
                    sb.append(f.getName).append("\r\n")
                }
            }
            case f if f.isFile() => {
                sb.append(f.getName).append("\r\n")
            }
            case _ => {
                sb.append("\r\n")
            }
        }
        return sb.toString()
    }
}
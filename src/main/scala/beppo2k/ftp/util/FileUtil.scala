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


    def detail(file:File) :String =  {
        Platform.isWindows() match {
            case true => {
                val dfmt = new SimpleDateFormat("MMM dd yyyy hh:mm")
                val strDir = file.isDirectory() match {case true => "<DIR>" case _ => "     " }
                return "%s %s ".format(
                    dfmt.format(new java.util.Date(file.lastModified())) ,
                    strDir)
            }
            case false => {
                val attr = Files.getFileAttributeView(file.toPath , classOf[PosixFileAttributeView]).readAttributes()
                val isDir = file.isDirectory() match { case true => "d" case false => "-" }
                val dfmt = new SimpleDateFormat("MMM dd yyyy hh:mm")
                return "%s%s %10s %10s %10d %s ".format(
                    isDir,
                    PosixFilePermissions.toString(attr.permissions()) ,
                    attr.owner().getName() ,
                    attr.group().getName() ,
                    attr.size() ,
                    dfmt.format(new java.util.Date(file.lastModified())))
            }

        }
    }

    private def getHomeCurrentPath(homeDir:String , currentDir:String) :Option[(Path , Path)] = {
        val homeFile = new File(homeDir)
        if(!homeFile.exists) return None
        val homePath = homeFile.toPath.toAbsolutePath

        val currentFile = new File(currentDir)
        if(!currentFile.exists) return None
        val currentPath = currentFile.toPath.toAbsolutePath

        return Some((homePath , currentPath))
    }

    def normalizePath(homeDir:String , currentDir:String) :Option[String] = {
        val (homePath , currentPath) = getHomeCurrentPath(homeDir , currentDir) match {
            case None => return None
            case Some(t) => t
        }

        return Platform.isWindows match {
            case true => {
                val h = homePath.toString.replaceAll("\\\\","/")
                currentPath.toString.replaceAll("\\\\","/").replaceAll(h,"") match {
                    case "" => Some("/")
                    case cc => Some(cc)
                }
            }
            case false => {
                val c = currentPath.toString.replaceAll(homePath.toString , "/")
                Some(c)
            }
        }
    }

    def changeCurrentDir(homeDir:String , currentDir:String , targetDir:String) :Option[String] = {
        val (homePath , currentPath) = getHomeCurrentPath(homeDir , currentDir) match {
            case None => return None
            case Some(t) =>  t
        }

        val movedCurrentDir = new File(currentPath.toString + File.separator + targetDir)
        if(!movedCurrentDir.exists()) return None

        val movedCurrentPath = movedCurrentDir.toPath().toRealPath()

        if(!movedCurrentPath.startsWith(homePath)){
            return None
        }

        return normalizePath(homeDir , movedCurrentPath.toString) match {
            case None => None
            case Some(_) => Some(movedCurrentPath.toString)
        }
    }
}
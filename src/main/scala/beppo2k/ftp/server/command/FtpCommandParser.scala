package beppo2k.ftp.server.command

import java.nio.channels.SocketChannel
import java.nio.ByteBuffer
import beppo2k.ftp.util.Log

import scala.util.control.Breaks

object FtpCommandParser {

    def parseCommand(channel: SocketChannel): FtpCommand = {

        val bbf = ByteBuffer.allocate(1)
        val line = new Array[Byte](1024)

        var idx = 0

        bbf.rewind()

        val b1 = new Breaks

        b1.breakable {
            while ((channel.read(bbf)) > 0) {
                bbf.flip();
                line(idx) = bbf.get()
                idx = idx + 1

                bbf.rewind()

                if (idx >= 1) {
                    if (line(idx) == '\n' && line(idx - 1) == '\r') {
                        b1.break
                    }
                } else if (idx >= 1024) {
                    b1.break
                }
            }
        }

        if (idx == 0) {
            return null
        }

        var cmd: String = null
        var args: String = null

        b1.breakable {
            for (i <- 0 until line.length) {
                if (cmd == null && ((line(i) == ' ') || (line(i) == '\n' && line(i - 1) == '\r'))) {
                    val c = new Array[Byte](i)
                    System.arraycopy(line, 0, c, 0, i)
                    cmd = new String(c)
                    if ((line(i) == '\n' && line(i - 1) == '\r')) {
                        cmd = cmd.replaceAll("[\\r\\n]+.*$", "");
                        b1.break
                    }
                } else if (cmd != null && line(i - 1) == ' ') {
                    val argLen = line.length - i;
                    val a = new Array[Byte](argLen)
                    System.arraycopy(line, i, a, 0, argLen);
                    args = new String(a);
                    args = args.replaceAll("[\\r\\n]+.*$", "");
                    b1.break
                }
            }
        }

        Log.info("cmd[%s] arg[%s]" , cmd , args)

        val cmdClass = cmd match {
            case "USER" =>  new User(args)
            case "PASS" =>  new Pass(args)
            case "CWD"  =>  new Cwd(args)
            case "QUIT" =>  new Quit(args)
            case "PASV" =>  new Pasv(args)
            case "RETR" =>  new Retr(args)
            case "STOR" =>  new Stor(args)
            case "RMD"  =>  new Rmd(args)
            case "MKD"  =>  new Mkd(args)
            case "PWD" =>  new Pwd(args)
            case "XPWD" =>  new Pwd(args)
            case "LIST" =>  new List(args)
            case "NLST" =>  new List(args)
            case "SYST" =>  new Syst(args)
            case "PORT" =>  new Port(args)
//            case "ACCT" =>  new Acct(args)
//            case "CDUP" =>  new Cdup(args)
//            case "SMNT" =>  new Smnt(args)
//            case "REIN" =>  new Rein(args)
//            case "PORT" =>  new Port(args)
//            case "STRU" =>  new Stru(args)
//            case "MODE" =>  new Mode(args)
//            case "STOU" =>  new Stou(args)
//            case "APPE" =>  new Appe(args)
//            case "ALLO" =>  new Allo(args)
//            case "REST" =>  new Rest(args)
//            case "RNTO" =>  new Rnto(args)
//            case "ABOR" =>  new Abor(args)
//            case "DELE" =>  new Dele(args)
//            case "NLST" =>  new Nlst(args)
//            case "SITE" =>  new Site(args)
//            case "STAT" =>  new Stat(args)
//            case "HELP" =>  new Help(args)
//            case "NOOP" =>  new Noop(args)
            case _ => new FtpCommandNotFound()
        }
        return cmdClass
    }
}
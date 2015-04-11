package beppo2k.ftp.server

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import beppo2k.ftp.server.command.FtpCommandServer
import beppo2k.ftp.server.datatransfer.{FtpDataTransferActor, Start}

object DataTransferActor {
    val system = ActorSystem("system")
    val ref = system.actorOf(Props[FtpDataTransferActor])

    def get :ActorRef = {
        return ref
    }
}
object Main extends App {

    val port =  (super.args != null && super.args.length == 1) match {
        case true => args(0).toInt
        case false => 21
    }
    val actRef = DataTransferActor.get
    actRef ! Start()
    val ftpServer = new FtpCommandServer(port)
    ftpServer.start()
}
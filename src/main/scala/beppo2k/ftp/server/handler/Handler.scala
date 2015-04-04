package beppo2k.ftp.server.handler

import java.nio.channels.Selector
import java.nio.channels.SelectionKey

trait Handler {
    def handle(selector:Selector , key:SelectionKey )
}
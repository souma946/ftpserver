package beppo2k.ftp.util

import java.net.NetworkInterface
import scala.collection.JavaConversions._
import java.net.Inet6Address
import java.net.Inet4Address
import java.net.InetAddress

object NetworkUtil {

    def getLocalIpv4Address :Option[InetAddress] = {
        for(interface <- NetworkInterface.getNetworkInterfaces()){
            for(ipaddress <- interface.getInetAddresses()){
                if(ipaddress.isAnyLocalAddress()){
                    // *
                }else if(ipaddress.isLoopbackAddress()){
                    // 127.0.0.1
                }else if(ipaddress.isInstanceOf[Inet6Address]){
                    // 0:0:...
                }else if(ipaddress.isInstanceOf[Inet4Address]){
                    return Some(ipaddress)
                }
            }
        }
        return None
    }
}
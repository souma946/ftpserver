# ftpserver
=====
FTP Server written in scala.

## Description
This is a test project for study scala and non-blocking I/O. Most of ftp commands don't work correctly.
Now support following commands.

USER  
PASS  
PASV  
STOR  
RETR  
SYST  
LIST  
MKDIR  
RMDIR

Login ,file permission and home directory don't work.

## Usage
java -jar ftpserver-assembly-0.0.1.jar <port>  

## Install
git clone https://github.com/beppo2k/ftpserver.git   
sbt assembly

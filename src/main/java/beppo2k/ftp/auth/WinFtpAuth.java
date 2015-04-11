package beppo2k.ftp.auth;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinNT.HANDLEByReference;
import com.sun.jna.ptr.IntByReference;

import java.net.InetAddress;

public class WinFtpAuth implements FtpAuth{

    private static final int PROFILE_DIR_BUFF_SIZE = 10000;

    private interface Userenv extends Library {

        Userenv INSTANCE = (Userenv) Native.loadLibrary("Userenv", Userenv.class);

        boolean GetUserProfileDirectoryW(HANDLE hToken , char[] lpProfileDir , IntByReference lpcchSize);
    }

    private HANDLE hToken;

    @Override
    public boolean login(String username , String password){
        try{
            HANDLEByReference phUser = new HANDLEByReference();
            boolean result = Advapi32.INSTANCE.LogonUser(
                    username,
                    InetAddress.getLocalHost().getHostName(),
                    password,
                    WinBase.LOGON32_LOGON_NETWORK,
                    WinBase.LOGON32_PROVIDER_DEFAULT,
                    phUser);

            if(result){
                this.hToken = phUser.getValue();
            }
            return result;
        }catch(Throwable th){
            th.printStackTrace();
            return false;
        }
    }

    @Override
    public String getHomeDir(String username){
        try{
            char[] lpProfileDir = new char[PROFILE_DIR_BUFF_SIZE];
            IntByReference lpcchSize = new IntByReference(lpProfileDir.length);

            boolean result = Userenv.INSTANCE.GetUserProfileDirectoryW(
                    hToken,
                    lpProfileDir,
                    lpcchSize);

            if(result){
                int len = lpcchSize.getValue();
                return new String(lpProfileDir , 0 , len - 1);
            }else{
                return null;
            }
        }catch(Throwable th){
            th.printStackTrace();
            return null;
        }
    }
}

package beppo2k.ftp.auth;

/**
 * Created by Yoshiyuki on 2015/04/11.
 */
public interface FtpAuth {
    public boolean login(String username , String password);
    public String getHomeDir(String username);
}

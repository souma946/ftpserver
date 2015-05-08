package beppo2k.ftp.auth;

public interface FtpAuth {
    public boolean login(String username , String password);
    public String getHomeDir(String username);
}

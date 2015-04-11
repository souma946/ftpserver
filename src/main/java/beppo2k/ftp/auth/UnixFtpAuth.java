package beppo2k.ftp.auth;

public class UnixFtpAuth implements FtpAuth{

    @Override
    public native boolean login(String username , String password);
    public native String get_home_dir(String username);

    static {
        System.loadLibrary("ftpauth");
    }

    @Override
    public String getHomeDir(String username){
        return get_home_dir(username);
    }
    /*
    public static void main(String[] args) {
        UnixFtpAuth auth = new UnixFtpAuth();

        if(args == null || args.length != 2){
            System.err.println("invalid arguments");
            return;
        }

        String username = args[0];
        String password = args[1];

        System.out.println("login start");
        boolean result = auth.login(username , password);
        System.out.println("login result " + result);

        System.out.println("home start");
        String homeDir = auth.get_home_dir(username);

        System.out.println("home dir " + homeDir);
        System.out.println("home end");
    }
    */
}

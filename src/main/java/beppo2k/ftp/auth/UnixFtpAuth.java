package beppo2k.ftp.auth;

import org.jvnet.libpam.PAM;
import org.jvnet.libpam.UnixUser;

public class UnixFtpAuth implements FtpAuth {

	private UnixUser user;

	@Override
    public boolean login(String username , String password){
		try{
			PAM pam = new PAM("login");
			UnixUser user = pam.authenticate(username , password);
			this.user = user;
			return true;
		}catch(Exception e){
			return false;
		}	}

    @Override
    public String getHomeDir(String username){
		if(this.user != null){
			return this.user.getDir();
		}
		return null;
    }
}

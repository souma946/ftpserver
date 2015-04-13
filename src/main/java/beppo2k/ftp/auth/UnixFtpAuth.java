package beppo2k.ftp.auth;

import com.sun.jna.*;
import java.util.*;

public class UnixFtpAuth implements FtpAuth {

	public interface CLibrary extends Library {
		CLibrary INSTANCE = (CLibrary)Native.loadLibrary("c" , CLibrary.class);
		spwd getspnam(String username);

		passwd getpwnam(String username);
	}

	public interface Crypt extends Library {
		Crypt INSTANCE = (Crypt)Native.loadLibrary("crypt" , Crypt.class);
		String crypt(String key , String salt);
	}

	public static class spwd extends Structure {
	    public String sp_namp;
	    public String sp_pwdp;
	    public long sp_lstchg;
	    public long sp_min;
	    public long sp_max;
	    public long sp_warn;
	    public long sp_inact;
	    public long sp_expire;
	    public long sp_flag;

		protected List<String> getFieldOrder() {
    		return Arrays.asList(new String[] { "sp_namp", "sp_pwdp", "sp_lstchg", "sp_min", "sp_max" , "sp_warn" , "sp_inact" , "sp_expire" , "sp_flag" });
		}
	}

	
	public static class passwd extends Structure{
	    public String pw_name;
	    public String pw_passwd;
	    public int pw_uid;
	    public int pw_gid;
	    public String pw_gecos;
	    public String pw_dir;
	    public String pw_shell;
	
		protected List<String> getFieldOrder() {
			return Arrays.asList(new String[] { "pw_name", "pw_passwd", "pw_uid", "pw_gid", "pw_gecos" , "pw_dir" , "pw_shell"});
		}
	}

	@Override
    public boolean login(String username , String password){

		spwd s = CLibrary.INSTANCE.getspnam(username);

		if(s != null){
			String result = Crypt.INSTANCE.crypt(password , s.sp_pwdp);
			if(result != null){
				if(result.equals(s.sp_pwdp)){
					return true;
				}
			}
		}
		return false;
	}

    @Override
    public String getHomeDir(String username){
		passwd p = CLibrary.INSTANCE.getpwnam(username);

		String result = null;
		if(p == null){
			result =  null;
		}else{
			result =  p.pw_dir;
		}
        return result;
    }
}

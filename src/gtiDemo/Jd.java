package gtiDemo;

import java.util.Collection;
import java.util.List;

public class Jd {

	public void name() {
		
		   UserFormMap userFormMap = (UserFormMap) Common.findUserSession();
	        NavbarFormMap navbarFormMap = new NavbarFormMap();
	        navbarFormMap.put("userId", userFormMap.get("id"));
	        navbarFormMap.put("pageLevel", "1");
	        List<NavbarFormMap> mps = navbarMapper.findRes(navbarFormMap);
	        return mps;
	}
}

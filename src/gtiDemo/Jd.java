package gtiDemo;

import java.io.Serializable;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;

public class Jd {

	public void name() {
		/* 建立一个Collection */
		 String[] strings = {"A", "B", "C","234234", "D","E"};
		 System.out.println(strings+"233453432423"); /* 依次输出“A”、“B”、“C”、“D” */
		 Collection listS = java.util.Arrays.asList(strings);
			/* 建立一个Collection */

		 String[] strings = {"A", "B", "C", "D"};
		 Collection list = java.util.Arrays.asList(strings);
     
		 
		
		 for (Object str : list) {
		     System.out.println(str);
		 }

			int beernum =99;
			String word = "bottle";
			while (beernum>0){
			if (beernum == 1){
			word = "bootle";
			}

			System.out.print(beernum+""+word+"of beer on the wall");
			System.out.println(beernum+""+"of beer");
			System.out.println("Take one down.");
			System.out.println("passit around.");
			beernum = beernum -1;

			   if (beernum>0); {
			     System.out.println(beernum+""+"of beer on wall");
			   }
			   {
			     System.out.println("No more bottles of beer on the wall");
			   }
    
		 
			}
			}
	 public void setKickoutAfter(boolean kickoutAfter) {
	        this.kickoutAfter = kickoutAfter;
	    }

	    public void setMaxSession(int maxSession) {
	        this.maxSession = maxSession;
	    }

	    public void setSessionManager(SessionManager sessionManager) {
	        this.sessionManager = sessionManager;
	    }

	    public void setCacheManager(CacheManager cacheManager) {
	        this.cache = cacheManager.getCache("shiro-kickout-session");
	    }

	    @Override
	    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
	        return false;
	    }

	    @Override
	    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
	        Subject subject = getSubject(request, response);
	        if(!subject.isAuthenticated() && !subject.isRemembered()) {
	            //如果没有登录，直接进行之后的流程
	            return true;
	        }

	        Session session = subject.getSession();
	        String username = (String) subject.getPrincipal();
	        Serializable sessionId = session.getId();

	        //TODO 同步控制
	        Deque<Serializable> deque = cache.get(username);
	        if(deque == null) {
	            deque = new LinkedList<Serializable>();
	            cache.put(username, deque);
	        }

	        //如果队列里没有此sessionId，且用户没有被踢出；放入队列
	        if(!deque.contains(sessionId) && session.getAttribute("kickout") == null) {
	            deque.push(sessionId);
	        }

	        //如果队列里的sessionId数超出最大会话数，开始踢人
	        while(deque.size() > maxSession) {
	            Serializable kickoutSessionId = null;
	            if(kickoutAfter) { //如果踢出后者
	                kickoutSessionId = deque.removeFirst();
	            } else { //否则踢出前者
	                kickoutSessionId = deque.removeLast();
	            }
	            try {
	                Session kickoutSession = sessionManager.getSession(new DefaultSessionKey(kickoutSessionId));
	                if(kickoutSession != null) {
	                    //设置会话的kickout属性表示踢出了
	                    kickoutSession.setAttribute("kickout", true);
	                }
	            } catch (Exception e) {//ignore exception
	            }
	        }

	        //如果被踢出了，直接退出，重定向到踢出后的地址
	        if (session.getAttribute("kickout") != null) {
	            //会话被踢出了
	            try {
	                subject.logout();
	            } catch (Exception e) { //ignore
	            }
	            saveRequest(request);
	            WebUtils.issueRedirect(request, response, kickoutUrl);
	            return false;
	        }

	        return true;
	    }
}


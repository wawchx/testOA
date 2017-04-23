/*
 * @(#)ServletDispatcherInfoResult.java 2008-12-5
 * 
 * ��Ϣ��˹���ϵͳ
 */

package com.duker.mygift.struts.result;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.ServletDispatcherResult;

import com.duker.mygift.vo.Position;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.util.TextParseUtil;
import com.opensymphony.xwork2.util.ValueStack;

/**
 * <pre>
 * ���������ʾ��תresult
 * ʹ�÷���
 * ��action������ָ��name= result&quot;resultInfo&quot; type=&quot;resultInfo&quot;,��������Ҫ��ת�Ĳ���
 * 	&lt;action name=&quot;addsightspot&quot; class=&quot;sightSpotAction&quot; method=&quot;addSightSpot&quot;&gt;
 * 		&lt;interceptor-ref name=&quot;actionError&quot; /&gt;
 * 		&lt;interceptor-ref name=&quot;params&quot; /&gt;
 * 		&lt;interceptor-ref name=&quot;scope&quot;&gt;
 * 			&lt;param name=&quot;session&quot;&gt;userName&lt;/param&gt;
 * 			&lt;param name=&quot;key&quot;&gt;login.&lt;/param&gt;
 * 		&lt;/interceptor-ref&gt;
 * 		&lt;result name=&quot;resultInfo&quot; type=&quot;resultInfo&quot;&gt;
 *   			&lt;param name=&quot;types[0]&quot;&gt;0&lt;/param&gt;
 *   			&lt;param name=&quot;names[0]&quot;&gt;�޸�&lt;/param&gt;
 *   			&lt;param name=&quot;urls[0]&quot;&gt;/sightspot/user/findsightspot.action?sightId=${sightSpot.sightId}&lt;/param&gt;	
 *   			&lt;param name=&quot;types[1]&quot;&gt;0&lt;/param&gt;
 *   			&lt;param name=&quot;names[1]&quot;&gt;�������&lt;/param&gt;
 *   			&lt;param name=&quot;urls[1]&quot;&gt;/sightspot/addsightspot.action&lt;/param&gt;	  				
 *   			&lt;param name=&quot;types[2]&quot;&gt;0&lt;/param&gt;
 *   			&lt;param name=&quot;names[2]&quot;&gt;�����б�&lt;/param&gt;
 *   			&lt;param name=&quot;urls[2]&quot;&gt;/sightspot/user/findsightspots.action&lt;/param&gt;
 * 		&lt;/result&gt;
 * 	&lt;/action&gt;
 *  
 * @author wangzh
 * 
 * @version 0.9
 * 
 * �޸İ汾: 0.9
 * �޸�����: 2008-12-5
 * �޸��� :  wangzh
 * �޸�˵��: �������
 * ������ ��
 * </pre>
 */
public class ServletDispatcherInfoResult extends ServletDispatcherResult {

	/**
	 * url����0:url��ַ 1:JavaScript����
	 */
	private List<Integer> types = new LinkedList<Integer>();

	/**
	 * ��ʾ��
	 */
	private List<String> names = new LinkedList<String>();

	/**
	 * ��ת��ַ
	 */
	private List<String> urls = new LinkedList<String>();

	/**
	 * �Ƿ�ˢ�¸�ҳ��
	 */
	private boolean refresh = false;

	/**
	 * �Ƿ񲻹ؼ���ʾ����
	 */
	private boolean notClose = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.struts2.dispatcher.StrutsResultSupport#execute(com.opensymphony
	 * .xwork2.ActionInvocation)
	 */
	@Override
	public void execute(ActionInvocation invocation) throws Exception {
		if (types != null && names != null && urls != null) {
			List<Position> positions = new ArrayList<Position>(urls.size());
			Iterator<Integer> typesIt = types.iterator();
			Iterator<String> namesIt = names.iterator();
			Iterator<String> urlsIt = urls.iterator();
			ValueStack stack = invocation.getStack();

			while (typesIt.hasNext() && namesIt.hasNext() && urlsIt.hasNext()) {
				String name = namesIt.next();
				String url = urlsIt.next();
				name = TextParseUtil.translateVariables('$', name, stack);
				url = TextParseUtil.translateVariables('$', url, stack);
				positions.add(new Position(typesIt.next(), name, url));
			}

			HttpServletRequest request = ServletActionContext.getRequest();
			request.setAttribute("positions", positions);
			request.setAttribute("refresh", refresh);
			request.setAttribute("notClose", notClose);
		}

		super.execute(invocation);
	}

	public List<String> getNames() {
		return names;
	}

	public void setNames(List<String> names) {
		this.names = names;
	}

	public List<Integer> getTypes() {
		return types;
	}

	public void setTypes(List<Integer> types) {
		this.types = types;
	}

	public List<String> getUrls() {
		return urls;
	}

	public void setUrls(List<String> urls) {
		this.urls = urls;
	}

	public boolean isRefresh() {
		return refresh;
	}

	public void setRefresh(boolean refresh) {
		this.refresh = refresh;
	}

	public boolean isNotClose() {
		return notClose;
	}

	public void setNotClose(boolean notClose) {
		this.notClose = notClose;
	}

}

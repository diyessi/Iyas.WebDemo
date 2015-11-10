package qa.qcri.qf.cQAdemo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Servlet Filter implementation class CORSFilter
 * This filter helps make it possible to use the demo servlet in a mash-up from another web site 
 */
@WebFilter(urlPatterns="/*")
public class CORSFilter implements Filter {

    /**
     * Default constructor. 
     */
    public CORSFilter() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	void setHeaders(HttpServletResponse httpResponse, HashMap<String, String> headers){
		for(Map.Entry<String,String> mapEntry : headers.entrySet()){
			httpResponse.setHeader(mapEntry.getKey(), mapEntry.getValue());
		}
	}
	
	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (response instanceof HttpServletResponse 
				&& request instanceof HttpServletRequest){
			HttpServletRequest httpRequest = (HttpServletRequest)request;
			/*
			Enumeration<String> headers = (Enumeration<String>)httpRequest.getHeaderNames();
			while(headers.hasMoreElements()){
				String header = headers.nextElement();
				Enumeration<String> values = httpRequest.getHeaders(header);
				while(values.hasMoreElements()){
					String value = values.nextElement();
					System.err.format("  %s:%s%n", header, value);
				}
			}
			*/
			final String origin = httpRequest.getHeader("Origin");
			if (null != origin){
				final HashMap<String, String> headers = new HashMap<String, String>();
				headers.put("Access-Control-Allow-Origin", origin);
				headers.put("Access-Control-Allow-Credentials", "true");
				if ("OPTIONS".equals(httpRequest.getMethod())){
					headers.put("Access-Control-Allow-Methods", "GET,POST,PUT");
					headers.put("Access-Control-Max-Age", "1728000");
					String allowHeaders = httpRequest.getHeader("Access-Control-Request-Headers");
					if (null != allowHeaders)
						headers.put("Access-Control-Allow-Headers", allowHeaders);
				}

				HttpServletResponse httpResponse = (HttpServletResponse)response;
				setHeaders(httpResponse, headers);
				HttpServletResponse wrappedResponse = new HttpServletResponseWrapper(httpResponse){
					public void reset(){
						super.reset();
						/* DefaultServlet resets, so restore the CORS headers before
						 * it adds the content 
						 */
						setHeaders(this, headers);
					}
				};
				chain.doFilter(request, wrappedResponse);
				return;
			}
		}
		chain.doFilter(request, response);
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

}

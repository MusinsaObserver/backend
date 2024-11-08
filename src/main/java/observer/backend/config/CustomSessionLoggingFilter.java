package observer.backend.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

public class CustomSessionLoggingFilter implements Filter {
	private static final Logger logger = LoggerFactory.getLogger(CustomSessionLoggingFilter.class);

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
		throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		if (httpRequest.getCookies() != null) {
			for (var cookie : httpRequest.getCookies()) {
				if ("JSESSIONID".equals(cookie.getName())) {
					logger.debug("Found JSESSIONID Cookie: {}", cookie.getValue());
				}
			}
		} else {
			logger.debug("No cookies found in request");
		}
		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {}

	@Override
	public void destroy() {}
}

package com.sap.espm.model.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.xsa.security.container.XSUserInfo;


@ServletSecurity(@HttpConstraint(rolesAllowed = { "Create" }))

public class EspmSecureServiceFactoryFilter implements Filter {

	/**
	 * {@link Logger} implementation for logging.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(EspmServiceFactoryFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		try {
			if (request instanceof HttpServletRequest) {
				HttpServletRequest oCntxt = (HttpServletRequest) request;
				String url = oCntxt.getRequestURI().toString();
				
				if(url.contains("/secure/")) {
					
					XSUserInfo userInfo = (XSUserInfo) oCntxt.getUserPrincipal();
					
					if(userInfo.checkLocalScope("Create")) {
						LOGGER.info("Role success ");
						chain.doFilter(request, response);
					}
					else {
						LOGGER.info("Role not success");
						
						HttpServletResponse httpResp = (HttpServletResponse) response;
						httpResp.sendError(HttpServletResponse.SC_UNAUTHORIZED,
								"Access denied to the secure entity, user need to have 'Retailer' role");
					}
				}
				else {
					chain.doFilter(request, response);
				}
				
			}

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}

	}

	@Override
	public void destroy() {

	}

}
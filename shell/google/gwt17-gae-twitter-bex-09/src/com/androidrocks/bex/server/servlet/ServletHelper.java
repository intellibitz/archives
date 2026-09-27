/**
 *
 */
package com.androidrocks.bex.server.servlet;

import com.androidrocks.bex.client.json.RequestFailure;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author muthu
 */
public final class ServletHelper {

    private static final Logger log = Logger.getLogger(ServletHelper.class
            .getName());
    static final String EMPTY = "";

    public static void onRequestFailure(HttpServletRequest req, HttpServletResponse resp, Exception e) throws IOException {
        RequestFailure fail = new RequestFailure();
        fail.setReason("Exception: "+e.getMessage());
        req.getSession().setAttribute("requestFailure", fail);
        resp.sendRedirect("/_APP_ERROR");
    }
}
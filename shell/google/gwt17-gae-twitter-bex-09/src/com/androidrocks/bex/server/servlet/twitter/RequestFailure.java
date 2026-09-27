/**
 *
 */
package com.androidrocks.bex.server.servlet.twitter;

import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author muthu
 */
public class RequestFailure extends HttpServlet {

    private static final Logger log = Logger
            .getLogger(RequestFailure.class.getName());

    /*
      * (non-Javadoc)
      *
      * @see
      * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest
      * , javax.servlet.http.HttpServletResponse)
      */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO Auto-generated method stub
        doPost(req, resp);
    }

    /*
      * (non-Javadoc)
      *
      * @see
      * javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest
      * , javax.servlet.http.HttpServletResponse)
      */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        com.androidrocks.bex.client.json.RequestFailure fail = (com.androidrocks.bex.client.json.RequestFailure) req.getSession().getAttribute(
                "requestFailure");
        Gson gson = new Gson();
        resp.getWriter().print(gson.toJson(fail));
    }

}

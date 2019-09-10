package com.mizo0203.hoshiguma;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

public class LogServlet extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(LogServlet.class.getName());

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    LOG.info(req.getReader().readLine());
  }
}

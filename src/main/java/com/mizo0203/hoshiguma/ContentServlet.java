package com.mizo0203.hoshiguma;

import com.google.gson.Gson;
import com.mizo0203.hoshiguma.domain.UseCase;
import com.mizo0203.hoshiguma.repo.liff.data.CandidateDates;
import com.mizo0203.hoshiguma.repo.liff.data.GroupId;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class ContentServlet extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(ContentServlet.class.getName());

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try (UseCase useCase = new UseCase()) {
      GroupId groupId = new Gson().fromJson(req.getReader(), GroupId.class);
      LOG.info("groupId: " + groupId.groupId);
      String[] candidateDates = useCase.getCandidateDates(groupId.groupId);
      resp.setContentType("application/json");
      resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
      String x = new Gson().toJson(new CandidateDates(candidateDates));
      LOG.info("resp: " + x);
      resp.getWriter().println(x);
    }
  }
}

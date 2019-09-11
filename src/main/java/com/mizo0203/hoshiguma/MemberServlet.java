package com.mizo0203.hoshiguma;

import com.mizo0203.hoshiguma.domain.UseCase;
import com.mizo0203.hoshiguma.repo.liff.data.Member;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class MemberServlet extends HttpServlet {

  private static final Logger LOG = Logger.getLogger(MemberServlet.class.getName());

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) {
    String groupId = req.getParameter("groupId");
    String userId = req.getParameter("userId");
    String displayName = req.getParameter("displayName");
    Map<Long, Member.Answer> answer = new HashMap<>();
    LOG.info("groupId: " + groupId);
    LOG.info("displayName: " + displayName);
    for (int i = 0; i < 10; i++) {
      Member.Answer a = Member.Answer.fromString(req.getParameter("c" + i));
      if (a != null) {
        answer.put(Long.valueOf(req.getParameter("d" + i)), a);
      }
    }
    LOG.info("answer: " + answer);
    try (UseCase useCase = new UseCase()) {
      useCase.submitAnswer(groupId, userId, displayName, answer);
    }
  }
}

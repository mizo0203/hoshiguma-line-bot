/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mizo0203.hoshiguma.push_task;

import com.mizo0203.hoshiguma.repo.Repository;
import com.mizo0203.hoshiguma.repo.line.messaging.data.MessageObject;
import com.mizo0203.hoshiguma.repo.line.messaging.data.TextMessageObject;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Logger;

public class ReminderTaskServlet extends HttpServlet {
  public static final String PARAM_NAME_SOURCE_ID = "param_name_source_id";
  private static final Logger LOG = Logger.getLogger(ReminderTaskServlet.class.getName());
  private Repository mRepository;

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
    mRepository = new Repository();
    try {
      String source_id = req.getParameter(PARAM_NAME_SOURCE_ID);
      LOG.info("ReminderTaskServlet is processing " + source_id);
      MessageObject[] messages = new MessageObject[1];
      messages[0] = new TextMessageObject("リマインダーだ！\nもし、出欠入力がまだなら入力してくれ！");
      mRepository.pushMessage(source_id, messages);
    } finally {
      mRepository.destroy();
    }
  }
}

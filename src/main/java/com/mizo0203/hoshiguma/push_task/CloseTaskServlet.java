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

import com.mizo0203.hoshiguma.domain.UseCase;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Logger;

public class CloseTaskServlet extends HttpServlet {
  public static final String PARAM_NAME_SOURCE_ID = "param_name_source_id";
  private static final Logger LOG = Logger.getLogger(CloseTaskServlet.class.getName());
  private UseCase mUseCase;

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
    mUseCase = new UseCase();
    try {
      String source_id = req.getParameter(PARAM_NAME_SOURCE_ID);
      LOG.info("CloseTaskServlet is processing " + source_id);
      mUseCase.closeCandidateDates(source_id);
    } finally {
      mUseCase.close();
    }
  }
}

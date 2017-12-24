package com.mizo0203.hoshiguma.util;

import com.google.gson.Gson;
import com.mizo0203.hoshiguma.repo.line.messaging.data.ReplyMessageData;
import com.mizo0203.hoshiguma.repo.line.messaging.data.RequestBody;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class PaserUtil {

  /**
   * @see Gson#fromJson(String, Class)
   */
  public static RequestBody parseWebhooksData(String json) {
    return new Gson().fromJson(json, RequestBody.class);
  }

  /**
   * @see Gson#fromJson(String, Class)
   */
  public static String toJson(ReplyMessageData data) {
    return new Gson().toJson(data, ReplyMessageData.class);
  }

  private static String parseString(BufferedReader br) throws IOException {
    StringBuilder sb = new StringBuilder();
    String line;
    while ((line = br.readLine()) != null) {
      sb.append(line);
    }
    br.close();
    return sb.toString();
  }

  public static String parseString(InputStream is) throws IOException {
    InputStreamReader isr = new InputStreamReader(is,
        StandardCharsets.UTF_8);
    return parseString(new BufferedReader(isr));
  }
}

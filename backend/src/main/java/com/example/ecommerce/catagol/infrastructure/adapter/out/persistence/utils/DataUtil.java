package com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.utils;

import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class DataUtil {

  private DataUtil() {

  }

  public static String getTextSure(String text) {
    if(text == null || StringUtils.isBlank(text)) {
      return null;
    }

    return text.trim();
  }

  public static Instant getInstantFromLocalDateTime(LocalDateTime localDateTime) {
    return localDateTime != null ? localDateTime.atZone(ZoneId.systemDefault()).toInstant()
      : null;
  }

  public static LocalDateTime getLocalDateTimeFromInstant(Instant instant) {
    return instant != null ? LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
      : null;
  }


}

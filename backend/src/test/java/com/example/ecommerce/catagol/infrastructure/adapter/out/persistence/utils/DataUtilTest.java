package com.example.ecommerce.catagol.infrastructure.adapter.out.persistence.utils;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DataUtilTest {

  @Test
  void returnsNullWhenTextIsNull() {
    assertNull(DataUtil.getTextSure(null));
  }

  @Test
  void returnsNullWhenTextIsEmpty() {
    assertNull(DataUtil.getTextSure(""));
  }

  @Test
  void returnsNullWhenTextContainsOnlyWhitespace() {
    assertNull(DataUtil.getTextSure(" \t\n  "));
  }

  @Test
  void trimsLeadingAndTrailingWhitespaceFromText() {
    assertEquals("valid text", DataUtil.getTextSure(" \tvalid text \n"));
  }

  @Test
  void preservesWhitespaceInsideNonBlankText() {
    assertEquals("valid  text", DataUtil.getTextSure("  valid  text  "));
  }

  @Test
  void convertsLocalDateTimeToInstantUsingSystemDefaultZone() {
    LocalDateTime localDateTime = LocalDateTime.of(
      2026, 10, 14, 8, 30, 15, 123_456_789);

    Instant instant = DataUtil.getInstantFromLocalDateTime(localDateTime);

    assertEquals(localDateTime.atZone(ZoneId.systemDefault()).toInstant(), instant);
  }

  @Test
  void returnsNullWhenConvertingNullLocalDateTimeToInstant() {
    assertNull(DataUtil.getInstantFromLocalDateTime(null));
  }

  @Test
  void convertsInstantToLocalDateTimeUsingSystemDefaultZone() {
    Instant instant = Instant.parse("2026-10-14T14:30:15.123456789Z");

    LocalDateTime localDateTime = DataUtil.getLocalDateTimeFromInstant(instant);

    assertEquals(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()), localDateTime);
  }

  @Test
  void returnsNullWhenConvertingNullInstantToLocalDateTime() {
    assertNull(DataUtil.getLocalDateTimeFromInstant(null));
  }

  @Test
  void preservesInstantWhenConvertingBetweenDateTimeTypes() {
    Instant instant = Instant.parse("2026-10-14T14:30:15.123456789Z");

    LocalDateTime localDateTime = DataUtil.getLocalDateTimeFromInstant(instant);
    Instant convertedInstant = DataUtil.getInstantFromLocalDateTime(localDateTime);

    assertEquals(instant, convertedInstant);
  }


}

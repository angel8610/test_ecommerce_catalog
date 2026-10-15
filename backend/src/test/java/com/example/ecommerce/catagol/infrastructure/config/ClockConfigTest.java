package com.example.ecommerce.catagol.infrastructure.config;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ClockConfigTest {

  @Test
  void createsClockUsingSystemDefaultZoneAndCurrentTime() {
    Clock clock = new ClockConfig().clock();
    Instant beforeClockRead = Instant.now();

    Instant clockTime = clock.instant();
    Instant afterClockRead = Instant.now();

    assertEquals(ZoneId.systemDefault(), clock.getZone());
    assertFalse(clockTime.isBefore(beforeClockRead));
    assertFalse(clockTime.isAfter(afterClockRead));
  }


}

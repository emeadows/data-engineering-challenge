package com.mogtech.challange.model

import java.time.{ Instant, LocalDateTime, ZoneOffset }
import java.time.format.DateTimeFormatter
import java.time.temporal.{ TemporalField, WeekFields }
import java.util.Locale

trait DateHelper {

  implicit class LongToDate(ts: Long) extends DateHelper {

    def asIsoDateTime: String = {
      val ldt = LocalDateTime.ofInstant(Instant.ofEpochSecond(ts), ZoneOffset.UTC)
      ldt.beginningOfMinute.format(DateTimeFormatter.ISO_DATE_TIME)
    }
  }

  val temporalField: TemporalField = WeekFields.of(Locale.getDefault()).dayOfWeek()

  implicit class LocalDateTimeOps(ldt: LocalDateTime) {
    def isoFormatted: String = ldt.format(DateTimeFormatter.ISO_DATE_TIME)

    def beginningOfMinute:   LocalDateTime = ldt.withSecond(0)
    def beginningOfTheHour:  LocalDateTime = ldt.beginningOfMinute.withMinute(0)
    def beginningOfDay:      LocalDateTime = ldt.beginningOfTheHour.withHour(0)
    def beginningOfTheWeek:  LocalDateTime = ldt.beginningOfDay.`with`(temporalField, 1)
    def beginningOfTheMonth: LocalDateTime = ldt.beginningOfDay.withDayOfMonth(1)
    def beginningOfTheYear:  LocalDateTime = ldt.beginningOfTheMonth.withMonth(1)
  }

}

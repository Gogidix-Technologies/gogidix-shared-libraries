package com.exalt.ecosystem.shared.utilities;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.TimeZone;

/**
 * Comprehensive date and time utility class for the Exalt Social E-commerce Ecosystem.
 * Provides advanced date manipulation, formatting, and calculation methods with timezone support.
 * 
 * <p>This utility class handles common date operations across the ecosystem including:
 * business day calculations, timezone conversions, date parsing, and formatting operations.</p>
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
public final class DateUtils {
    
    // Common date formatters
    public static final DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    public static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    public static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter DISPLAY_DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    public static final DateTimeFormatter API_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    public static final DateTimeFormatter FILE_SAFE_DATE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    // Common timezone constants
    public static final ZoneId UTC = ZoneId.of("UTC");
    public static final ZoneId LONDON = ZoneId.of("Europe/London");
    public static final ZoneId NEW_YORK = ZoneId.of("America/New_York");
    public static final ZoneId DUBAI = ZoneId.of("Asia/Dubai");
    public static final ZoneId SINGAPORE = ZoneId.of("Asia/Singapore");
    
    private DateUtils() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Gets the current timestamp in UTC.
     * 
     * @return current UTC timestamp
     */
    public static LocalDateTime nowUtc() {
        return LocalDateTime.now(UTC);
    }
    
    /**
     * Gets the current timestamp in the specified timezone.
     * 
     * @param zoneId timezone
     * @return current timestamp in the specified timezone
     */
    public static LocalDateTime now(ZoneId zoneId) {
        return LocalDateTime.now(zoneId);
    }
    
    /**
     * Converts a LocalDateTime from one timezone to another.
     * 
     * @param dateTime the datetime to convert
     * @param fromZone source timezone
     * @param toZone target timezone
     * @return converted datetime
     */
    public static LocalDateTime convertTimezone(LocalDateTime dateTime, ZoneId fromZone, ZoneId toZone) {
        if (dateTime == null) return null;
        return dateTime.atZone(fromZone).withZoneSameInstant(toZone).toLocalDateTime();
    }
    
    /**
     * Converts a LocalDateTime to UTC.
     * 
     * @param dateTime the datetime to convert
     * @param fromZone source timezone
     * @return datetime in UTC
     */
    public static LocalDateTime toUtc(LocalDateTime dateTime, ZoneId fromZone) {
        return convertTimezone(dateTime, fromZone, UTC);
    }
    
    /**
     * Converts a UTC LocalDateTime to the specified timezone.
     * 
     * @param utcDateTime the UTC datetime
     * @param toZone target timezone
     * @return datetime in the target timezone
     */
    public static LocalDateTime fromUtc(LocalDateTime utcDateTime, ZoneId toZone) {
        return convertTimezone(utcDateTime, UTC, toZone);
    }
    
    /**
     * Formats a LocalDateTime using the specified pattern.
     * 
     * @param dateTime datetime to format
     * @param pattern format pattern
     * @return formatted date string
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) return null;
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }
    
    /**
     * Formats a LocalDateTime for display purposes.
     * 
     * @param dateTime datetime to format
     * @return formatted display string (dd/MM/yyyy HH:mm:ss)
     */
    public static String formatForDisplay(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DISPLAY_DATE_TIME) : null;
    }
    
    /**
     * Formats a LocalDate for display purposes.
     * 
     * @param date date to format
     * @return formatted display string (dd/MM/yyyy)
     */
    public static String formatForDisplay(LocalDate date) {
        return date != null ? date.format(DISPLAY_DATE) : null;
    }
    
    /**
     * Formats a LocalDateTime for API responses.
     * 
     * @param dateTime datetime to format
     * @return formatted API string (yyyy-MM-dd'T'HH:mm:ss)
     */
    public static String formatForApi(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(API_DATE_TIME) : null;
    }
    
    /**
     * Formats a LocalDateTime for file names (safe characters only).
     * 
     * @param dateTime datetime to format
     * @return formatted file-safe string (yyyyMMdd_HHmmss)
     */
    public static String formatForFileName(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(FILE_SAFE_DATE_TIME) : null;
    }
    
    /**
     * Parses a date string using multiple common formats.
     * 
     * @param dateString date string to parse
     * @return parsed LocalDateTime or null if parsing fails
     */
    public static LocalDateTime parseFlexible(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        
        String trimmed = dateString.trim();
        
        // Try common formats in order of likelihood
        DateTimeFormatter[] formatters = {
            API_DATE_TIME,
            ISO_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        };
        
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDateTime.parse(trimmed, formatter);
            } catch (DateTimeParseException e) {
                // Try next formatter
            }
        }
        
        // Try parsing as date only and set time to start of day
        try {
            LocalDate date = LocalDate.parse(trimmed, ISO_DATE);
            return date.atStartOfDay();
        } catch (DateTimeParseException e) {
            // Try dd/MM/yyyy format
            try {
                LocalDate date = LocalDate.parse(trimmed, DISPLAY_DATE);
                return date.atStartOfDay();
            } catch (DateTimeParseException ex) {
                return null;
            }
        }
    }
    
    /**
     * Parses a date string using the specified format.
     * 
     * @param dateString date string to parse
     * @param pattern format pattern
     * @return parsed LocalDateTime or null if parsing fails
     */
    public static LocalDateTime parse(String dateString, String pattern) {
        if (dateString == null || pattern == null) return null;
        try {
            return LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern(pattern));
        } catch (DateTimeParseException e) {
            return null;
        }
    }
    
    /**
     * Calculates the number of business days between two dates.
     * Business days are Monday through Friday, excluding weekends.
     * 
     * @param startDate start date (inclusive)
     * @param endDate end date (exclusive)
     * @return number of business days
     */
    public static long calculateBusinessDays(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) return 0;
        if (startDate.isAfter(endDate)) return 0;
        
        long businessDays = 0;
        LocalDate current = startDate;
        
        while (current.isBefore(endDate)) {
            DayOfWeek dayOfWeek = current.getDayOfWeek();
            if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                businessDays++;
            }
            current = current.plusDays(1);
        }
        
        return businessDays;
    }
    
    /**
     * Adds business days to a date, skipping weekends.
     * 
     * @param startDate starting date
     * @param businessDaysToAdd number of business days to add
     * @return new date after adding business days
     */
    public static LocalDate addBusinessDays(LocalDate startDate, int businessDaysToAdd) {
        if (startDate == null) return null;
        if (businessDaysToAdd == 0) return startDate;
        
        LocalDate result = startDate;
        int daysAdded = 0;
        int direction = businessDaysToAdd > 0 ? 1 : -1;
        int targetDays = Math.abs(businessDaysToAdd);
        
        while (daysAdded < targetDays) {
            result = result.plusDays(direction);
            DayOfWeek dayOfWeek = result.getDayOfWeek();
            if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                daysAdded++;
            }
        }
        
        return result;
    }
    
    /**
     * Checks if a date falls on a weekend.
     * 
     * @param date date to check
     * @return true if the date is Saturday or Sunday
     */
    public static boolean isWeekend(LocalDate date) {
        if (date == null) return false;
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
    
    /**
     * Checks if a date falls on a business day (Monday-Friday).
     * 
     * @param date date to check
     * @return true if the date is a business day
     */
    public static boolean isBusinessDay(LocalDate date) {
        return !isWeekend(date);
    }
    
    /**
     * Gets the start of the day for a LocalDateTime.
     * 
     * @param dateTime input datetime
     * @return datetime at start of day (00:00:00)
     */
    public static LocalDateTime startOfDay(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.toLocalDate().atStartOfDay() : null;
    }
    
    /**
     * Gets the end of the day for a LocalDateTime.
     * 
     * @param dateTime input datetime
     * @return datetime at end of day (23:59:59.999999999)
     */
    public static LocalDateTime endOfDay(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.toLocalDate().atTime(LocalTime.MAX) : null;
    }
    
    /**
     * Gets the start of the week (Monday) for a given date.
     * 
     * @param date input date
     * @return start of week (Monday)
     */
    public static LocalDate startOfWeek(LocalDate date) {
        return date != null ? date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) : null;
    }
    
    /**
     * Gets the end of the week (Sunday) for a given date.
     * 
     * @param date input date
     * @return end of week (Sunday)
     */
    public static LocalDate endOfWeek(LocalDate date) {
        return date != null ? date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)) : null;
    }
    
    /**
     * Gets the start of the month for a given date.
     * 
     * @param date input date
     * @return first day of the month
     */
    public static LocalDate startOfMonth(LocalDate date) {
        return date != null ? date.with(TemporalAdjusters.firstDayOfMonth()) : null;
    }
    
    /**
     * Gets the end of the month for a given date.
     * 
     * @param date input date
     * @return last day of the month
     */
    public static LocalDate endOfMonth(LocalDate date) {
        return date != null ? date.with(TemporalAdjusters.lastDayOfMonth()) : null;
    }
    
    /**
     * Gets the start of the year for a given date.
     * 
     * @param date input date
     * @return first day of the year
     */
    public static LocalDate startOfYear(LocalDate date) {
        return date != null ? date.with(TemporalAdjusters.firstDayOfYear()) : null;
    }
    
    /**
     * Gets the end of the year for a given date.
     * 
     * @param date input date
     * @return last day of the year
     */
    public static LocalDate endOfYear(LocalDate date) {
        return date != null ? date.with(TemporalAdjusters.lastDayOfYear()) : null;
    }
    
    /**
     * Calculates the age in years from a birth date.
     * 
     * @param birthDate birth date
     * @return age in years
     */
    public static int calculateAge(LocalDate birthDate) {
        return calculateAge(birthDate, LocalDate.now());
    }
    
    /**
     * Calculates the age in years from a birth date to a reference date.
     * 
     * @param birthDate birth date
     * @param referenceDate reference date for age calculation
     * @return age in years
     */
    public static int calculateAge(LocalDate birthDate, LocalDate referenceDate) {
        if (birthDate == null || referenceDate == null) return 0;
        if (birthDate.isAfter(referenceDate)) return 0;
        return Period.between(birthDate, referenceDate).getYears();
    }
    
    /**
     * Calculates the difference between two dates in various units.
     * 
     * @param startDate start date
     * @param endDate end date
     * @param unit time unit (DAYS, HOURS, MINUTES, etc.)
     * @return difference in the specified unit
     */
    public static long calculateDifference(LocalDateTime startDate, LocalDateTime endDate, ChronoUnit unit) {
        if (startDate == null || endDate == null) return 0;
        return unit.between(startDate, endDate);
    }
    
    /**
     * Checks if a date is in the past.
     * 
     * @param date date to check
     * @return true if the date is before today
     */
    public static boolean isPast(LocalDate date) {
        return date != null && date.isBefore(LocalDate.now());
    }
    
    /**
     * Checks if a date is in the future.
     * 
     * @param date date to check
     * @return true if the date is after today
     */
    public static boolean isFuture(LocalDate date) {
        return date != null && date.isAfter(LocalDate.now());
    }
    
    /**
     * Checks if a date is today.
     * 
     * @param date date to check
     * @return true if the date is today
     */
    public static boolean isToday(LocalDate date) {
        return date != null && date.equals(LocalDate.now());
    }
    
    /**
     * Gets the quarter of the year for a given date.
     * 
     * @param date input date
     * @return quarter (1, 2, 3, or 4)
     */
    public static int getQuarter(LocalDate date) {
        if (date == null) return 0;
        return (date.getMonthValue() - 1) / 3 + 1;
    }
    
    /**
     * Gets the week number of the year for a given date.
     * 
     * @param date input date
     * @return week number (1-53)
     */
    public static int getWeekOfYear(LocalDate date) {
        if (date == null) return 0;
        return date.get(ChronoField.ALIGNED_WEEK_OF_YEAR);
    }
    
    /**
     * Converts LocalDateTime to legacy Date object.
     * 
     * @param localDateTime modern date time
     * @return legacy Date object
     */
    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
    
    /**
     * Converts LocalDate to legacy Date object.
     * 
     * @param localDate modern date
     * @return legacy Date object at start of day
     */
    public static Date toDate(LocalDate localDate) {
        if (localDate == null) return null;
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
    
    /**
     * Converts legacy Date to LocalDateTime.
     * 
     * @param date legacy date
     * @return modern LocalDateTime
     */
    public static LocalDateTime fromDate(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
    
    /**
     * Converts legacy Date to LocalDate.
     * 
     * @param date legacy date
     * @return modern LocalDate
     */
    public static LocalDate fromDateToLocalDate(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
    
    /**
     * Generates a list of dates between two dates (inclusive).
     * 
     * @param startDate start date
     * @param endDate end date
     * @return list of dates between start and end (inclusive)
     */
    public static List<LocalDate> getDateRange(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dates = new ArrayList<>();
        if (startDate == null || endDate == null) return dates;
        
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            dates.add(current);
            current = current.plusDays(1);
        }
        
        return dates;
    }
    
    /**
     * Generates a list of business dates between two dates (inclusive).
     * 
     * @param startDate start date
     * @param endDate end date
     * @return list of business dates between start and end (inclusive)
     */
    public static List<LocalDate> getBusinessDateRange(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dates = new ArrayList<>();
        if (startDate == null || endDate == null) return dates;
        
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (isBusinessDay(current)) {
                dates.add(current);
            }
            current = current.plusDays(1);
        }
        
        return dates;
    }
    
    /**
     * Truncates a LocalDateTime to the specified unit.
     * 
     * @param dateTime datetime to truncate
     * @param unit unit to truncate to (DAYS, HOURS, MINUTES, etc.)
     * @return truncated datetime
     */
    public static LocalDateTime truncate(LocalDateTime dateTime, ChronoUnit unit) {
        if (dateTime == null) return null;
        return dateTime.truncatedTo(unit);
    }
    
    /**
     * Gets the elapsed time in a human-readable format.
     * 
     * @param startTime start time
     * @param endTime end time
     * @return human-readable elapsed time (e.g., "2 hours 30 minutes")
     */
    public static String getElapsedTimeFormatted(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) return "Unknown";
        
        Duration duration = Duration.between(startTime, endTime);
        long totalSeconds = Math.abs(duration.getSeconds());
        
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        
        StringBuilder result = new StringBuilder();
        
        if (days > 0) {
            result.append(days).append(days == 1 ? " day" : " days");
        }
        if (hours > 0) {
            if (result.length() > 0) result.append(" ");
            result.append(hours).append(hours == 1 ? " hour" : " hours");
        }
        if (minutes > 0) {
            if (result.length() > 0) result.append(" ");
            result.append(minutes).append(minutes == 1 ? " minute" : " minutes");
        }
        if (seconds > 0 && days == 0 && hours == 0) {
            if (result.length() > 0) result.append(" ");
            result.append(seconds).append(seconds == 1 ? " second" : " seconds");
        }
        
        return result.length() > 0 ? result.toString() : "0 seconds";
    }
    
    /**
     * Checks if two date ranges overlap.
     * 
     * @param start1 start of first range
     * @param end1 end of first range
     * @param start2 start of second range
     * @param end2 end of second range
     * @return true if ranges overlap
     */
    public static boolean isDateRangeOverlapping(LocalDate start1, LocalDate end1, 
                                               LocalDate start2, LocalDate end2) {
        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            return false;
        }
        return start1.isBefore(end2.plusDays(1)) && start2.isBefore(end1.plusDays(1));
    }
    
    /**
     * Gets the time zone offset for a given zone at a specific date.
     * 
     * @param date the date
     * @param zoneId the timezone
     * @return offset in hours
     */
    public static int getTimezoneOffset(LocalDate date, ZoneId zoneId) {
        if (date == null || zoneId == null) return 0;
        ZoneOffset offset = date.atStartOfDay(zoneId).getOffset();
        return offset.getTotalSeconds() / 3600;
    }
}
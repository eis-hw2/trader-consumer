package com.example.taskconsumer.Util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtil {
    public final static String TOMMOROW_OPEN = "TM_OP";
    public final static String TOMMOROW_CLOSE = "TM_CLOSE";

    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    public static SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static long getMillisInterval(Calendar startTime, Calendar endTime){
        return endTime.getTimeInMillis() - startTime.getTimeInMillis();
    }

    public static int getMinuteInterval(Calendar startTime, Calendar endTime){
        return (int) (getMillisInterval(startTime, endTime) / 60000);
    }

    public static String calendarToString(Calendar calendar, SimpleDateFormat format){
        return format.format(calendar.getTime());
    }

    public static Calendar stringToCalendar(String str, SimpleDateFormat format) throws ParseException{
        if (str.equals(TOMMOROW_OPEN))
            return getTomorrowOpenTime();
        if (str.equals(TOMMOROW_CLOSE))
            return getTomorrowCloseTime();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(format.parse(str));
        return calendar;
    }

    public static final Calendar getTomorrow(){
        Calendar calendar = Calendar.getInstance();
        // after 5 min
        //calendar.add(Calendar.MINUTE, 5);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        return calendar;
    }

    public static final Calendar getTomorrowOpenTime(){
        Calendar tm = getTomorrow();
        tm.set(Calendar.HOUR_OF_DAY, 9);
        return tm;
    }

    public static final Calendar getTomorrowCloseTime(){
        Calendar tm = getTomorrow();
        tm.set(Calendar.HOUR_OF_DAY, 3);
        return tm;
    }

    public static void main(String[] args){
        try {

            Calendar c = stringToCalendar("2019-05-29 20:00:00", datetimeFormat);
            System.out.println(datetimeFormat.format(c.getTime()));

        }
        catch (Exception e){
            System.out.println("error");
        }
    }
}

package br.com.previsoescomgps;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class Weather {
    public final String dayOfWeek;
    public final String minTemp;
    public final String maxTemp;
    public final String humidity;
    public final String description;
    public final String iconURL;

    public Weather (long dt, double minTemp, double maxTemp,
                    double humidity, String description,
                    String iconName){
        NumberFormat numberFormat =
                NumberFormat.getNumberInstance();
        numberFormat.setMaximumFractionDigits(0);
        NumberFormat percentFormat =
                NumberFormat.getPercentInstance();
        this.minTemp = numberFormat.format(minTemp);
        this.maxTemp = numberFormat.format(maxTemp);
        this.humidity = percentFormat.format(humidity);
        this.description = description;
        this.iconURL = "http://openweathermap.org/img/w/" + iconName + ".png";
        this.dayOfWeek = convert(dt);
    }

    private String convert (long dt){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dt * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE HH:mm");
        TimeZone tz = TimeZone.getDefault();
        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
        return sdf.format(calendar.getTime());
    }
}
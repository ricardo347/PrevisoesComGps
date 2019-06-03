package br.com.previsoescomgps;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
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



   /* {
        "dt":1559530800,
            "main":{"temp":21.82,
                    "temp_min":21.77,
                    "temp_max":21.82,"pressure":1013.79,"sea_level":1013.79,"grnd_level":1013.72,
                    "humidity":64,"temp_kf":0.05},
            "weather":[
                    {"id":800,
                            "main":"Clear"
                            ,"description":"clear sky"
                            ,"icon":"01n"}],
            "clouds":{"all":0},
            "wind":{"speed":2.51,"deg":31.603},
            "sys":{"pod":"n"},
            "dt_txt":"2019-06-03 03:00:00"
    }
*/


}
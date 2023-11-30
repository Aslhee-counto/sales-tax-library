package com.xion.data;

import com.xion.exceptions.GstException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateRange {

    private Date start;
    private Date end;

    public static DateRange parsePeriod(String period) throws GstException {
        String[] split = period.split("_");
        if (split.length!=2) throw new GstException("period is incorrectly formatted: " + period);

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {

            DateRange range = new DateRange();
            range.setStart(format.parse(split[0]));
            range.setEnd(format.parse(split[1]));

            if(range.getStart().after(range.getEnd()))
                throw new GstException("start date is after end date in period: " + period);
            return range;

        }catch (ParseException e) {
            throw new GstException(e);
        }
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

}

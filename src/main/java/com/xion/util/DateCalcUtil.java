package com.xion.util;

import java.util.LinkedList;
import java.util.List;

public class DateCalcUtil {

    private static final List<List<Integer>> matrix;

    static {
        List<Integer> janList = new LinkedList<>();
        List<Integer> febList = new LinkedList<>();
        List<Integer> marList = new LinkedList<>();
        List<Integer> aprList = new LinkedList<>();
        janList = new LinkedList<>();
        janList.add(0);
        janList.add(3);
        janList.add(6);
        janList.add(9);
        janList.add(12);
        febList = new LinkedList<>();
        febList.add(1);
        febList.add(4);
        febList.add(7);
        febList.add(10);
        febList.add(13);
        marList = new LinkedList<>();
        marList.add(2);
        marList.add(5);
        marList.add(8);
        marList.add(11);
        marList.add(14);
        matrix = new LinkedList<>();
        matrix.add(janList);
        matrix.add(febList);
        matrix.add(marList);
        matrix.add(aprList);
    }

    public static int getDistanceToQuarterEnd(int month, int todayMonth) throws Exception{
        List<Integer> quarter = getQuarterEndMonths(month);
        for (int monthOfQuarter : quarter){
            if (monthOfQuarter >= todayMonth){
                return monthOfQuarter-todayMonth;
            }
        }
        return (11-todayMonth) + quarter.get(0);
    }

    private static List<Integer> getQuarterEndMonths(int month) throws Exception{
        if (month > 11 || month < 0)
            throw new Exception("month " + month + " is out of scope. months are 0-11");
        for(List<Integer> quarter : matrix){
            if (quarter.contains(month))
                return quarter;
        }
        throw new Exception("month " + month + " is out of scope. months are 0-11");
    }

}

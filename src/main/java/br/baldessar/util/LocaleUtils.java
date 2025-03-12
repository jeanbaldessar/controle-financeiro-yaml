package br.baldessar.util;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class LocaleUtils {
	
	public static final DecimalFormat decimalFormat = new DecimalFormat("#,###.00");
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    
    static {
    	dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
}
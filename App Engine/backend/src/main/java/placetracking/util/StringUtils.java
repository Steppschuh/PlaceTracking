package placetracking.util;

import java.util.concurrent.TimeUnit;

public final class StringUtils {
	
	public static final String millisToReadableTime(long milliseconds) {
		long millis = milliseconds;
		long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder();
        
        if (days > 1) {
        	sb.append(days);
            sb.append(" days");
        }
        if (hours > 1) {
        	if (days > 1) {
        		sb.append(", ");
        	}
        	sb.append(hours);
            sb.append(" hours");
        }
        if (minutes > 1) {
        	if (hours > 1 || days > 1) {
        		sb.append(", ");
        	}
        	sb.append(minutes);
            sb.append(" minutes");
        }
        if (TimeUnit.MILLISECONDS.toMinutes(milliseconds) < 2) {
        	sb.append(seconds);
            sb.append(" seconds");
        }
        
        return sb.toString();
	}
	
}

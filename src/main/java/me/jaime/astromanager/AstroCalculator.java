package me.jaime.astromanager;

import me.jaime.astromanager.objects.CelestialBody;
import java.time.LocalDate;

public class AstroCalculator {

    public static double calculateCurrentHeight(CelestialBody object, double latitude, double lstDegrees) {
        double latRad = Math.toRadians(latitude);
        double decRad = Math.toRadians(object.getDEC());

        double haDegrees = lstDegrees - object.getRA();
        double haRad = Math.toRadians(haDegrees);

        double sinAlt = (Math.sin(decRad) * Math.sin(latRad)) + (Math.cos(decRad) * Math.cos(latRad) * Math.cos(haRad));

        double altitudeRad = Math.asin(sinAlt);
        return Math.toDegrees(altitudeRad);
    }

    public static double parseRA(String raStr) {
        try {
            String[] parts = raStr.trim().split(":");
            double h = Double.parseDouble(parts[0]);
            double m = Double.parseDouble(parts[1]);
            double s = (parts.length > 2) ? Double.parseDouble(parts[2]) : 0.0;
            return (h + m / 60.0 + s / 3600.0) * 15.0;
        } catch (Exception e) { return 0.0; }
    }

    public static double parseDEC(String decStr) {
        try {
            String[] parts = decStr.trim().split(":");
            double sign = decStr.contains("-") ? -1 : 1;
            double g = Math.abs(Double.parseDouble(parts[0]));
            double m = Double.parseDouble(parts[1]);
            double s = (parts.length > 2) ? Double.parseDouble(parts[2]) : 0.0;
            return sign * (g + m / 60.0 + s / 3600.0);
        } catch (Exception e) { return 0.0; }
    }

    public static double calculateLST(LocalDate date, double localDecimalHour, double userLongitude) {

        java.time.ZoneId zone = java.time.ZoneId.systemDefault();
        java.time.ZonedDateTime zdt = date.atStartOfDay(zone);
        double offsetHours = zdt.getOffset().getTotalSeconds() / 3600.0;

        double utcHour = localDecimalHour - offsetHours;

        if (utcHour < 0) utcHour += 24;
        if (utcHour >= 24) utcHour -= 24;

        int y = date.getYear();
        int m = date.getMonthValue();
        int d = date.getDayOfMonth();

        if (m <= 2) {
            m += 12;
            y -= 1;
        }

        int a = y / 100;
        int b = 2 - a + (a / 4);

        double jd = Math.floor(365.25 * (y + 4716)) + Math.floor(30.6001 * (m + 1)) + d + b - 1524.5;

        double t = (jd - 2451545.0) / 36525.0;

        double gmstDegrees = 280.46061837 + 360.98564736629 * (jd - 2451545.0) + 0.000387933 * t * t - t * t * t / 38710000.0;

        gmstDegrees = gmstDegrees % 360.0;
        if (gmstDegrees < 0) gmstDegrees += 360.0;

        double gmstHours = gmstDegrees / 15.0;

        double gmstCurrent = gmstHours + (utcHour * 1.00273790935);

        double lst = gmstCurrent + (userLongitude / 15.0);

        while (lst < 0) lst += 24.0;
        while (lst >= 24) lst -= 24.0;

        return lst;
    }

    public static String calculateTimeLeft(CelestialBody object, double lat, double lon, double currentHour, LocalDate date,
                                           double minAltitude, double blockStart, double blockEnd) {

        double lstHours = calculateLST(date, currentHour, lon);
        double currentAlt = calculateCurrentHeight(object, lat, lstHours * 15.0);
        double currentAz = getAzimuth(object, lat, lstHours);

        boolean isBlockedByWall = (currentAz >= blockStart && currentAz <= blockEnd);
        if (currentAlt < minAltitude || isBlockedByWall) {
            return "---";
        }

        for (double i = 0; i < 24; i += 0.1) {
            double futureHour = currentHour + i;
            if (futureHour >= 24) futureHour -= 24;

            double futureLST = calculateLST(date, futureHour, lon);
            double futureAlt = calculateCurrentHeight(object, lat, futureLST * 15.0);
            double futureAz = getAzimuth(object, lat, futureLST);

            if (futureAlt < minAltitude) {
                return formatTime(i, "↘");
            }

            if (futureAz >= blockStart && futureAz <= blockEnd) {
                return formatTime(i, "🏢");
            }
        }

        return "♾ Always";
    }

    private static String formatTime(double hoursDiff, String icon) {
        int h = (int) hoursDiff;
        int m = (int) ((hoursDiff - h) * 60);

        if (h == 0 && m < 5) return icon + " Now";
        return String.format("%s %dh %dm", icon, h, m);
    }
    public static double getAzimuth(CelestialBody object, double latitud, double lstHours) {

        double latRad = Math.toRadians(latitud);
        double decRad = Math.toRadians(object.getDEC());

        double haGrados = (lstHours * 15.0) - object.getRA();
        double haRad = Math.toRadians(haGrados);

        double y = Math.sin(haRad);
        double x = (Math.cos(haRad) * Math.sin(latRad)) - (Math.tan(decRad) * Math.cos(latRad));

        double azRad = Math.atan2(y, x);
        double azGrados = Math.toDegrees(azRad);

        azGrados = azGrados + 180;

        azGrados = azGrados % 360;
        if (azGrados < 0) azGrados += 360;

        return azGrados;
    }
}
package anokhov.EngineUpdater.facility;

public class RegNumber {

    private static long regNumberIndex = 0;

    public static String getRegNumber(String regNumberPrefix) {
        regNumberIndex++;
        return regNumberPrefix + String.format("%05d", regNumberIndex);
    }

}

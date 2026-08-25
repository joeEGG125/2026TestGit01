package com.syscom.fep.frmcommon.ebcdic;

import org.apache.commons.lang3.StringUtils;

/**
 * CCSID(Coded Character Set Identifier) values defined on IBM i
 *
 * Please reference @see <a href="https://www.ibm.com/docs/en/i/7.5?topic=information-ccsid-values-defined-i">CCSID values defined on IBM i</a>
 *
 * @author Richard
 */
public enum CCSID {
    English(37, 1100, "US, Canada, Netherlands, Portugal, Brazil, New Zealand, Australia"),
    Netherlands(256, 1100, "Netherlands"),
    Austria_Germany(273, 1100, "Austria, Germany"),
    Denmark_Norway(277, 1100, "Denmark, Norway"),
    Finland_Sweden(278, 1100, "Finland, Sweden"),
    Italy(280, 1100, "Italy"),
    Spanish_Latin_America(284, 1100, "Spanish, Latin America"),
    UK(285, 1100, "United Kingdom"),
    Japan_Katakana(290, 1100, "Japan Katakana"),
    France(297, 1100, "France"),
    Japan_English(300, 1200, "Japan English"),
    Japanese_PC(301, 2200, "Japanese PC Data"),
    ANSI_X34(367, 5100, "ANSI X3.4 ASCII standard; USA"),
    Arabic_Speaking_Countries_1(420, 1100, "Arabic-speaking countries"),
    Greece(423, 1100, "Greece"),
    Hebrew(424, 1100, "Hebrew"),
    Arabic_Speaking_Countries_2(425, 1100, "Arabic-speaking countries"),
    PC_Data(437, 2100, "PC Data; PC Base; USA"),
    International_Latin_1(500, 1100, "Belgium, Canada, Switzerland, International Latin-1"),
    MS_DOS_Arabic(720, 2100, "MS-DOS Arabic"),
    MS_DOS_Greek(737, 2100, "MS-DOS Greek PC-Data"),
    MS_DOS_Baltic(775, 2100, "MS-DOS Baltic PC-Data"),
    ISO_8859_7(813, 4100, "ISO 8859-7; Greek/Latin"),
    ISO_8859_1(819, 4100, "ISO 8859-1; Latin Alphabet No. 1"),
    Korea_Extended(833, 1100, "Korea (extended range)"),
    Korea_Host(834, 1200, "Korea host double byte (including 1880 UDC)"),
    Traditional_Chinese(835, 1200, "Traditional Chinese host double byte (including 6204 UDC)"),
    Simplified_Chinese_extended(836, 1100, "Simplified Chinese (extended range)"),
    Simplified_Chinese(837, 1200, "Simplified Chinese"),
    Thailand_extended(838, 1100, "Thailand (extended range)");

    private int value;
    private int encoding;
    private String description;

    private CCSID(int value, int encoding, String description) {
        this.value = value;
        this.encoding = encoding;
        this.description = description;
    }

    public int getValue() {
        return value;
    }

    public int getEncoding() {
        return encoding;
    }

    public String getDescription() {
        return description;
    }

    public static CCSID fromValue(int value) {
        for (CCSID e : values()) {
            if (e.getValue() == value) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
    }

    public static CCSID parse(Object nameOrValue) {
        if (nameOrValue instanceof Number) {
            return fromValue(((Number) nameOrValue).intValue());
        } else if (nameOrValue instanceof String) {
            String nameOrValueStr = (String) nameOrValue;
            if (StringUtils.isNumeric(nameOrValueStr)) {
                return fromValue(Integer.parseInt(nameOrValueStr));
            }
            for (CCSID e : values()) {
                if (e.name().equalsIgnoreCase(nameOrValueStr)) {
                    return e;
                }
            }
        }
        throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
    }
}

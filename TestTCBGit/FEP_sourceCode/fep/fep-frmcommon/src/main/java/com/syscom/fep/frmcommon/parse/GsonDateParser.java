package com.syscom.fep.frmcommon.parse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.FormatUtil;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Date;

/**
 * 針對Date類型的字串做特殊處理
 *
 * @param <T>
 * @author Richard
 */
public class GsonDateParser<T> extends GsonParser<T> {
    public GsonDateParser(Type type) {
        this(type, FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS);
    }

    public GsonDateParser(Gson gson, Type type) {
        this(type);
    }

    public GsonDateParser(Type type, String pattern) {
        super(new GsonBuilder()
                .registerTypeAdapter(Date.class, new TypeAdapter<Date>() {
                    private final LogHelper logger = new LogHelper();

                    @Override
                    public void write(JsonWriter jsonWriter, Date date) throws IOException {
                        if (date != null)
                            jsonWriter.value(FormatUtil.dateTimeFormat(date, pattern));
                        else
                            jsonWriter.nullValue();
                    }

                    @Override
                    public Date read(JsonReader jsonReader) throws IOException {
                        try {
                            String value = jsonReader.nextString();
                            if (StringUtils.isNotBlank(value))
                                return FormatUtil.parseDataTime(value, pattern);
                        } catch (ParseException e) {
                            logger.error(e, "Error parsing date");
                        }
                        return null;
                    }
                })
                .create(), type);
    }
}
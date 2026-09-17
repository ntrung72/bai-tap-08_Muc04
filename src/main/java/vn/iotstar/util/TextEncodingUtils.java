package vn.iotstar.util;

import org.springframework.web.util.HtmlUtils;

public final class TextEncodingUtils {
    private TextEncodingUtils() {
    }

    public static String normalize(String value) {
        return value == null ? null : HtmlUtils.htmlUnescape(value);
    }
}

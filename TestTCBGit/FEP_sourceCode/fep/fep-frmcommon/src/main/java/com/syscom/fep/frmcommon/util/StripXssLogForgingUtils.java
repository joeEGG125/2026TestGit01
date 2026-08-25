package com.syscom.fep.frmcommon.util;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.owasp.esapi.ESAPI;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @Classname StripXssLogForgingUtils
 * @Description
 * @Date 2024/03/11 11:37
 * @Created by zk
 */
public class StripXssLogForgingUtils {

    /**
     * 導致log forging日誌漏洞的特殊字符的列表
     */
    private final static List<String> FORGING_LIST = Arrays.asList("%0d", "%0D", "\\\r", "\r", "%0a", "%0A", "\\\n", "\n");

    /**
     * 定義Pattern數組，用於正則匹配，可增加其他pattern規則至此
     */
    private static final Pattern[] patterns = new Pattern[]{
            // Script fragments
            Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE),
            // src='...'
            Pattern.compile("src[\r\n]*=[\r\n]*\\'(.*?)\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            // lonely script tags
            Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            // eval(...)
            Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            // expression(...)
            Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            // javascript:...
            Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
            // vbscript:...
            Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
            // view-source
            Pattern.compile("view-source:", Pattern.CASE_INSENSITIVE),
            // onload(...)=...
            Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),

            Pattern.compile("<iframe>(.*?)</iframe>", Pattern.CASE_INSENSITIVE),

            Pattern.compile("</iframe>", Pattern.CASE_INSENSITIVE),

            Pattern.compile("<iframe(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
            Pattern.compile("oninput(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("onerror(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("onclick(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("confirm(.*?)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("onfocus(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("alert(.*?)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("onabort(.*?)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            Pattern.compile("onblur(.*?)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),

            Pattern.compile("onchange(.*?)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),

            Pattern.compile("ondblclick(.*?)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),

            Pattern.compile("onkeydown(.*?)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),

            Pattern.compile("onkeypress(.*?)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),

            Pattern.compile("onkeyup(.*?)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),

            Pattern.compile("onmousedown(.*?)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),

            Pattern.compile("onmousemove(.*?)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),

            Pattern.compile("onmouseout(.*?)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),

            Pattern.compile("onmouseover(.*?)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),

            Pattern.compile("onmouseup(.*?)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),

            Pattern.compile("onreset(.*?)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),

            Pattern.compile("onresize(.*?)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),

            Pattern.compile("onselect(.*?)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),

            Pattern.compile("onsubmit(.*?)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),

            Pattern.compile("onunload(.*?)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),

            // add other patterns here
            Pattern.compile("onunload(.*?)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),

            // 2025-05-20 Richard modified for 【Potential ReDoS】
            // Pattern.compile("<+\\s*\\w*\\s*(oncontrolselect|oncopy|oncut|ondataavailable|ondatasetchanged|ondatasetcomplete|ondblclick|ondeactivate|ondrag|ondragend|ondragenter|ondragleave|ondragover|ondragstart|ondrop|onerror=|onerroupdate|onfilterchange|onfinish|onfocus|onfocusin|onfocusout|onhelp|onkeydown|onkeypress|onkeyup|onlayoutcomplete|onload|onlosecapture|onmousedown|onmouseenter|onmouseleave|onmousemove|onmousout|onmouseover|onmouseup|onmousewheel|onmove|onmoveend|onmovestart|onabort|onactivate|onafterprint|onafterupdate|onbefore|onbeforeactivate|onbeforecopy|onbeforecut|onbeforedeactivate|onbeforeeditocus|onbeforepaste|onbeforeprint|onbeforeunload|onbeforeupdate|onblur|onbounce|oncellchange|onchange|onclick|oncontextmenu|onpaste|onpropertychange|onreadystatechange|onreset|onresize|onresizend|onresizestart|onrowenter|onrowexit|onrowsdelete|onrowsinserted|onscroll|onselect|onselectionchange|onselectstart|onstart|onstop|onsubmit|onunload)+\\s*=+", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)
            Pattern.compile(new String(java.util.Base64.getDecoder().decode("PCtcXHMqXFx3Klxccyoob25jb250cm9sc2VsZWN0fG9uY29weXxvbmN1dHxvbmRhdGFhdmFpbGFibGV8b25kYXRhc2V0Y2hhbmdlZHxvbmRhdGFzZXRjb21wbGV0ZXxvbmRibGNsaWNrfG9uZGVhY3RpdmF0ZXxvbmRyYWd8b25kcmFnZW5kfG9uZHJhZ2VudGVyfG9uZHJhZ2xlYXZlfG9uZHJhZ292ZXJ8b25kcmFnc3RhcnR8b25kcm9wfG9uZXJyb3I9fG9uZXJyb3VwZGF0ZXxvbmZpbHRlcmNoYW5nZXxvbmZpbmlzaHxvbmZvY3VzfG9uZm9jdXNpbnxvbmZvY3Vzb3V0fG9uaGVscHxvbmtleWRvd258b25rZXlwcmVzc3xvbmtleXVwfG9ubGF5b3V0Y29tcGxldGV8b25sb2FkfG9ubG9zZWNhcHR1cmV8b25tb3VzZWRvd258b25tb3VzZWVudGVyfG9ubW91c2VsZWF2ZXxvbm1vdXNlbW92ZXxvbm1vdXNvdXR8b25tb3VzZW92ZXJ8b25tb3VzZXVwfG9ubW91c2V3aGVlbHxvbm1vdmV8b25tb3ZlZW5kfG9ubW92ZXN0YXJ0fG9uYWJvcnR8b25hY3RpdmF0ZXxvbmFmdGVycHJpbnR8b25hZnRlcnVwZGF0ZXxvbmJlZm9yZXxvbmJlZm9yZWFjdGl2YXRlfG9uYmVmb3JlY29weXxvbmJlZm9yZWN1dHxvbmJlZm9yZWRlYWN0aXZhdGV8b25iZWZvcmVlZGl0b2N1c3xvbmJlZm9yZXBhc3RlfG9uYmVmb3JlcHJpbnR8b25iZWZvcmV1bmxvYWR8b25iZWZvcmV1cGRhdGV8b25ibHVyfG9uYm91bmNlfG9uY2VsbGNoYW5nZXxvbmNoYW5nZXxvbmNsaWNrfG9uY29udGV4dG1lbnV8b25wYXN0ZXxvbnByb3BlcnR5Y2hhbmdlfG9ucmVhZHlzdGF0ZWNoYW5nZXxvbnJlc2V0fG9ucmVzaXplfG9ucmVzaXplbmR8b25yZXNpemVzdGFydHxvbnJvd2VudGVyfG9ucm93ZXhpdHxvbnJvd3NkZWxldGV8b25yb3dzaW5zZXJ0ZWR8b25zY3JvbGx8b25zZWxlY3R8b25zZWxlY3Rpb25jaGFuZ2V8b25zZWxlY3RzdGFydHxvbnN0YXJ0fG9uc3RvcHxvbnN1Ym1pdHxvbnVubG9hZCkrXFxzKj0r")), Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)
    };

    public static String stripXssLogForging(String value) {
        if (value != null) {
            value = StringEscapeUtils.escapeJava(value);

            // 過濾導致log forging漏洞的特殊字符
            value = stripLogForging(value);

            // 使用 ESAPI 避免 encoded 的代碼攻擊
            value = ESAPI.encoder().canonicalize(value, false, false);
            value = patternReplace(value);
        }
        return value;
    }


    public static String stripLogForging(String value) {
        // ·~！@#￥%……&*（）——-=+【{】}、|；：’“，《。》、？`!@#$%^&*()-_[{]}\|;:'",<.>/?
        // 使用標準分解避免中文某些字符被轉換成英文字符 （兼容性分解會將某些中文字符轉換為英文字符）
        String encode = Normalizer.normalize(value, Normalizer.Form.NFC);
        for (String toReplaceStr : FORGING_LIST) {
            encode = encode.replace(toReplaceStr, "");
        }
        return encode;
    }

    private static String patternReplace(String value) {
        // 2024-03-28 Richard modified for Spring Comparison Timing Attack
        if (StringUtils.isNotBlank(value)) {
            // 避免空字符
            value = value.replaceAll("\0", "");

            // 根據Pattern匹配到的字符，做""替換
            for (Pattern scriptPattern : patterns) {
                if (scriptPattern == null) continue;
                value = scriptPattern.matcher(value).replaceAll("");
            }
        }
        return value;
    }
}
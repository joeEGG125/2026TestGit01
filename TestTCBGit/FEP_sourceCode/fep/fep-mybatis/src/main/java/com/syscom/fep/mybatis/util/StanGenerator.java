package com.syscom.fep.mybatis.util;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.mybatis.ext.mapper.StanExtMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class StanGenerator {
    private static final int LENGTH_LIMIT = 7;
    // private static final char BEGIN_FIRST_LETTER = 'A';
    private static final char PAD_CHAR = '0';
    @Autowired
    private StanExtMapper mapper;

    public String generate() {
        int stan = mapper.getStanSequence();
        // if (stan == 33554431) {
        //     mapper.resetStanToNext();
        //     generate();
        // }
        String generateStan = this.generate(stan);
        // 如果為null, 則說明超出了計算範圍, 則需要將STAN SEQUENCE重置為1
        // if (generateStan == null) {
        //     mapper.resetStanSequence();
        //     return this.generate();
        // }
        LogHelperFactory.getGeneralLogger().info("Stan has been generate, stan = [", stan, "], generateStan = [", generateStan, "]");
        return generateStan;
    }

    private String generate(int stan) {
        // 原始stan 轉換後
        //        1 E000001
        // 16777215 EFFFFFF
        // 16777216 F000001
        // 33554430 FFFFFFF
        if (stan <= 16777215) {
            String hex = StringUtils.join("E", StringUtils.leftPad(Integer.toHexString(stan), 6, '0').toUpperCase());
            return hex;
        } else if (stan <= 33554430) {
            stan = stan - 16777215;
            String hex = StringUtils.join("F", StringUtils.leftPad(Integer.toHexString(stan), 6, '0').toUpperCase());
            return hex;
        }
        return null;
    }

    // private String generate(int stan) {
    //     // 0000001 ~ 9999999
    //     if (stan <= 9999999) {
    //         return StringUtils.leftPad(Integer.toString(stan), LENGTH_LIMIT, PAD_CHAR);
    //     }
    //     // A000001 ~ F999999
    //     else if (stan <= 15999993) {
    //         int n = (stan - 9999999) / 999999;
    //         int m = (stan - 9999999) % 999999;
    //         if (m == 0) {
    //             n = n - 1;
    //             m = 999999;
    //         }
    //         return StringUtils.join((char) ((int) BEGIN_FIRST_LETTER + n), StringUtils.leftPad(Integer.toString(m), LENGTH_LIMIT - 1, PAD_CHAR));
    //     }
    //     // AA00001 ~ AF99999
    //     else if (stan <= 16599987) {
    //         int n = (stan - 15999993) / 99999;
    //         int m = (stan - 15999993) % 99999;
    //         if (m == 0) {
    //             n = n - 1;
    //             m = 99999;
    //         }
    //         return StringUtils.join(StringUtils.repeat(BEGIN_FIRST_LETTER, 1), (char) ((int) BEGIN_FIRST_LETTER + n), StringUtils.leftPad(Integer.toString(m), LENGTH_LIMIT - 2, PAD_CHAR));
    //     }
    //     // AAA0001 ~ AAF9999
    //     else if (stan <= 16659981) {
    //         int n = (stan - 16599987) / 9999;
    //         int m = (stan - 16599987) % 9999;
    //         if (m == 0) {
    //             n = n - 1;
    //             m = 9999;
    //         }
    //         return StringUtils.join(StringUtils.repeat(BEGIN_FIRST_LETTER, 2), (char) ((int) BEGIN_FIRST_LETTER + n), StringUtils.leftPad(Integer.toString(m), LENGTH_LIMIT - 3, PAD_CHAR));
    //     }
    //     // AAAA001 ~ AAAF999
    //     else if (stan <= 16665975) {
    //         int n = (stan - 16659981) / 999;
    //         int m = (stan - 16659981) % 999;
    //         if (m == 0) {
    //             n = n - 1;
    //             m = 999;
    //         }
    //         return StringUtils.join(StringUtils.repeat(BEGIN_FIRST_LETTER, 3), (char) ((int) BEGIN_FIRST_LETTER + n), StringUtils.leftPad(Integer.toString(m), LENGTH_LIMIT - 4, PAD_CHAR));
    //     }
    //     // AAAAA01 ~ AAAAF99
    //     else if (stan <= 16666569) {
    //         int n = (stan - 16665975) / 99;
    //         int m = (stan - 16665975) % 99;
    //         if (m == 0) {
    //             n = n - 1;
    //             m = 99;
    //         }
    //         return StringUtils.join(StringUtils.repeat(BEGIN_FIRST_LETTER, 4), (char) ((int) BEGIN_FIRST_LETTER + n), StringUtils.leftPad(Integer.toString(m), LENGTH_LIMIT - 5, PAD_CHAR));
    //     }
    //     // AAAAAA1 ~ AAAAAF9
    //     else if (stan <= 16666623) {
    //         int n = (stan - 16666569) / 9;
    //         int m = (stan - 16666569) % 9;
    //         if (m == 0) {
    //             n = n - 1;
    //             m = 9;
    //         }
    //         return StringUtils.join(StringUtils.repeat(BEGIN_FIRST_LETTER, 5), (char) ((int) BEGIN_FIRST_LETTER + n), m);
    //     }
    //     return null;
    // }

//    public static void main(String[] args) throws IOException {
//        StanGenerator generator = new StanGenerator();
//        System.out.println(generator.generate(1));
//        System.out.println(generator.generate(9999998));
//        System.out.println(generator.generate(9999999));
//        System.out.println(generator.generate(10000000));
//        System.out.println("================================");
//        System.out.println(generator.generate(15999992));
//        System.out.println(generator.generate(15999993));
//        System.out.println(generator.generate(15999994));
//        System.out.println("================================");
//        System.out.println(generator.generate(16599986));
//        System.out.println(generator.generate(16599987));
//        System.out.println(generator.generate(16599988));
//        System.out.println("================================");
//        System.out.println(generator.generate(16659980));
//        System.out.println(generator.generate(16659981));
//        System.out.println(generator.generate(16659982));
//        System.out.println("================================");
//        System.out.println(generator.generate(16665974));
//        System.out.println(generator.generate(16665975));
//        System.out.println(generator.generate(16665976));
//        System.out.println("================================");
//        System.out.println(generator.generate(16666568));
//        System.out.println(generator.generate(16666569));
//        System.out.println(generator.generate(16666570));
//        System.out.println("================================");
//        System.out.println(generator.generate(16666621));
//        System.out.println(generator.generate(16666622));
//        System.out.println(generator.generate(16666623));
//        System.out.println("================================");
//        StringBuilder sb = new StringBuilder(1024);
//        int count = 1;
//        for (int i = 10000000; i <= 16666623; i++) {
//            sb.append(generator.generate(i)).append(",");
//            if (count++ == 20) {
//                sb.append("\r\n");
//                count = 1;
//            }
//        }
//        FileUtils.write(new File("C:/Users/Richard/Desktop/stan.txt"), sb.toString(), StandardCharsets.UTF_8, false);
//    }
}

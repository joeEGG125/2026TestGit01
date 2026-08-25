package com.syscom.fep.vo.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * FISC的交易代號
 */
public enum FISCPCode {
	PCode2510(2510),
	PCode2500(2500),
	PCode2521(2521),
	PCode2522(2522),
	PCode2523(2523),
	PCode2524(2524),
	PCode2531(2531),
	PCode2532(2532),
	PCode2525(2525),
	PCode2526(2526),
	PCode2541(2541),
	PCode2542(2542),
	PCode2543(2543),
	PCode2551(2551),
	PCode2552(2552),
	PCode2561(2561),
	PCode2562(2562),
	PCode2563(2563),
	PCode2564(2564),
	PCode2568(2568),
	PCode2569(2569),
	PCode2261(2261),
	PCode2262(2262),
	PCode2263(2263),
	PCode2264(2264),
	PCode2280(2280),
	PCode2567(2567),
	PCode2400(2400),
	PCode2401(2401),
	PCode2410(2410),
	PCode2420(2420),
	PCode2430(2430),
	PCode2450(2450),
	PCode2460(2460),
	PCode2470(2470),
	PCode2480(2480),
	// add by maxine for spec update
	PCode2411(2411),
	PCode2451(2451),
	// 2016-01-27 Added by Nick for EMV代理
	// 2015-12-18 Added by Nick for ARPC EMVCommonI
	PCode2600(2600),
	PCode2601(2601),
	PCode2620(2620),
	PCode2621(2621),
	PCode2630(2630),
	PCode2631(2631),
	PCode2622(2622),
	PCode2632(2632),
	PCode2633(2633),
	PCode2640(2640),
	PCode5001(5001),
	PCode5312(5312),
	PCode5203(5203),
	PCode5314(5314),
	PCode5102(5102),
	// 2017-09-07 Modify by Ruling for 晶片金融卡跨國提款及消費扣款交易
	PCode2505(2505),
	PCode2545(2545),
	PCode2546(2546),
	pcode2549(2549),
	PCode2571(2571),
	PCode2572(2572),
	PCode2573(2573),
	PCode2574(2574),
	// 2018-07-26 Modify by Ruling for CASH OUTBOUND
	PCode2555(2555),
	PCode2556(2556),
	PCode2160(2160),
	PCode2290(2290),
	// 2018-07-26 Modify by Ruling for 2566約定及金融卡簽收邏輯調整
	PCode2566(2566);

	private int value;

	private FISCPCode(int value) {
		this.value = value;
	}
	
	public String getValueStr() {
		return String.valueOf(value);
	}

	public static FISCPCode fromValue(int value) {
		for (FISCPCode e : values()) {
			if (e.value == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static FISCPCode parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (FISCPCode e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}
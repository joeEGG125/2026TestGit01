package com.syscom.fep.enchelper.vo;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.enchelper.enums.ENCKeyType;
import com.syscom.fep.enchelper.enums.ENCKeyType;

/**
 * key_identify(X63) : = key_qty(N1) + key_type1(X2) + key_id_1(X20) + key_type2(X2) + key_id_2(X20) + key_type3(X2) + key_id_3(X20)
 */
public class KeyIdentity {
	/**
	 * Key Quantity
	 */
	private int keyQty;

	/**
	 * Key Type1
	 */
	private ENCKeyType keyType1;

	/**
	 * Key Type2
	 */
	private ENCKeyType keyType2;

	/**
	 * Key Type3
	 */
	private ENCKeyType keyType3;

	/**
	 * KeyId1
	 */
	private KeyId keyId1;

	/**
	 * KeyId2
	 */
	private KeyId keyId2;

	/**
	 * KeyId3
	 */
	private KeyId keyId3;
	
	public KeyIdentity() {
		this.keyId1 = new KeyId();
		this.keyId2 = new KeyId();
		this.keyId3 = new KeyId();
		this.keyType1 = ENCKeyType.None;
		this.keyType2 = ENCKeyType.None;
		this.keyType3 = ENCKeyType.None;
	}

	public int getKeyQty() {
		return keyQty;
	}

	public void setKeyQty(int keyQty) {
		this.keyQty = keyQty;
	}

	public ENCKeyType getKeyType1() {
		return keyType1;
	}

	public void setKeyType1(ENCKeyType keyType1) {
		this.keyType1 = keyType1;
	}

	public ENCKeyType getKeyType2() {
		return keyType2;
	}

	public void setKeyType2(ENCKeyType keyType2) {
		this.keyType2 = keyType2;
	}

	public ENCKeyType getKeyType3() {
		return keyType3;
	}

	public void setKeyType3(ENCKeyType keyType3) {
		this.keyType3 = keyType3;
	}

	public KeyId getKeyId1() {
		return keyId1;
	}

	public void setKeyId1(KeyId keyId1) {
		this.keyId1 = keyId1;
	}

	public KeyId getKeyId2() {
		return keyId2;
	}

	public void setKeyId2(KeyId keyId2) {
		this.keyId2 = keyId2;
	}

	public KeyId getKeyId3() {
		return keyId3;
	}

	public void setKeyId3(KeyId keyId3) {
		this.keyId3 = keyId3;
	}
	
	/**
	 * 組合KeyIdentity
	 * 因為FN00203檢核密碼時KeyID需要用到兩組Key
	 * 明祥新增KeyType2跟KeyType3，若沒用到就填兩個空白。
	 * 所以所有key.KeyType ==> key.KeyType1
	 */
	@Override
	public String toString() {
		String key = StringUtils.join(String.valueOf(this.keyQty), this.keyType1.name(), this.keyId1);
		if (this.keyType2 == ENCKeyType.None) {
			key = StringUtils.join(key, StringUtils.repeat(StringUtils.SPACE, 2));
		} else {
			key = StringUtils.join(key, this.keyType2.name());
		}
		key= StringUtils.join(key, keyId2);
		if (this.keyType3 == ENCKeyType.None) {
			key = StringUtils.join(key, StringUtils.repeat(StringUtils.SPACE, 2));
		} else {
			key = StringUtils.join(key, this.keyType3.name());
		}
		key = StringUtils.join(key, this.keyId3);
		return key;
	}
}

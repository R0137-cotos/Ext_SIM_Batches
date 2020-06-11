package jp.co.ricoh.cotos.dto;

import java.util.Comparator;

public class ExtendsParameterDtoComparator implements Comparator<SIMExtendsParameterIteranceDto> {
	public int compare(SIMExtendsParameterIteranceDto a, SIMExtendsParameterIteranceDto b) {
		String no1 = a.getProductCode();
		String no2 = b.getProductCode();
		String no3 = a.getSerialNumber();
		String no4 = b.getSerialNumber();

		//　商品コードの昇順でソート
		if (no1 != null && no2 != null) {
			if (no1.compareTo(no2) > 0) {
				return 1;
			} else if (no1.compareTo(no2) == 0) {
				// 商品コードが同じ場合、シリアル番号の昇順でソート
				if (no3 != null && no4 != null) {
					if (no3.compareTo(no4) > 0) {
						return 1;
					} else if (no3.compareTo(no4) == 0) {
						return 0;
					}
					return -1;
				}
				return 0;
			} else {
				return -1;
			}
		}
		return 0;
	}
}

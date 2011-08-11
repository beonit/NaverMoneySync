package beonit.NaverMoneySync;

import java.util.StringTokenizer;

public class Parser {
	public static String Parse(String sms) throws Exception{
		/*
		 * [KB카드] 이승한님 8*9*카드 03월06일01:01 3000원 훼리미리마트 매탄 사용
		 * 삼성카드 03/06 14:26 11번가 39,320원 일시불사용 감사합니다
    	 * http://moneybook.naver.com/m/write.nhn?method=quick
    	 * 아래 형식으로 자유롭게 여러건을 등록하세요.
    	 * 날짜 v 사용내역 v 카드 or 현금 v 금액 (v=공백)
    	 * 여러건 입력 시 ; 세미콜론으로 구분 합니다.
    	 * 카드 승인 SMS를 복사하여 붙여 입력 할 수 있어요.
    	 * 예) 06/05 사과 1,500원; 계란 2,800원
    	 */
		return new Parser(sms).toString();
	}
	
	public String cardCompany;
	public String date;
	public String store;
	public String money;
	
	public String toString(){
		// 날짜 v 사용내역 v 카드 or 현금 v 금액 (v=공백)
		return date + " " + store + " " + cardCompany + " " + money;
	}
	
	public Parser(String sms) throws Exception{
		if( sms == null )
			return;
		StringTokenizer tokens = new StringTokenizer(sms);
		if( !tokens.hasMoreTokens() )
			throw new Exception("sms content parse fail");
		cardCompany = tokens.nextToken();	// [KB카드], 삼성카드
		if( cardCompany.equals("삼성카드"))
			parseSamsung(tokens);
		else if( cardCompany.equals("[KB카드]") ){
			cardCompany = "KB카드";
			parseKB(tokens);
		}
		else{
			throw new Exception("not supported card type");
		}
	}

	private void parseSamsung(StringTokenizer tokens) throws Exception {
//		 삼성카드 03/06 14:26 11번가 39,320원 일시불사용 감사합니다
		if( !tokens.hasMoreTokens() )
			throw new Exception("sms content parse fail");
		date = tokens.nextToken();
		if( !tokens.hasMoreTokens() )
			throw new Exception("sms content parse fail");
		tokens.nextToken();
		if( !tokens.hasMoreTokens() )
			throw new Exception("sms content parse fail");
		store = tokens.nextToken();
		if( !tokens.hasMoreTokens() )
			throw new Exception("sms content parse fail");
		money = tokens.nextToken();
	}

	private void parseKB(StringTokenizer tokens) throws Exception {
//		[KB카드] 이승한님 8*9*카드 03월06일01:01 3000원 훼리미리마트 매탄 사용
		if( !tokens.hasMoreTokens() )
			throw new Exception("sms content parse fail");
		tokens.nextToken();
		if( !tokens.hasMoreTokens() )
			throw new Exception("sms content parse fail");
		tokens.nextToken();
		if( !tokens.hasMoreTokens() )
			throw new Exception("sms content parse fail");
		date = tokens.nextToken();  //03월06일01:01
		date = date.substring(0, 6);
		if( !tokens.hasMoreTokens() )
			throw new Exception("sms content parse fail");
		money = tokens.nextToken();
		store = tokens.nextToken();
		while( tokens.hasMoreTokens() )
			store += " " + tokens.nextToken();
	}
}

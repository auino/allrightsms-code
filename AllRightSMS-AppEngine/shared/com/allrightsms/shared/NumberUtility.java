package com.allrightsms.shared;

public class NumberUtility {

	public static String purgePrefix(String num) {
		if (num.startsWith("+"))
				return num.substring(3);
		// parto dalla seconda posizione, perchè potrei avere un +39 davanti al
		// numero
			return num;		
	}

	public static boolean ValidatePhoneNumber(String phNumber) {
		// tre numeri per il prefisso e 7 per il numero, separati da - o spazio
		String numPattern = "^\\d{3}[- ]?\\d{7}$";
		return phNumber.matches(numPattern);
	}
	
	public static String purgeNumber(String num)
	{
		if (num.contains("-"))
			return num.replace("-", "");
		else if (num.contains(" "))
			num.replace(" ", "");
		else
			return num;
		return num;
	}
	
	public static String extractName(String s)
	{
		return s.substring(0, s.indexOf("(")-1);
	}
	
	public static String addDash(String num){
		return num.substring(0,3)+"-"+num.substring(3, num.length());
	}
	
	//private method for uniform suggestion string
	public static String createSuggestionString(String name, String num){
		return name+" ("+NumberUtility.purgeNumber(num)+")";
	}
}

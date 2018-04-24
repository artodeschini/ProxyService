package br.org.cni.sgt.utils;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.Normalizer;

public class StringUtils {

	private static final Charset ISO_8859_1 = Charset.forName("US-ASCII");
	private static final Charset UTF_8 = Charset.forName("UTF-8");

	/**
	 * @param str
	 *            A string a normalizar.
	 * @return A string normalizada.
	 */
	public static String normalize(String str1) {
		String str2 = str1;
		str2 = str2.replaceAll("[�����]", "A");
		str2 = str2.replaceAll("[�����]", "a");
		str2 = str2.replaceAll("[����]", "E");
		str2 = str2.replaceAll("[����]", "e");
		str2 = str2.replaceAll("����", "I");
		str2 = str2.replaceAll("�����", "i");
		str2 = str2.replaceAll("[�����]", "O");
		str2 = str2.replaceAll("[�����]", "o");
		str2 = str2.replaceAll("[����]", "U");
		str2 = str2.replaceAll("[����]", "u");
		str2 = str2.replaceAll("�", "C");
		str2 = str2.replaceAll("�", "c");
		str2 = str2.replaceAll("[��]", "y");
		str2 = str2.replaceAll("�", "Y");
		str2 = str2.replaceAll("�", "n");
		str2 = str2.replaceAll("�", "N");
		str2 = str2.replaceAll("[-+=*&%$#@!_]", "");
		str2 = str2.replaceAll("['\"]", "");
		str2 = str2.replaceAll("[<>()\\{\\}]", "");
		str2 = str2.replaceAll("['\\\\.,()|/]", "");
		str2 = str2.replaceAll("[^!-�]{1}[^ -�]{0,}[^!-�]{1}|[^!-�]{1}", " ");
		
		str2 = Normalizer.normalize(str1, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
		str2 = str2.replaceAll("'"," ");
		return str2;
	}
	
	public static String replaceSpaceByUnderLine(String str1) {
		String str2 = str1;
		return str2.replaceAll(" ", "_");
	}
	
	public static String replaceToUri(String str) {
		StringBuilder builder = new StringBuilder();
		
		str = str.trim();
		
		for (int i = 0; i < str.length(); i++) {
			if ( str.charAt(i) == ' ' ) {
				builder.append("%20");
			}
			builder.append(str.charAt(i) );
		}
		
		return builder.toString();
	}

	public static String stringAsciiToUtf8(String ascii) {
		if (ascii != null) {
			return new String(ascii.getBytes(ISO_8859_1), UTF_8);
		}

		return "";
	}
	
	public static String onlyNumbers(String aNumeber) {
		return aNumeber.replaceAll("\\D", "");
	}
	
	public static String formateNuZerosEsquerda(Number number,int nuCasas){
    	String formato = "";
		for(int i = 0; i < nuCasas ;i++)
    	{
    		formato+="0";
    	}
    	DecimalFormat df = new DecimalFormat(formato);
    	return (number != null && nuCasas > 0) ? df.format(number) : "";
	}
	
	public static String chaveBuscaCidade(String name, String uf) {
		StringBuilder sb = new StringBuilder();
		sb.append( uf );
		sb.append( "_" );
		sb.append( replaceSpaceByUnderLine( normalize(name) ) );
		return sb.toString().toUpperCase();
	}
	
	
	public static String UTF8toISO(String str){
        Charset utf8charset = Charset.forName("UTF-8");
        Charset iso88591charset = Charset.forName("ISO-8859-1");

        ByteBuffer inputBuffer = ByteBuffer.wrap(str.getBytes());

        // decode UTF-8
        CharBuffer data = utf8charset.decode(inputBuffer);

        // encode ISO-8559-1
        ByteBuffer outputBuffer = iso88591charset.encode(data);
        byte[] outputData = outputBuffer.array();
        
        return new String(outputData);
    }
	
	public static String ISOtoUTF8(String str){
        Charset utf8charset = Charset.forName("UTF-8");
        Charset iso88591charset = Charset.forName("ISO-8859-1");

        ByteBuffer inputBuffer = ByteBuffer.wrap(str.getBytes());

        // decode ISO-8559-1
        CharBuffer data = iso88591charset.decode(inputBuffer);

        // encode UTF-8
        ByteBuffer outputBuffer = utf8charset.encode(data);
        byte[] outputData = outputBuffer.array();
        
        return new String(outputData);
    }
	
}

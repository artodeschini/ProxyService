package br.org.cni.sgt.utils;
public abstract class SGTBase {
	
	public static String HTTP = "http";
	public static String HTTPS = "https";
	public static String PROTO = HTTP;
	
	public static String PORT_P = "9763";
	public static String PORT_H = "9764";
	public static String PORT_D = "9764";
	
	public static String SERVER_P = "sgt.cni.org.br";
	public static String SERVER_H = "sgth.sc.senai.br";
	public static String SERVER_D = "sgtd.sc.senai.br";
	
	public static String getBase(String b) {
		String base = "";
		if ( "h".equalsIgnoreCase( b ) ) {
			base = PROTO + "://" + SERVER_H + ":" + PORT_H + "/SGTWebApp/rest";
		} else if ( "p".equalsIgnoreCase( b )) {
			base = PROTO + "://" + SERVER_P + ":" + PORT_P + "/SGTWebApp/rest";
		} else if ( "d".equalsIgnoreCase( b ) ) {
			base = PROTO + "://" + SERVER_D + ":" + PORT_D + "/SGTWebApp/rest";
		} else {
			throw new RuntimeException("ERRO AO SELECIONAR BASE NO SGT");
		}
		return base;
	}
	
}

package br.org.cni.sgt.utils;
	
	import java.io.IOException;
	import java.io.OutputStreamWriter;
	import java.net.HttpURLConnection;
	import java.net.URL;
	import java.util.List;

	import org.json.JSONStringer;
	import org.json.JSONWriter;


	public class AutenticadorSGT {
		
		private static AutenticadorSGT oAuth;
		private List<String> cookies;

		private AutenticadorSGT(String b) throws IOException {
			
			StringBuilder urlSGT = new StringBuilder()
			.append( SGTBase.getBase( b ) )
			.append("/security/login");

			URL securitySGT = new URL(  urlSGT.toString() );
			HttpURLConnection oAuthSGT = (HttpURLConnection) securitySGT.openConnection();
			
			oAuthSGT.setDoOutput(true);
			oAuthSGT.setDoInput(true);
			oAuthSGT.setRequestMethod("POST");
			oAuthSGT.setRequestProperty("Content-Type", "application/json");
			oAuthSGT.setRequestProperty("Accept", "application/json");

			JSONWriter payload = new JSONStringer();
			
			payload.object()
				.key("login")
				.value("secret")
				.key("senha")
				.value("secret")
			 	.endObject();
			
			OutputStreamWriter osw = new OutputStreamWriter( oAuthSGT.getOutputStream() );
			osw.write( payload.toString() );
			osw.flush();

			cookies = oAuthSGT.getHeaderFields().get("Set-Cookie");
		}

		public static synchronized AutenticadorSGT getInstance(String b) {
			if (oAuth == null) {
				try {
					oAuth = new AutenticadorSGT( b );
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				for (String c : oAuth.cookies) {
					if (c.contains("false")) {
						try {
							oAuth = new AutenticadorSGT( b);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

			}
			return oAuth;
		}
		
		public String bCookie() {
			StringBuilder sb = new StringBuilder();
			for (String c : cookies) {                    
				sb.append( c.split(";", 2)[0] );
			}
			return sb.toString();
		}

		public List<String> getCookieList() {
			return cookies;
		}
		
		public static synchronized void rmfCookie() {
			oAuth = null;
		}

}

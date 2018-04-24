package br.org.cni.sgt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import br.org.cni.sgt.utils.AutenticadorSGT;
import br.org.cni.sgt.utils.SGTBase;
import br.org.cni.sgt.utils.StringUtils;

public class ConsultaCidadeSGT {
	
	public CidadeSearchDTO findCidadeSGT(CidadeSearchDTO dto, String b) throws IOException {
		CidadeSearchDTO find = null;
		
		StringBuilder uriCidadeSGT = new StringBuilder()
		.append( SGTBase.getBase( b ) )
        .append("/pessoa/Municipio?buscaLivreMunicipio=")
        .append( StringUtils.replaceToUri( dto.getNomefmt() ) );
        
		URL urlCidadeSGT = new URL( uriCidadeSGT.toString() );
		
		HttpURLConnection connectionSGT = (HttpURLConnection) urlCidadeSGT.openConnection();
		connectionSGT.setRequestMethod("GET");
		connectionSGT.setRequestProperty("Accept", "application/json");
		
		AutenticadorSGT.getInstance(b).rmfCookie();
		
		connectionSGT.addRequestProperty("Cookie", AutenticadorSGT.getInstance(b).bCookie() );
		
		BufferedReader bufferResponseSGT = null;
		
		String sgtResponse = "";
		String context;
		bufferResponseSGT = new BufferedReader( new InputStreamReader( connectionSGT.getInputStream(), "UTF-8" ));
		
		while ( (context = bufferResponseSGT.readLine() ) != null) {
			sgtResponse = context;
		}
		
//		System.out.println( sgtResponse );
		
		JSONArray array = new JSONArray( sgtResponse );
		
		Map<String, CidadeSearchDTO> cidadesSGT = new HashMap<String, CidadeSearchDTO>();
		
		String uf;
//		String chave;
		String nome;
		
		for (int i = 0; i < array.length(); i++) {
			JSONObject c = array.getJSONObject(i);
			uf = c.getJSONObject("unidadeFederativa").get("sigla").toString();
//			System.out.println( c );
			nome = c.getString("descricao");
			if ( dto.getUf().equals( uf ) ) {
				cidadesSGT.put( StringUtils.chaveBuscaCidade(nome, uf) , new CidadeSearchDTO(c.getString("codigoIBGE"), nome, uf, String.valueOf( c.getInt("id"))));
			}
		}
		
		find = cidadesSGT.get(  dto.keyBusca() );
//		System.out.println( find );
		
		return find;
	}

}

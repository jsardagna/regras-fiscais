package com.contaazul.fiscal.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.contaazul.fiscal.utils.Estados;
import com.contaazul.fiscal.utils.SSLHelper;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/fiscal/arquivos/update")
public class FilesUpdate {

	private static final String URL_CONFAZ = "https://www.confaz.fazenda.gov.br/legislacao/portal-nacional-da-substituicao-tributaria";

	@RequestMapping(method = RequestMethod.GET)
	public String update() {
		String retorno = "";
		Map<Estados, String> estados = carregaEstados();
		for (Entry<Estados, String> estado : estados.entrySet()) {
			try {
				retorno += " " + estado.getKey().toString() + "<br/>";
				Elements elements = carregaURL(estado.getValue(), "a[href*=\".xls\"]");
				for (Element element : elements) {
					retorno += " " + element.attr("href") + " " + element.text() + "<br/> ";
					String url = element.attr("href");
					String fileName = estado.getKey().toString() + " - " + element.text().toLowerCase() + ".xlsx";
					extractXLS(url, fileName);	
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return retorno;

	}

	private void extractXLS(String url, String fileName) {
		try {
			System.out.println("carregando " + fileName);
			final File f = new File("documentos/"+fileName);
			if (!f.exists()) {
				byte[] bytes = SSLHelper.getConnection(url).header("Accept-Encoding", "gzip, deflate")
						.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
						.ignoreContentType(true).maxBodySize(0).timeout(600000).execute().bodyAsBytes();

				System.out.println("salvando " + fileName);
				FileUtils.writeByteArrayToFile(f, bytes);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Map<Estados, String> carregaEstados() {
		Map<Estados, String> estados = new HashMap<Estados, String>();
		try {
			String cssQuery = "table td a";

			Elements elements = carregaURL(URL_CONFAZ, cssQuery);
			for (Element element : elements) {
				String value = element.text();
				if (value != null) {
					for (Estados estado : Estados.values()) {
						if (estado.toString().toLowerCase().equals(value.toLowerCase().trim())) {
							estados.put(estado, element.attr("href"));
						}
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return estados;
	}

	private Elements carregaURL(String url, String cssQuery) throws IOException {
		Document doc = SSLHelper.getConnection(url).ignoreContentType(true).get();

		Elements elements = doc.select(cssQuery);
		return elements;
	}

}

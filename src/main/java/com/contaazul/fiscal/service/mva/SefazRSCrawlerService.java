package com.contaazul.fiscal.service.mva;

import java.io.File;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.contaazul.fiscal.enttiy.Cest;
import com.contaazul.fiscal.enttiy.mva.Item;
import com.contaazul.fiscal.enttiy.mva.Lei;
import com.contaazul.fiscal.enttiy.mva.MVA;
import com.contaazul.fiscal.enttiy.mva.SefazRSCols;
import com.contaazul.fiscal.enttiy.mva.Segmento;
import com.contaazul.fiscal.service.ipi.NCMService;
import com.contaazul.fiscal.utils.ColumnsHTML;
import com.contaazul.fiscal.utils.Estados;
import com.contaazul.fiscal.utils.FiscalCrawlerUtils;

@Service
public class SefazRSCrawlerService {

	@Autowired
	private LeiService regras;

	@Autowired
	CestService cestService;

	@Autowired
	NCMService ncmService;

	private static final String RS_MVA_FILE = "RS-MVA.html";
	private static final String URL_MVA_RS = "http://www.legislacao.sefaz.rs.gov.br/Site/DocumentView.aspx?inpKey=109362&inpCodDispositive=0&inpStToValidateDoc=&inpForceEdit=&inpDtTimeTunnel=&inpDsKeywords=&Debug=";

	public String updateFile() {
		FiscalCrawlerUtils.extractFile(URL_MVA_RS, RS_MVA_FILE, true);
		return "carregado";
	}

	public void updateRules() {
		try {
			String cssQuery = "table:contains(CÓDIGO ESPECIFICADOR DA SUBSTITUIÇÃO TRIBUTÁRIA - CEST)";
			File in = FiscalCrawlerUtils.carregaArquivo(RS_MVA_FILE);

			Document doc = Jsoup.parse(in, null);

			LocalDate versao = FiscalCrawlerUtils.extractDate(doc, "div.static_date:contains(Este documento foi gerado em)");

			Lei lei = regras.findByArquivoAndEfeito(RS_MVA_FILE, versao);
			if (lei == null) {
				lei = new Lei();
				lei.setArquivo(RS_MVA_FILE);
				lei.setCriacao(LocalDate.now());
				lei.setEfeito(versao);
				lei.setEstado(Estados.RS);
			} else {
				return;
			}

			Elements tables = doc.select(cssQuery);
			for (Element table : tables) {

				Segmento segmento = new Segmento();
				segmento.setNome("");
				lei.addSegmento(segmento);

				Elements rows = table.select("tr");
				Elements segs = rows.get(0).select("td");
				if (segs.size() > 0 && segs.get(0).text().matches(("(?=ITEM )(?=.*? - ).*"))) {
					segmento.setDescricaoSegmento(FiscalCrawlerUtils.corrigeTexto(segs.get(0).text()));
				}

				if (segs.size() > 0 && segs.get(0).text().matches(("(?=NOTA - ).*"))) {
					segmento.setObservacoes(FiscalCrawlerUtils.corrigeTexto(segs.get(0).text()));
				}

				if (segs.size() > 1 && segs.get(1).text().matches(("(?=NOTA - ).*"))) {
					segmento.setObservacoes(FiscalCrawlerUtils.corrigeTexto(segs.get(1).text()));
				}

				Map<SefazRSCols, Integer> colunas = new HashMap<SefazRSCols, Integer>();
				columns(rows, colunas);
				for (int r = 0; r < rows.size(); r++) {
					Element row = rows.get(r);
					ColumnsHTML cols = new ColumnsHTML(row.select("td"));
					if (cols.size() >= colunas.size()) {
						Item item = null;
						item = new Item();
						segmento.addItem(item);
						if (colunas.containsKey(SefazRSCols.NUM)) {
							String value = cols.get(colunas.get(SefazRSCols.NUM));
							if (FiscalCrawlerUtils.isNumericc(value)) {
								item.setOrdem(FiscalCrawlerUtils.corrigeValor(value).longValue());
							}
						}
						if (colunas.containsKey(SefazRSCols.DESCRICAO)) {
							item.setDescricao(FiscalCrawlerUtils.corrigeTexto(cols.get(colunas.get(SefazRSCols.DESCRICAO))));
						}
//						if (colunas.containsKey(SefazRSCols.NCM)) {
//							String value = corrigeTexto(cols.get(colunas.get(SefazRSCols.NCM)).text());
//							NCM ncm = ncmService.findByCodigo(value);
//							if (ncm == null) {
//								ncm = new NCM();
//								ncm.setCodigo(value);
//								ncm.setDescricao(corrigeTexto(cols.get(colunas.get(SefazRSCols.DESCRICAO)).text()));
//								ncmService.save(ncm);
//							}
//							item.setNcm(ncm);
//						}
						if (colunas.containsKey(SefazRSCols.CEST)) {
							Cest.addCEST(cestService, item, cols.get(colunas.get(SefazRSCols.CEST)));
						}
						if (colunas.containsKey(SefazRSCols.MVAINTERNA)) {
							MVA.addMVA(item, cols.get(colunas.get(SefazRSCols.MVAINTERNA)), "OPERAÇÃO INTERNA", "MVAINTERNA");

						}
						if (colunas.containsKey(SefazRSCols.MVA12)) {
							MVA.addMVA(item, cols.get(colunas.get(SefazRSCols.MVA12)), "SUJEITA À ALÍQUOTA DE 12%", "MVA12");
						}
						if (colunas.containsKey(SefazRSCols.MVA4)) {
							MVA.addMVA(item, cols.get(colunas.get(SefazRSCols.MVA4)), "SUJEITA À ALÍQUOTA DE 4%", "MVA4");
						}
					}
				}
			}
			regras.save(lei);
			regras.updateUltimaVersao();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static int columns(Elements rows, Map<SefazRSCols, Integer> colunas) {
		int[][] cell = new int[10][10];
		int headLen = 0;
		for (int r = 0; r < (rows.size() > 7 ? 7 : rows.size()); r++) {
			Element row = rows.get(r);
			Elements cols = row.select("td");
			int colNum = 0;
			for (int c = 0; c < cols.size(); c++) {
				for (int cl = colNum; cl <= 10; cl++) {
					if (cell[r][cl] > 0) {
						colNum += 1;
					} else {
						break;
					}
				}
				Element col = cols.get(c);
				int rspan = StringUtils.isNumeric(col.attr("rowspan")) ? Integer.parseInt(col.attr("rowspan")) : 1;
				if (rspan > 1) {
					for (int r2 = r + 1; r2 < (rspan + r > 10 ? 10 : rspan + r); r2++) {
						cell[r2][colNum] = 1;
					}
				}

				for (SefazRSCols el : SefazRSCols.values()) {
					if (col.text().matches(el.regex)) {
						colunas.put(el, colNum);
					}
				}
				colNum += StringUtils.isNumeric(col.attr("colspan")) ? Integer.parseInt(col.attr("colspan")) : 1;
			}

			System.out.println("\n" + rows.get(0).text());
			for (Entry<SefazRSCols, Integer> keyentry : colunas.entrySet()) {
				System.out.println(keyentry.getValue() + " " + keyentry.getKey());
			}
		}
		return headLen;
	}

}

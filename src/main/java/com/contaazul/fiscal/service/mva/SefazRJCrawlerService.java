package com.contaazul.fiscal.service.mva;

import java.io.File;
import java.io.FileInputStream;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Table;
import org.apache.poi.hwpf.usermodel.TableCell;
import org.apache.poi.hwpf.usermodel.TableIterator;
import org.apache.poi.hwpf.usermodel.TableRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.contaazul.fiscal.enttiy.Cest;
import com.contaazul.fiscal.enttiy.mva.Item;
import com.contaazul.fiscal.enttiy.mva.Lei;
import com.contaazul.fiscal.enttiy.mva.MVA;
import com.contaazul.fiscal.enttiy.mva.SefazRJCols;
import com.contaazul.fiscal.enttiy.mva.Segmento;
import com.contaazul.fiscal.service.ipi.NCMService;
import com.contaazul.fiscal.utils.ColumnsDoc;
import com.contaazul.fiscal.utils.Estados;
import com.contaazul.fiscal.utils.FiscalCrawlerUtils;

@Service
public class SefazRJCrawlerService {

	@Autowired
	private LeiService regras;

	@Autowired
	CestService cestService;

	@Autowired
	NCMService ncmService;

	private static final String MVA_RJ = "anexo_I_livroII.doc";
	private static final String URL_MVA_RJ = "http://www.fazenda.rj.gov.br/sefaz/content/conn/UCMServer/path/Contribution%20Folders/site_fazenda/informacao/icms/substituicao_trib/anexo_I_livroII_2.doc?lve";

	final NumberFormat numberFormat = NumberFormat.getInstance(Locale.GERMANY);

	public LocalDate updateFile() {
		return FiscalCrawlerUtils.extractFile(URL_MVA_RJ, MVA_RJ, true);
	}

	public void updateRules() {
		try {
			File file = FiscalCrawlerUtils.carregaArquivo(MVA_RJ);
			LocalDate versao = Instant.ofEpochMilli(file.lastModified()).atZone(ZoneId.systemDefault()).toLocalDate();
			Lei lei = regras.findByArquivoAndEfeito(MVA_RJ, versao);
			if (lei == null) {
				lei = new Lei();
				lei.setArquivo(MVA_RJ);
				lei.setCriacao(LocalDate.now());
				lei.setEfeito(versao);
				lei.setEstado(Estados.RJ);
			} else {
				return;
			}

			HWPFDocument doc = new HWPFDocument(new FileInputStream(file));
			Range range = doc.getRange();
			TableIterator itr = new TableIterator(range);

			while (itr.hasNext()) {
				Table table = itr.next();
				Map<SefazRJCols, Integer> colunas = new HashMap<SefazRJCols, Integer>();
				extrairColunas(colunas, table);
				Segmento segmento = new Segmento();
				segmento.setNome("");
				lei.addSegmento(segmento);

				for (int rowIndex = 0; rowIndex < table.numRows(); rowIndex++) {
					ColumnsDoc cols = new ColumnsDoc(table.getRow(rowIndex));
					if (cols.size() >= colunas.size()) {
						Item item = null;
						item = new Item();
						segmento.addItem(item);
						if (colunas.containsKey(SefazRJCols.NUM)) {
							String value = cols.get(colunas.get(SefazRJCols.NUM));
							if (FiscalCrawlerUtils.isNumericc(value)) {
								item.setOrdem(FiscalCrawlerUtils.corrigeValor(value).longValue());
							}
						}
						if (colunas.containsKey(SefazRJCols.DESCRICAO)) {
							item.setDescricao(FiscalCrawlerUtils.corrigeTexto(cols.get(colunas.get(SefazRJCols.DESCRICAO))));
						}
						if (colunas.containsKey(SefazRJCols.CEST)) {
							Cest.addCEST(cestService, item, cols.get(colunas.get(SefazRJCols.CEST)));

						}
						if (colunas.containsKey(SefazRJCols.MVAINTERNA) && cols.size() > colunas.get(SefazRJCols.MVAINTERNA)) {
							MVA.addMVA(item, cols.get(colunas.get(SefazRJCols.MVAINTERNA)), "OPERAÇÃO INTERNA", "MVAINTERNA");

						}
						if (colunas.containsKey(SefazRJCols.MVA12) && cols.size() > colunas.get(SefazRJCols.MVA12)) {
							MVA.addMVA(item, cols.get(colunas.get(SefazRJCols.MVA12)), "SUJEITA À ALÍQUOTA DE 12%", "MVA12");
						}
						if (colunas.containsKey(SefazRJCols.MVA4) && cols.size() > colunas.get(SefazRJCols.MVA4)) {
							MVA.addMVA(item, cols.get(colunas.get(SefazRJCols.MVA4)), "SUJEITA À ALÍQUOTA DE 4%", "MVA4");
						}
					}
				}

				doc.close();

			}
			regras.save(lei);
			regras.updateUltimaVersao();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void extrairColunas(Map<SefazRJCols, Integer> colunas, Table table) {
		for (int rowIndex = 0; rowIndex < (table.numRows() > 2 ? 2 : table.numRows()); rowIndex++) {
			TableRow row = table.getRow(rowIndex);
			for (int colIndex = 0; colIndex < row.numCells(); colIndex++) {
				TableCell cell = row.getCell(colIndex);
				cell.isMerged();
				String value = cell.getParagraph(0).text();
				if (value != null) {
					for (SefazRJCols el : SefazRJCols.values()) {
						if (value.matches(el.regex)) {
							colunas.put(el, colIndex);
						}
					}
				}

			}
		}
	}

}

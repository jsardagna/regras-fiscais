package com.contaazul.fiscal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Table;
import org.apache.poi.hwpf.usermodel.TableCell;
import org.apache.poi.hwpf.usermodel.TableIterator;
import org.apache.poi.hwpf.usermodel.TableRow;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.contaazul.fiscal.enttiy.mva.SefazRJCols;
import com.contaazul.fiscal.enttiy.mva.SefazRSCols;
import com.contaazul.fiscal.utils.FiscalCrawlerUtils;

public class Teste {

	private static final String MVA_RJ = "anexo_I_livroII.doc";
	private static final String URL_LINK_DATA = "http://www.fazenda.rj.gov.br/sefaz/faces/oracle/webcenter/portalapp/pages/navigation-renderer.jspx?_afrLoop=6152478571557273&datasource=UCMServer%23dDocName%3AWCC217880&_adf.ctrl-state=to5wwxda6_40";

	public static void main(String[] args) throws IOException {

		String link = "Anexo I do Livro II do RICMS/00 - 21-03-2019";
		try {
			Elements elements = FiscalCrawlerUtils.carregaURLNormal(URL_LINK_DATA, "a[href*=\".doc?lve\"]");
			System.out.println(elements.text());
			FiscalCrawlerUtils.extractFile(elements.attr("href"), MVA_RJ, true);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void extraimva_rj() throws IOException, FileNotFoundException {
		File file = FiscalCrawlerUtils.carregaArquivo(MVA_RJ);
		HWPFDocument doc = new HWPFDocument(new FileInputStream(file));

		Range range = doc.getRange();

		TableIterator itr = new TableIterator(range);
		Map<SefazRJCols, Integer> colunas = new HashMap<SefazRJCols, Integer>();

		while (itr.hasNext()) {
			Table table = itr.next();
			extrairColunas(colunas, table);
			for (Entry<SefazRJCols, Integer> keyentry : colunas.entrySet()) {
				System.out.println(keyentry.getValue() + " " + keyentry.getKey());
			}
		}

		doc.close();
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

	public static void mvaRS() {
		final String RS_MVA_FILE = "RS-MVA.html";
		try {
			String cssQuery = "table:contains(CÓDIGO ESPECIFICADOR DA SUBSTITUIÇÃO TRIBUTÁRIA - CEST)";
			File in = FiscalCrawlerUtils.carregaArquivo(RS_MVA_FILE);
			Document doc = Jsoup.parse(in, null);
			Elements tables = doc.select(cssQuery);
			for (Element table : tables) {
				Elements rows = table.select("tr");
				Map<SefazRSCols, Integer> colunas = new HashMap<SefazRSCols, Integer>();
				columns(rows, colunas);
				for (int r = 0; r < rows.size(); r++) {
					Element row = rows.get(r);
					Elements cols = row.select("td");
					if (cols.size() >= colunas.size()) {
						for (Entry<SefazRSCols, Integer> keyentry : colunas.entrySet()) {
							System.out.println(keyentry.getKey() + ": " + cols.get(keyentry.getValue()).text());
						}
					}

				}
			}

		} catch (IOException e) {
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

	public static void ipi() throws IOException {

		final File f = new File("documentos/tipi-1.pdf");
		PDDocument document = PDDocument.load(f);

		PDFTextStripper reader = new PDFTextStripper();
		reader.setParagraphStart("\t");

		int total = document.getPages().getCount();

		for (int i = 1; i < total; i++) {
			reader.setStartPage(i);
			reader.setEndPage(i);
			String lastNCM = "";
			String content = reader.getText(document);
			int index = StringUtils.indexOf(content, "NCM DESCRIÇÃO");
			if (index > 0) {
				String[] paragrafo = StringUtils.substring(content, index).split("\\t");
				for (String string : paragrafo) {
					if (StringUtils.trim(string).startsWith("Ex ")) {
						String[] linhas = string.split("\\n");
						for (String linha : linhas) {
							String clean = StringUtils.trimToNull(StringUtils.remove(StringUtils.remove(linha, "\n"), "\r"));
							String aliquota = StringUtils.substringAfterLast(clean, " ");
							String descricao = StringUtils.substringBeforeLast(clean, " ");
							if (aliquota != null && aliquota.length() < 3) {
								System.out.println(lastNCM + " - " + aliquota + " - " + descricao);
							}
						}
					} else {
						String clean = StringUtils.trimToNull(StringUtils.remove(StringUtils.remove(string, "\n"), "\r"));
						String NCM = StringUtils.substringBefore(clean, " ");
						String aliquota = StringUtils.substringAfterLast(clean, " ");
						String descricao = StringUtils.substringBeforeLast(StringUtils.substringAfter(clean, " "), " ");
						if (clean != null && StringUtils.isNumeric(NCM.subSequence(0, 1)) && aliquota.length() < 3) {
							System.out.println(NCM + " - " + aliquota + " - " + descricao);
							lastNCM = NCM;
						}
					}
				}

			}

		}
		// Closing the document
		document.close();
	}

}

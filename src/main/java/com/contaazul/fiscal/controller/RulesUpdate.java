package com.contaazul.fiscal.controller;

import java.io.File;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.contaazul.fiscal.enttiy.Acordo;
import com.contaazul.fiscal.enttiy.Aliquota;
import com.contaazul.fiscal.enttiy.Cest;
import com.contaazul.fiscal.enttiy.Especificacao;
import com.contaazul.fiscal.enttiy.Item;
import com.contaazul.fiscal.enttiy.Lei;
import com.contaazul.fiscal.enttiy.MVA;
import com.contaazul.fiscal.enttiy.PFC;
import com.contaazul.fiscal.enttiy.Segmento;
import com.contaazul.fiscal.service.CestService;
import com.contaazul.fiscal.service.LeiService;
import com.contaazul.fiscal.utils.Estados;
import com.contaazul.fiscal.utils.FiscalFileUtils;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/fiscal/regras")
public class RulesUpdate {

	final DateTimeFormatter formatterD = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	final NumberFormat numberFormat = NumberFormat.getInstance(Locale.GERMANY);

	@Autowired
	private LeiService regras;

	@Autowired
	CestService cestService;

	@RequestMapping(value = "update", method = RequestMethod.GET)
	public String update() {

		final File dir = new File(FiscalFileUtils.DOCUMENTOS);

		for (File xls : dir.listFiles(FiscalFileUtils.XLSFILTER)) {
			System.out.println(xls.getName());
			Lei lei = regras.findByArquivo(xls.getName());

			if (lei == null) {
				System.out.println(xls.getName() + " Entrou");
				Workbook workbook;
				try {
					workbook = WorkbookFactory.create(xls);

					System.out.println("Workbook has " + workbook.getNumberOfSheets() + " Sheets : ");
					Iterator<Sheet> sheetIterator = workbook.sheetIterator();
					System.out.println("Retrieving Sheets using Iterator");
					lei = new Lei();
					lei.setArquivo(xls.getName());
					lei.setCriacao(LocalDate.now());

					while (sheetIterator.hasNext()) {
						Sheet sheet = sheetIterator.next();
						if (!"Leiaute".equals(sheet.getSheetName())) {
							System.out.println("=> " + sheet.getSheetName());

							DataFormatter dataFormatter = new DataFormatter();

							Segmento segmento = new Segmento();
							segmento.setNome(sheet.getSheetName());
							lei.addSegmento(segmento);
							Iterator<Row> rowIterator = sheet.rowIterator();

							Integer cItem = null;
							Integer cCest = null;
							Integer cDescricao = null;
							Integer cOpInterna = null;
							Map<Integer, String> cAcordos = new LinkedHashMap<Integer, String>();
							LinkedHashMap<Integer, String[]> cAliquota = new LinkedHashMap<Integer, String[]>();
							LinkedHashMap<Integer, String[]> cMVA = new LinkedHashMap<Integer, String[]>();
							LinkedHashMap<Integer, String[]> cPFC = new LinkedHashMap<Integer, String[]>();
							LinkedHashMap<Integer, String[]> cEspecificacaoAliquota = new LinkedHashMap<Integer, String[]>();
							Integer headRow = null;
							Long pfc = 0l, mva = 0l, aliq = 0l;
							while (rowIterator.hasNext()) {
								Row row = rowIterator.next();
								Iterator<Cell> cellIterator = row.cellIterator();
								Item item = null;
								if (headRow != null) {
									item = new Item();
									segmento.addItem(item);
								}
								while (cellIterator.hasNext()) {
									Cell cell = cellIterator.next();
									String cellValue = "";
									if (CellType.FORMULA.equals(cell.getCellType()) && CellType.STRING.equals(cell.getCachedFormulaResultType())) {
										cellValue = cell.getStringCellValue();
									} else if (CellType.FORMULA.equals(cell.getCellType()) && CellType.NUMERIC.equals(cell.getCachedFormulaResultType())) {
										cellValue = "" + cell.getNumericCellValue();
									} else {
										cellValue = dataFormatter.formatCellValue(cell);
									}

									if (!StringUtils.isEmpty(cellValue) && cellValue.matches(".*[a-zA-Z0-9].*")) {

										if (headRow == null && (cellValue.toUpperCase().startsWith("ITEM") || cellValue.toUpperCase().startsWith("CEST"))) {
											headRow = row.getRowNum();
										}

										if (headRow == null) {

											if (cellValue.startsWith("Versão")) {
												segmento.setVersao(StringUtils.chomp(StringUtils.removeStartIgnoreCase(cellValue, "Versão:")).toUpperCase());
											}

											if (lei.getEstado() == null && StringUtils.startsWithIgnoreCase(cellValue, "Unidade Federada Destinatária / Declarante:")) {
												lei.setEstado(Estados.valueOf(StringUtils
														.trim(StringUtils.chomp(StringUtils.removeStartIgnoreCase(cellValue, "Unidade Federada Destinatária / Declarante:")).toUpperCase())));
											}

											if (StringUtils.startsWithIgnoreCase(cellValue, "Produção de efeitos a partir de")) {

												String[] data = StringUtils.split(StringUtils.stripToEmpty(StringUtils.removeStartIgnoreCase(cellValue, "Produção de efeitos a partir de")), "/");
												String value = String.format("%02d/%02d/%4d", Integer.parseInt(data[0]), Integer.parseInt(data[1]), Integer.parseInt(data[2]));
												segmento.setEfeito(LocalDate.parse(value, formatterD));
											}

											if (cellValue.toUpperCase().startsWith("ANEXO") && cellValue.toUpperCase().contains("SEGMENTO")) {
												segmento.setDescricaoSegmento(StringUtils.capitalize(cellValue));
											}

											if (cellValue.toUpperCase().startsWith("OBSERVAÇÕES")) {
												segmento.setObservacoes(StringUtils.capitalize(cellValue));
											}

										} else if (headRow.equals(row.getRowNum())) {

											if (lei.getEfeito() == null || (segmento.getEfeito() != null && lei.getEfeito().isBefore(segmento.getEfeito()))) {
												lei.setEfeito(segmento.getEfeito());
												lei.setVersao(segmento.getVersao());
											}

											if (cItem == null && cellValue.toUpperCase().startsWith("ITEM")) {
												cItem = cell.getColumnIndex();
											} else if (cCest == null && cellValue.toUpperCase().startsWith("CEST")) {
												cCest = cell.getColumnIndex();
											} else if (cDescricao == null && cellValue.toUpperCase().startsWith("DESCRIÇÃO")) {
												cDescricao = cell.getColumnIndex();
											} else if (cOpInterna == null && StringUtils.chop(cellValue).toUpperCase().matches("(?=.*?OP)(?=.*?INTER).*")) {
												cOpInterna = cell.getColumnIndex();
											} else if (cellValue.toUpperCase().matches("(?=.*?ESPECIF)(?=.*?MVA).*")) {
												cEspecificacaoAliquota.put(cell.getColumnIndex(), new String[] { cellValue, "MVA" + mva++ });
											} else if (cellValue.toUpperCase().matches("(?=.*?ESPECIF)(?=.*?PFC).*")) {
												cEspecificacaoAliquota.put(cell.getColumnIndex(), new String[] { cellValue, "PFC" + pfc++ });
											} else if (cellValue.toUpperCase().matches("(?=.*?ESPECIF).*")) {
												cEspecificacaoAliquota.put(cell.getColumnIndex(), new String[] { cellValue, "ALIQ" + aliq++ });
											} else if (StringUtils.startsWithIgnoreCase(StringUtils.stripAccents(cellValue), "MVA")) {
												cMVA.put(cell.getColumnIndex(), new String[] { cellValue, "MVA" + mva });
											} else if (StringUtils.startsWithIgnoreCase(StringUtils.stripAccents(cellValue), "PFC")) { //
												cPFC.put(cell.getColumnIndex(), new String[] { cellValue, "PFC" + pfc });
											} else if (StringUtils.startsWithIgnoreCase(StringUtils.stripAccents(cellValue), "ALIQ")) { //
												cAliquota.put(cell.getColumnIndex(), new String[] { cellValue, "ALIQ" + aliq }); //
											} else {
												cAcordos.put(cell.getColumnIndex(), cellValue);
											}
										} else {
											if (cItem != null && cItem.equals(cell.getColumnIndex())) {
												item.setOrdem(StringUtils.isNumeric(cellValue) ? Long.parseLong(cellValue) : 0L);
											}
											if (cCest.equals(cell.getColumnIndex())) {
												String codigo = corrigeTexto(cellValue);
												Cest cest = cestService.findByCodigo(codigo);
												if (cest == null) {
													cest = new Cest();
													cest.setCodigo(codigo);
												}
												cestService.save(cest);
												item.setCest(cest);
											}
											if (cDescricao.equals(cell.getColumnIndex())) {
												item.setDescricao(corrigeTexto(cellValue));
											}
											if (cOpInterna != null && cOpInterna.equals(cell.getColumnIndex())) {
												item.setOpInterna(corrigeTexto(cellValue));
											}
											if (cAcordos.containsKey(cell.getColumnIndex())) {
												Acordo acordo = new Acordo();
												acordo.setTipo(cAcordos.get(cell.getColumnIndex()));
												acordo.setAcordo(corrigeTexto(cellValue));
												item.addAcordo(acordo);
											}
											if (cAliquota.containsKey(cell.getColumnIndex()) && isNumericc(cellValue)) {
												Aliquota aliquota = new Aliquota();
												aliquota.setDescricao(cAliquota.get(cell.getColumnIndex())[0]);
												aliquota.setTipo(cAliquota.get(cell.getColumnIndex())[1]);
												aliquota.setValor(corrigeValor(cellValue));
												item.addAliquota(aliquota);
											}
											if (cMVA.containsKey(cell.getColumnIndex()) && isNumericc(cellValue)) {
												MVA aliquota = new MVA();
												aliquota.setDescricao(cMVA.get(cell.getColumnIndex())[0]);
												aliquota.setTipo(cMVA.get(cell.getColumnIndex())[1]);
												aliquota.setValor(corrigeValor(cellValue));
												item.addMVA(aliquota);
											}
											if (cPFC.containsKey(cell.getColumnIndex()) && isNumericc(cellValue)) {
												PFC aliquota = new PFC();
												aliquota.setDescricao(cPFC.get(cell.getColumnIndex())[0]);
												aliquota.setTipo(cPFC.get(cell.getColumnIndex())[1]);
												aliquota.setValor(corrigeValor(cellValue));
												item.addPFC(aliquota);
											}
											if (cEspecificacaoAliquota.containsKey(cell.getColumnIndex())) {
												Especificacao especificacao = new Especificacao();
												especificacao.setDescricao(cEspecificacaoAliquota.get(cell.getColumnIndex())[0]);
												especificacao.setTipo(cEspecificacaoAliquota.get(cell.getColumnIndex())[1]);
												especificacao.setEspecificacao(corrigeTexto(cellValue));
												item.addEspecificacao(especificacao);
											}
										}
									}
								}
							}
						}
					}
					regras.save(lei);
					workbook.close();
					Runtime.getRuntime().gc();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		updateVersao();
		return "";
	}

	private String corrigeTexto(String cellValue) {
		return StringUtils.trimToNull(StringUtils.chomp(cellValue).toUpperCase());
	}

	private boolean isNumericc(String cellValue) {
		return StringUtils.isNumeric(cellValue.replaceAll("[^0-9]+", ""));
	}

	private BigDecimal corrigeValor(String cellValue) throws ParseException {
		return BigDecimal.valueOf(numberFormat.parse(cellValue.replaceAll("[^0-9,]+", "")).doubleValue());
	}

	@Transactional
	@RequestMapping(value = "versao", method = RequestMethod.GET)
	public void updateVersao() {
		regras.updateUltimaVersao();
	}

}

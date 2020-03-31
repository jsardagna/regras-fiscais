package com.contaazul.fiscal.controller;

import java.io.File;
import java.io.FileFilter;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.contaazul.fiscal.enttiy.IPI;
import com.contaazul.fiscal.enttiy.LeiIPI;
import com.contaazul.fiscal.enttiy.NCM;
import com.contaazul.fiscal.service.LeiIPIService;
import com.contaazul.fiscal.service.NCMService;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/fiscal/ipi/regras")
public class IPUpdate {

	private static final String DOCUMENTOS = "documentos";
	final DateTimeFormatter formatterD = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	final NumberFormat numberFormat = NumberFormat.getInstance(Locale.GERMANY);

	@Autowired
	private LeiIPIService leiservice;

	@Autowired
	NCMService ncmService;

	private static final FileFilter XLSFILTER = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			return pathname.getName().endsWith("pdf");
		}

	};

	@RequestMapping(value = "update", method = RequestMethod.GET)
	public String update() {

		// Loading an existing document
		final File f = new File(DOCUMENTOS + "/tipi-1.pdf");
		try {
			PDDocument document;
			document = PDDocument.load(f);
			// Instantiate PDFTextStripper class
			PDFTextStripper reader = new PDFTextStripper();
			reader.setParagraphStart("\t");

			int total = document.getPages().getCount();
			// Atualizacao
			reader.setStartPage(1);
			reader.setEndPage(1);
			String content = reader.getText(document);
			int inicio = StringUtils.indexOf(content, "Última atualização") + 20;
			LocalDate date = LocalDate.parse(StringUtils.substring(content, inicio, inicio + 12).trim(), formatterD);
			LeiIPI lei = leiservice.findByAtualizacao(date);
			if (lei != null) {
				return "já processado";
			} else {
				lei = new LeiIPI();
				lei.setCriacao(LocalDate.now());
				lei.setAtualizacao(date);
			}

			for (int i = 1; i < total; i++) {
				reader.setStartPage(i);
				reader.setEndPage(i);
				NCM lastNCM = null;
				content = reader.getText(document);
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
									IPI ipi = new IPI();
									ipi.setNcm(lastNCM);
									if (StringUtils.isNumeric(aliquota)) {
										ipi.setValor(BigDecimal.valueOf(numberFormat.parse(aliquota).doubleValue()));
									} else {
										ipi.setValor(BigDecimal.ZERO);
										ipi.setExcessao(aliquota);
									}
									ipi.setExcessao(descricao);
									lei.addItem(ipi);
								}
							}
						} else {
							String clean = StringUtils.trimToNull(StringUtils.remove(StringUtils.remove(string, "\n"), "\r"));
							String codigo = StringUtils.substringBefore(clean, " ");
							String aliquota = StringUtils.substringAfterLast(clean, " ");
							String descricao = StringUtils.substringBeforeLast(StringUtils.substringAfter(clean, " "), " ");
							if (clean != null && StringUtils.isNumeric(codigo.subSequence(0, 1)) && aliquota.length() < 3) {
								NCM ncm = ncmService.findByCodigo(codigo);
								if (ncm == null) {
									ncm = new NCM();
									ncm.setCodigo(codigo);
									ncm.setDescricao(descricao);
									ncmService.save(ncm);
								}
								IPI ipi = new IPI();
								ipi.setNcm(ncm);
								if (StringUtils.isNumeric(aliquota)) {
									ipi.setValor(BigDecimal.valueOf(numberFormat.parse(aliquota).doubleValue()));
								} else {
									ipi.setValor(BigDecimal.ZERO);
									ipi.setExcessao(aliquota);
								}
								lei.addItem(ipi);
								lastNCM = ncm;
							}
						}
					}

				}

			}
			leiservice.save(lei);
			document.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

}

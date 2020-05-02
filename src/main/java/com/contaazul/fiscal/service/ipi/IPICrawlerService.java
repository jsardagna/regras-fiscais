package com.contaazul.fiscal.service.ipi;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.contaazul.fiscal.enttiy.NCM;
import com.contaazul.fiscal.enttiy.ipi.IPI;
import com.contaazul.fiscal.enttiy.ipi.LeiIPI;
import com.contaazul.fiscal.utils.FiscalCrawlerUtils;

@Service
public class IPICrawlerService {

	private static final String URL_IPI = "http://receita.economia.gov.br/acesso-rapido/legislacao/documentos-e-arquivos/tipi-1.pdf";

	@Autowired
	private LeiIPIService leiservice;

	@Autowired
	NCMService ncmService;

	private static final String TIPI_1_PDF = "tipi-1.pdf";

	private String cleanLine(String linha) {
		return StringUtils.trimToNull(StringUtils.remove(StringUtils.remove(linha, "\n"), "\r"));
	}

	public String updateFileAndRules() {
		// Utilize Arquivo
		updateArquivoIPI();
		// Loading an existing document
		final File f = FiscalCrawlerUtils.carregaArquivo(TIPI_1_PDF);
		PDDocument document = null;
		try {
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
			LocalDate date = LocalDate.parse(StringUtils.substring(content, inicio, inicio + 12).trim(), IPI.formatterD);
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
						// Ser a Linha for de excessão
						if (StringUtils.trim(string).startsWith("Ex ")) {
							String[] linhas = string.split("\\n");
							for (String linha : linhas) {
								String clean = cleanLine(linha);
								String aliquota = StringUtils.substringAfterLast(clean, " ");
								String descricao = StringUtils.substringBeforeLast(StringUtils.substringAfter(clean, " "), " ");
								if (aliquota != null && aliquota.length() < 3) {
									IPI ipi = new IPI();
									ipi.setNcm(lastNCM);
									ipi.corrigeValor(aliquota);
									ipi.setExcessao(descricao);
									lei.addItem(ipi);
								}
							}
						} else {
							String clean = cleanLine(string);
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
								ipi.corrigeValor(aliquota);
								lei.addItem(ipi);
								lastNCM = ncm;
							}
						}
					}

				}

			}
			leiservice.save(lei);
			updateVersao();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (document != null) {
				try {
					document.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return "Concluido";
	}

	public void updateVersao() {
		leiservice.updateUltimaVersao();
	}

	public String updateArquivoIPI() {
		FiscalCrawlerUtils.extractFile(URL_IPI, TIPI_1_PDF, true);
		return "Concluido";
	}

}

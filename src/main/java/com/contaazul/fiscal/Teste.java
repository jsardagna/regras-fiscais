package com.contaazul.fiscal;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class Teste {

	public static void main(String[] args) throws IOException {
		// Loading an existing document
		final File f = new File("documentos/tipi-1.pdf");
		PDDocument document = PDDocument.load(f);

		// Instantiate PDFTextStripper class
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

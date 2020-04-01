package com.contaazul.fiscal.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class FiscalFileUtils {

	public static final String DOCUMENTOS = "documentos";

	public static void extractFile(String url, String fileName, boolean replace) {
		try {
			System.out.println("carregando " + fileName);
			final File f = new File(DOCUMENTOS + "/" + fileName);
			if (replace || !f.exists()) {
				byte[] bytes = SSLHelper.getConnection(url).header("Accept-Encoding", "gzip, deflate").userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
						.ignoreContentType(true).maxBodySize(0).timeout(60 * 60 * 1000).execute().bodyAsBytes();

				System.out.println("salvando " + fileName);
				FileUtils.writeByteArrayToFile(f, bytes);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static File carregaArquivo(String file) {
		return new File(FiscalFileUtils.DOCUMENTOS + "/" + file);
	}

	public static final FileFilter XLSFILTER = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			return pathname.getName().endsWith("xlsx");
		}

	};

}

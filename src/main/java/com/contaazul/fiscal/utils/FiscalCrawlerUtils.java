package com.contaazul.fiscal.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class FiscalCrawlerUtils {

	public static final NumberFormat numberFormat = NumberFormat.getInstance(Locale.GERMANY);

	public static final String DOCUMENTOS = "documentos";

	public static final DateTimeFormatter formatterD = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	public static final DateTimeFormatter formatterOLD = DateTimeFormatter.ofPattern("dd-MM-yyyy");

	private static final Pattern datePattern = Pattern.compile("(\\d{2}\\/\\d{2}\\/\\d{4})");

	public static LocalDate extractFile(String url, String fileName, boolean replace) {
		try {
			System.out.println("carregando " + fileName);
			final File f = new File(DOCUMENTOS + "/" + fileName);
			final File fnew = new File(DOCUMENTOS + "/new_" + fileName);
			final File fold = new File(DOCUMENTOS + "/" + formatterOLD.format(LocalDate.now()) + " - " + fileName);
			Long length = 0L;
			if (replace || !f.exists()) {
				if (f.exists()) {
					length = f.length();
					f.renameTo(fold);
				}
				byte[] bytes = SSLHelper.getConnection(url).header("Accept-Encoding", "gzip, deflate").userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
						.ignoreContentType(true).maxBodySize(0).timeout(60 * 60 * 60 * 1000).execute().bodyAsBytes();

				System.out.println("salvando " + f);
				FileUtils.writeByteArrayToFile(replace ? fnew : f, bytes);
				if (replace) {
					if (fnew.length() == length) {
						fold.renameTo(f);
						fnew.delete();
					} else {
						fnew.renameTo(f);
						return LocalDate.now();
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static LocalDate extractDate(Element doc, String pattern) {
		Matcher matcher = datePattern.matcher(doc.select(pattern).get(0).text());
		matcher.find();
		return LocalDate.parse(matcher.group(), FiscalCrawlerUtils.formatterD);
	}

	public static File carregaArquivo(String file) {
		return new File(FiscalCrawlerUtils.DOCUMENTOS + "/" + file);
	}

	public static final FileFilter XLSFILTER = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			return pathname.getName().endsWith("xlsx");
		}

	};

	public static Elements carregaURL(String url, String cssQuery) throws IOException {
		Document doc = SSLHelper.getConnection(url).ignoreContentType(true).get();
		System.out.println(doc.text());
		Elements elements = doc.select(cssQuery);
		return elements;
	}

	public static Elements carregaURLNormal(String url, String cssQuery) throws IOException {
		Document doc = SSLHelper.getConnection(url).header("Accept-Encoding", "gzip, deflate").userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0").get();
		System.out.println(doc.text());
		Elements elements = doc.select(cssQuery);
		return elements;
	}

	public static String arabicToRoman(int number) {
		if ((number <= 0) || (number > 4000)) {
			throw new IllegalArgumentException(number + " is not in range (0,4000]");
		}

		List<RomanNumeral> romanNumerals = RomanNumeral.getReverseSortedValues();

		int i = 0;
		StringBuilder sb = new StringBuilder();

		while ((number > 0) && (i < romanNumerals.size())) {
			RomanNumeral currentSymbol = romanNumerals.get(i);
			if (currentSymbol.getValue() <= number) {
				sb.append(currentSymbol.name());
				number -= currentSymbol.getValue();
			} else {
				i++;
			}
		}

		return sb.toString();
	}

	public static String corrigeTexto(String cellValue) {
		return StringUtils.trimToNull(StringUtils.chomp(cellValue).toUpperCase());
	}

	public static boolean isNumericc(String cellValue) {
		return cellValue != null && StringUtils.isNumeric(cellValue.replaceAll("[^0-9]+", ""));
	}

	public static BigDecimal corrigeValor(String cellValue) throws ParseException {
		return BigDecimal.valueOf(numberFormat.parse(cellValue.replaceAll("[^0-9,]+", "")).doubleValue());
	}
}

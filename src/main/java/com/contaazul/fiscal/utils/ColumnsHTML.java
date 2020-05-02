package com.contaazul.fiscal.utils;

import org.jsoup.select.Elements;

public class ColumnsHTML extends Columns {

	private Elements el = null;

	public ColumnsHTML(Elements el) {
		this.el = el;
	}

	@Override
	public String get(int col) {
		return el.get(col).text();
	}

	@Override
	public int size() {
		return el.size();
	}

};

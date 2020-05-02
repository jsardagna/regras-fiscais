package com.contaazul.fiscal.utils;

import org.apache.poi.hwpf.usermodel.TableCell;
import org.apache.poi.hwpf.usermodel.TableRow;

public class ColumnsDoc extends Columns {

	private TableRow row;

	public ColumnsDoc(TableRow row) {
		this.row = row;
	}

	@Override
	public String get(int col) {
		TableCell cell = this.row.getCell(col);
		return cell.getParagraph(0).text();
	}

	@Override
	public int size() {
		return row.numCells();
	}

};

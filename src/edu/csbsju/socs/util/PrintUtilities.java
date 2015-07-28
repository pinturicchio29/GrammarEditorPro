/* Copyright (c) 2003, Carl Burch. License information is located in the
 * edu.csbsju.socs.grammar.Main source code and at
 * www.cburch.com/proj/grammar/. */

package edu.csbsju.socs.util;

import java.awt.*;
import javax.swing.*;
import java.awt.print.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class PrintUtilities {
	private static final int TAB_WIDTH = 8;
	private static final Font TEXT_FONT
		= new Font("Monospaced", Font.PLAIN, 12);

	private static class ComponentPrinter implements Printable {
		private Component componentToBePrinted;

		public ComponentPrinter(Component componentToBePrinted) {
			this.componentToBePrinted = componentToBePrinted;
		}
		public int print(Graphics g, PageFormat format, int pageIndex) {
			if (pageIndex > 0) {
				return NO_SUCH_PAGE;
			} else {
				Graphics2D g2d = (Graphics2D)g;
				g2d.translate(format.getImageableX(),
					format.getImageableY());
				Dimension dims = componentToBePrinted.getSize();
				double scale = Math.min(1.0, Math.min(
					format.getImageableWidth() / 1.05 / dims.getWidth(),
					format.getImageableHeight() / 1.05 / dims.getHeight()));
				g2d.scale(scale, scale);

				disableDoubleBuffering(componentToBePrinted);
				componentToBePrinted.paint(g2d);
				enableDoubleBuffering(componentToBePrinted);

				return PAGE_EXISTS;
			}
		}

		/** The speed and quality of printing suffers dramatically if
		*  any of the containers have double buffering turned on.
		*  So this turns if off globally.
		*  @see enableDoubleBuffering
		*/
		private void disableDoubleBuffering(Component c) {
			RepaintManager currentManager = RepaintManager.currentManager(c);
			currentManager.setDoubleBufferingEnabled(false);
		}

		/** Re-enables double buffering globally. */
		private void enableDoubleBuffering(Component c) {
			RepaintManager currentManager = RepaintManager.currentManager(c);
			currentManager.setDoubleBufferingEnabled(true);
		}
	}

	private static class TextPrinter implements Printable {
		private ArrayList lines;
		private int lines_per_page;
		private int num_pages;
		private boolean computed = false;

		public TextPrinter(String text) {
			// break file into lines
			StringTokenizer toks = new StringTokenizer(text, "\n", true);
			String empty = "";
			String last = null;
			lines = new ArrayList(toks.countTokens());
			while(toks.hasMoreTokens()) {
				String s = toks.nextToken();
				if(s.equals("\n")) {
					if(last != null && last.equals("\n")) lines.add(empty);
				} else {
					lines.add(s);
				}
				last = s;
			}

			// remove any empty lines at end of file
			for(int pos = lines.size() - 1; pos >= 0; pos--) {
				last = (String) lines.get(pos);
				if(!last.trim().equals("")) break;
				lines.remove(pos);
			}
		}
				
		public int print(Graphics g, PageFormat format, int pageIndex) {
			if(!computed) compute(g, format);
			if(pageIndex >= num_pages) return Printable.NO_SUCH_PAGE;

			g.setColor(Color.black);
			g.setFont(TEXT_FONT);
			FontMetrics fm = g.getFontMetrics();
			int line_height = fm.getHeight();
			int x = (int) Math.ceil(format.getImageableX());
			int y = (int) Math.ceil(format.getImageableY())
				+ fm.getAscent();
			int base = lines_per_page * pageIndex;
			for(int row = 0; row < lines_per_page
					&& base + row < lines.size(); row++) {
				// g.setFont(TEXT_FONT);
				String line = (String) lines.get(base + row);
				line = expandTabs(line);
				if(!line.trim().equals("")) g.drawString(line, x, y);
				y += line_height;
			}
			return Printable.PAGE_EXISTS;
		}

		private void compute(Graphics g, PageFormat format) {
			computed = true;
			g.setFont(TEXT_FONT);
			FontMetrics fm = g.getFontMetrics();
			int line_height = fm.getHeight();
			int leading = fm.getLeading();
			int page_height = (int) format.getImageableHeight();
			lines_per_page = (page_height - leading) / line_height;
			num_pages = (lines.size() + lines_per_page - 1)
				/ lines_per_page;
		}

		private String expandTabs(String base) {
			String ret = "";
			int pos = 0;
			while(true) {
				int next = base.indexOf('\t', pos);
				if(next < 0) break;
				String tab = " ";
				int spaces = (next + TAB_WIDTH - 1 - pos) % TAB_WIDTH;
				for(int i = 0; i < spaces; i++) tab += " ";
				ret = ret + tab + base.substring(pos, next);
				pos = next + 1;
			}
			ret = ret + base.substring(pos);
			return ret;
		}
	}

	public static void printText(String text)
			throws PrinterException {
		PrinterJob job = PrinterJob.getPrinterJob();
		job.setPrintable(new TextPrinter(text));
		if(job.printDialog()) job.print();
	}

	public static void printComponent(Component c)
			throws PrinterException {
		PrinterJob job = PrinterJob.getPrinterJob();
		job.setPrintable(new ComponentPrinter(c));
		if(job.printDialog()) job.print();
	}
}

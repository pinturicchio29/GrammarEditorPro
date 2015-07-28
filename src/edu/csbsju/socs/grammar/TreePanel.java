/* Copyright (c) 2003, Carl Burch. License information is located in the
 * edu.csbsju.socs.grammar.Main source code and at
 * www.cburch.com/proj/grammar/. */

package edu.csbsju.socs.grammar;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.*;

class TreePanel extends JPanel implements ActionListener{
	private static final int VERT_GAP = 55;
	private static final int HORZ_GAP = 30;
	private static final int LABEL_BORDER = 2;
	private static final int BORDER = 20;
	private static final int NO_CHILD_X = 3;

	private static class TreeData {
		int x; // x-coordinate of label's center
		int y;
		int label_width;
		int label_height;
		int label_top_offs;
		int label_base_offs;
		Tree data;
		TreeData parent = null;
		TreeData child = null;
		TreeData sibling = null;

		int width; // these are for during the auto-arrange only
		int height;

		TreeData(Tree data, int x, int y,
				int label_width, int label_height,
				int label_top_offs, int label_base_offs,
				int width, int height) {
			this.data = data;
			this.x = x;
			this.y = y;
			this.label_width = label_width;
			this.label_height = label_height;
			this.label_top_offs = label_top_offs;
			this.label_base_offs = label_base_offs;
			this.width = width;
			this.height = height;
		}
	}

	private Tree tree = null;
	private Font font = new Font("Dialog", Font.PLAIN, 20);
	private ArrayList tree_data = new ArrayList();

	public TreePanel() { this(null); }
	public TreePanel(Tree tree) {
		setBackground(Color.white);
		if(tree != null) setTree(tree);
		setPreferredSize(new Dimension(400, 400));
		this.setLayout(new BorderLayout());
	}

	public void setTree(Tree value) {
		if(value == null) value = new Tree(Strings.get("notInLanguageMsg"));
		this.tree = value;
		this.tree_data = null;
		autoArrange();
	}

	public Image getImage() {
		Graphics g = getGraphics();
		if(tree == null || g == null) return null;
		if(tree_data == null) autoArrange(g);
		if(tree_data == null) return null;

		Dimension dims = getPreferredSize();
		int width = (int) Math.ceil(dims.getWidth());
		int height = (int) Math.ceil(dims.getHeight());
		Image ret = createImage(width, height);
		g = ret.getGraphics();
		if(g == null) return null;
		g.setColor(Color.white);
		g.fillRect(0, 0, width, height);
		doPaint(g, BORDER, BORDER);
		return ret;
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		if(tree == null) return;
		if(tree_data == null) autoArrange(g);
		if(tree_data == null) return;

		Dimension size = getSize();
		Dimension pref = getPreferredSize();
		int x_offs = BORDER + Math.max(0, (size.width - pref.width) / 2);
		int y_offs = BORDER + Math.max(0, (size.height - pref.height) / 2);
		doPaint(g, x_offs, y_offs);
	}
	private void doPaint(Graphics g, int x_offs, int y_offs) {
		// draw connecting lines
		g.setColor(Color.gray);
		Iterator it = tree_data.iterator();
		while(it.hasNext()) {
			TreeData nd = (TreeData) it.next();

			// draw line to parent
			TreeData pd = nd.parent;
			if(pd != null) {
				g.drawLine(x_offs + nd.x, y_offs + nd.y,
					x_offs + pd.x, y_offs + pd.y);
			}

			// if appropriate, draw X marking non-leaf with no children
			Tree[] nch = nd.data.getChildren();
			if(nch == null || nch.length == 0) {
				if(!nd.data.isLeaf()) {
					int endx = x_offs + nd.x;
					int endy = y_offs + nd.y + VERT_GAP / 2;
					g.drawLine(x_offs + nd.x, y_offs + nd.y,
						endx, endy);
					g.drawLine(endx - NO_CHILD_X, endy - NO_CHILD_X,
						endx + NO_CHILD_X, endy + NO_CHILD_X);
					g.drawLine(endx - NO_CHILD_X, endy + NO_CHILD_X,
						endx + NO_CHILD_X, endy - NO_CHILD_X);
				}
			}
		}

		// draw labels
		g.setFont(font);
		it = tree_data.iterator();
		String StringaTemp = "";
		int i = 0;
		while(it.hasNext()) {
			TreeData nd = (TreeData) it.next();

			int x = x_offs + nd.x - nd.label_width / 2;
			int y = y_offs + nd.y + nd.label_top_offs;
			g.setColor(Color.white);
			g.fillRect(x - LABEL_BORDER, y - LABEL_BORDER,
				nd.label_width + 2 * LABEL_BORDER,
				nd.label_height + 2 * LABEL_BORDER);
			y = y_offs + nd.y + nd.label_base_offs;
			g.setColor(Color.white);
			g.setColor(Color.blue);
			String tempLabel = nd.data.getData().toString().replaceAll("\"", ""); 
			labelTree tempLab = new labelTree(tempLabel, x, y);
			g.drawString(tempLabel, x, y);
			
		}
		

	}

	public void autoArrange() {
		autoArrange(getGraphics());
		repaint();
	}
	private void autoArrange(Graphics g) {
		if(g == null) return;
		FontMetrics fm = getFontMetrics(new Font("Dialog", Font.PLAIN, 20));
		if(fm == null) return;

		tree_data = new ArrayList();
		g.setFont(font);
		TreeData data = autoArrangeSub(tree, 0, 0, fm, g);
		setPreferredSize(new Dimension(data.width + 2 * BORDER,
			data.height + 2 * BORDER));
		invalidate();
	}
	private TreeData autoArrangeSub(Tree t, int x, int y,
			FontMetrics fm, Graphics g) {
		Tree[] children = t.getChildren();

		LineMetrics lm = fm.getLineMetrics(t.getData().toString(), g);
		int label_width = fm.stringWidth(t.getData().toString());
		int label_height = (int) Math.ceil(lm.getAscent() + lm.getDescent());
		int label_x = x + label_width / 2;
		int label_y = y + fm.getAscent() / 2;
		int label_base_offs = (y + fm.getAscent()) - label_y;
		int label_top_offs = (int) Math.round(
			(label_y + label_base_offs - lm.getAscent()) - label_y);
		int width = label_width;
		int height = label_height;
		TreeData child = null;

		if(children.length == 0) {
			if(!t.isLeaf()) {
				height = Math.max(height, VERT_GAP / 2 + NO_CHILD_X);
				width = Math.max(width, 2 * NO_CHILD_X);
			}
		} else {
			int xpos = x;
			y += VERT_GAP;
			int max_height = 0;
			TreeData last = null;
			for(int i = 0; i < children.length; i++) {
				TreeData data = autoArrangeSub(children[i], xpos, y, fm, g);
				if(last == null) child = data;
				else last.sibling = data;
				last = data;
				xpos += data.width + HORZ_GAP;
				max_height = Math.max(max_height, data.height);
			}
			xpos -= HORZ_GAP;
			int center = (child.x + last.x) / 2;
			if(center >= label_x) {
				label_x = center;
			} else { // label is wider than children
				int offs = label_x - center;
				for(TreeData n = child; n != null; n = n.sibling) {
					translateTree(n, offs, 0);
				}
				xpos += offs;
			}
			width = Math.max(label_x + label_width / 2 - x, xpos - x);
			height = Math.max(label_height, VERT_GAP + max_height);
		}

		TreeData ret = new TreeData(t, label_x, label_y,
			label_width, label_height, label_top_offs, label_base_offs,
			width, height);
		ret.child = child;
		for(TreeData n = child; n != null; n = n.sibling) {
			n.parent = ret;
		}
		tree_data.add(ret);
		return ret;
	}
	private void translateTree(TreeData t, int x_offs, int y_offs) {
		t.x += x_offs;
		t.y += y_offs;
		for(TreeData n = t.child; n != null; n = n.sibling) {
			translateTree(n, x_offs, y_offs);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource() instanceof TreePanelToggleButton)
		{
			
		}
	}

}

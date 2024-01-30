package de.matul.jgrep;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.font.TextAttribute;
import java.text.AttributedString;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.Border;

@SuppressWarnings("serial")
public class HitCellRenderer extends JComponent implements ListCellRenderer<Hit> {

	private final Options options;
	Hit hit;
	
	public HitCellRenderer(Options options) {
		this.options = options;
		setFont(new Font("Monospaced", Font.PLAIN, 12));
	}

	public Component getListCellRendererComponent(JList list, Hit value, int index, boolean isSelected, boolean cellHasFocus) {
		
		UIDefaults uiTable = UIManager.getLookAndFeelDefaults();
		hit = (Hit)value;
		
		if (isSelected) {
			setBackground(list.getSelectionBackground());
		} else {
			setBackground(list.getBackground());
		}
		
		setEnabled(list.isEnabled());

		Border border = null;
		if (cellHasFocus) {
			if (isSelected) {
				border = uiTable.getBorder("List.focusSelectedCellHighlightBorder");
			}
			if (border == null) {
				border = uiTable.getBorder("List.focusCellHighlightBorder");
			}
		} else {
			border = null;
		}
		
		setBorder(border);
		
		FontMetrics metrics = getFontMetrics(getFont());
		int width = SwingUtilities.computeStringWidth(metrics, hit.toString());
		
		Dimension d = new Dimension(width+10, metrics.getHeight() + 5);
		setMinimumSize(d);
		setPreferredSize(d);
		
		//setText(hit.toString());
		
		return this;

	}
	
	public void paintComponent(Graphics g) {
		g.setColor(getBackground());
		g.setFont(getFont());
		g.fillRect(0, 0, getWidth(), getHeight());
		
		AttributedString attributedString = hit.getAttributedString(options.onlyMatching);
		attributedString.addAttribute(TextAttribute.FONT, getFont());
		
		g.setColor(getForeground());
		g.drawString(attributedString.getIterator(), 5, getFontMetrics(getFont()).getHeight());
	}

}

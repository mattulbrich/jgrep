package de.matul.jgrep;
import java.awt.*;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * A class illustrating running line number count on JTextPane. Nothing is
 * painted on the pane itself, but a separate JPanel handles painting the line
 * numbers.<br>
 * 
 * @author Daniel Sj�blom<br>
 *         Created on Mar 3, 2004<br>
 *         Copyright (c) 2004<br>
 * @version 1.0<br>
 */
@SuppressWarnings("serial")
public class LineNrPane extends JPanel {
	// for this simple experiment, we keep the pane + scrollpane as members.
	private JTextArea pane;
	private JScrollPane scrollPane;
	private LineNr linenr;
	
	public LineNrPane() {
		super();
		linenr = new LineNr();
		//linenr.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
		
		pane = new JTextArea()
		// we need to override paint so that the
		// linenumbers stay in sync
		{
			public void paint(Graphics g) {
				super.paint(g);
				linenr.repaint();
			}
		};
		scrollPane = new JScrollPane(pane);
		
		setLayout(new BorderLayout());
		add(linenr, BorderLayout.WEST);
		add(scrollPane, BorderLayout.CENTER);
		
		linenr.setMinimumSize(new Dimension(40,30));
		linenr.setPreferredSize(new Dimension(40,30));
	}

	private class LineNr extends JComponent {
		public void paint(Graphics g) {
			super.paint(g);

			Graphics2D g2 = (Graphics2D) g;
			RenderingHints rh = new RenderingHints(
					RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 g2.setRenderingHints(rh);
			int fontHeight = g.getFontMetrics(pane.getFont()).getHeight(); // font height
			Point viewPosition = scrollPane.getViewport().getViewPosition();

			int start = viewPosition.y / fontHeight; // pane.viewToModel(viewPosition); // starting pos in document

			int end = (viewPosition.y + scrollPane.getHeight()) / fontHeight; // end pos in doc

			int offset = viewPosition.y % fontHeight;

			// System.out.println(start + " " + end + " " + offset);

			g.setFont(new Font("Monospaced", Font.PLAIN, 12));
			for (int line = start, y = -offset-2; line <= end+1; line++, y += fontHeight) {
				g.drawString(String.format("%4d", line), 0, y);
			}

		}
	}

	// test main
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		final LineNrPane nr = new LineNrPane();
		frame.getContentPane().add(nr);
		frame.setSize(new Dimension(400, 400));
		frame.setVisible(true);
	}

	public JTextArea getPane() {
		return pane;
	}
}

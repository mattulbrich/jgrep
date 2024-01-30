package de.matul.jgrep;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

import de.matul.util.PreferenceSaver;

@SuppressWarnings("serial")
public class JGrepFrame extends JFrame {

	private static final Color HIGHLIGHT_COLOR = new Color(255, 200, 200);
	private static final Color CURRENT_LINE_COLOR = new Color(200,200,255);
	
	private JTextArea content;
	private String shownFile;
	private JList<Hit> hits;
	private HighlightPainter highlightpainter;
	private Object curlineTag = null;
	private HighlightPainter currentLinePainter;
	private final JGrep jgrep;
	private JProgressBar progress;
	private JLabel fileNameLabel;
	private final PreferenceSaver prefSaver =
			new PreferenceSaver(Preferences.userNodeForPackage(JGrepFrame.class));
	private JButton newCmd;

	public JGrepFrame(JGrep jgrep) throws Exception {
		super("JGrep");
		this.jgrep = jgrep;
		init();
		hits.setModel(jgrep.getHitListModel());

		prefSaver.load(this);
	}
	
	private void init() throws Exception {
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		split.setDividerLocation(400);
		split.setName("horizSplit");
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(split, BorderLayout.CENTER);
		{
			LineNrPane linenrPane = new LineNrPane();
			content = linenrPane.getPane();
			content.setFont(new Font("Monospaced", Font.PLAIN, 12));
			content.setEditable(false);
			//content.setEditorKit(new NoWrapEditorKit());
			{
				content.setWrapStyleWord(false);
				DefaultHighlighter highlight = new DefaultHighlighter();
				highlight.setDrawsLayeredHighlights(false);
				content.setHighlighter(highlight);
				currentLinePainter = new DefaultHighlighter.DefaultHighlightPainter(CURRENT_LINE_COLOR);
				highlightpainter = new DefaultHighlighter.DefaultHighlightPainter(HIGHLIGHT_COLOR);
				curlineTag = highlight.addHighlight(0, 0, currentLinePainter);
			}
			split.setTopComponent(linenrPane);
			split.setResizeWeight(1);
		}
		{
			JScrollPane hitScroll = new JScrollPane();
			hits = new JList<Hit>();
			{
				hits.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				hits.setCellRenderer(new HitCellRenderer(jgrep.getOptions()));
				hits.addListSelectionListener(new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						selectHit(hits.getSelectedIndex());
					}});
			}
			hitScroll.setViewportView(hits);
			split.setBottomComponent(hitScroll);
		}
		{
			JToolBar jtb = new JToolBar();
			getContentPane().add(jtb, BorderLayout.NORTH);
			{
				JButton again = new JButton("run again");
				jtb.add(again);
				again.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						runAgain();
					}
				});
			}
			{
				newCmd = new JButton("new pattern");
				jtb.add(newCmd);
				newCmd.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						newPatternOnSameFiles();
					}
				});
			}
			{
				JButton stop = new JButton("stop");
				jtb.add(stop);
				stop.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						jgrep.interrupt();
					}
				});
			}
			{
				JButton edit = new JButton("edit");
				if(jgrep.getOptions().editor == null) {
					edit.setEnabled(false);
					edit.setToolTipText("No editor configured.");
				} else {
					edit.setToolTipText("Open this file in external editor");
				}
				jtb.add(edit);
				edit.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						openEditor();
					}
				});
			}
			{
				JButton quit = new JButton("quit");
				jtb.add(quit);
				quit.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						savePreferences();
						System.exit(0);
					}
				});
			}
		}
		{
			JPanel status = new JPanel(new BorderLayout());
			status.setBorder(new EmptyBorder(5, 5, 5, 5));
			getContentPane().add(status, BorderLayout.SOUTH);
			{
				fileNameLabel = new JLabel();
				status.add(fileNameLabel, BorderLayout.WEST);
			}
			{
				progress = new JProgressBar();
				status.add(progress, BorderLayout.EAST);
			}
		}
		
		setSize(800, 600);
		setName("mainWindow");
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				savePreferences();
			}
		});
		
/*		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				System.out.println(getSize());
			}
		});*/
	}
	
	private void savePreferences() {
        try {
            prefSaver.save(this);
        } catch (BackingStoreException e) {
            // it is not tragic if the preferences cannot be stored.
            e.printStackTrace();
        }
    }

	private void setContent(String file) throws BadLocationException, IOException {
		if(file == null) {
			content.setText("");
			curlineTag = null;
		} else if(!file.equals(shownFile)) {
			// TODO Charset
			BufferedReader br = new BufferedReader(new FileReader(file));
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while(line != null) {
				sb.append(line).append("\n");
				line = br.readLine();
			}
			content.setText(sb.toString());
			
			Highlighter highlighter = content.getHighlighter();
			highlighter.removeAllHighlights();
			curlineTag = highlighter.addHighlight(0, 0, currentLinePainter);
			for (Hit hit : jgrep.getHitList()) {
				if(hit.getFilename().equals(file)) {
					int start = hit.getLineOffset() + hit.getPos();
					int end = start + hit.getLength();
					highlighter.addHighlight(start, end, highlightpainter);
				}
			}
		}
		
		shownFile = file;
		fileNameLabel.setText(file);
	}

	private void selectHit(int index) {
		if(index < 0)
			return;
		
		final Hit hit = jgrep.getHitList().get(index);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					setContent(hit.getFilename());
					Highlighter highlight = content.getHighlighter();
					highlight.changeHighlight(curlineTag, hit.getLineOffset(), hit.getLineOffset() + hit.getLine().length());
					content.scrollRectToVisible(content.modelToView(hit.getLineOffset()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}});
	}
	
	protected void openEditor()  {
		try {
			int sel = hits.getSelectedIndex();
			if(sel == -1)
				return;
			final Hit hit = jgrep.getHitList().get(sel);
			String command = jgrep.getOptions().editor.replace("%f", hit.getFilename()).
					replace("%l", Integer.toString(hit.getLineno()));
			Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void runAgain() {
		try {
			doGrep();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void newPatternOnSameFiles() {
		
		String pre;
		String selectedText = content.getSelectedText();
		if(selectedText != null && !selectedText.isEmpty())
			pre = selectedText;
		else
			pre = jgrep.getOptions().regex;
		
		String cmd = JOptionPane.showInputDialog("Enter grep command", pre);
		
		if(cmd == null)
			return;
		
		try {
			jgrep.getOptions().regex = cmd;
			doGrep();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void doGrep() throws IOException, BadLocationException {
		setContent(null);
		jgrep.doGrep(this);		
	}

	public void setRunning(boolean b) {
		progress.setIndeterminate(b);
		newCmd.setEnabled(!b);
	}

}

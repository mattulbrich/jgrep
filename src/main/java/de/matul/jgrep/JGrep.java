package de.matul.jgrep;

import picocli.CommandLine;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.text.BadLocationException;

public class JGrep {

	public final static String DEFAULT_EDITOR;
	static {
		String editor = System.getProperty("jgrep.editor");
		if (editor == null) {
			editor = System.getProperty("GUI_EDITOR");
		}
		DEFAULT_EDITOR = editor;
	}

	private Options options = new Options();
	private HitListModel grepResults = new HitListModel();
	private SwingWorker<Void, Hit> worker;

	public static void main(String[] args) {
		JGrep jgrep = new JGrep();
		jgrep.parseArgs(args);
		if (args.length == 0) {
			System.exit(0);
		}
		SwingUtilities.invokeLater(() -> {
            try {
                mainUI(jgrep);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
	}

	private static void mainUI(JGrep jgrep) throws Exception {
		JGrepFrame jgrepFrame = new JGrepFrame(jgrep);
		jgrepFrame.setVisible(true);
		jgrepFrame.doGrep();
	}

	private void parseArgs(String[] args) {
		CommandLine cl = new CommandLine(options);
		if (args.length == 0) {
			cl.usage(System.out);
		} else {
			cl.parseArgs(args);
		}
	}

	public void doGrep(final JGrepFrame frame) throws IOException, BadLocationException {
		
		assert options != null;

		if(worker != null) {
			throw new IllegalStateException("There is a process running");
		}
		
		if(options.regex == null) {
			throw new IllegalArgumentException("No regular expression given");
		}
		
		if(options.files == null || options.files.isEmpty()) {
			if(options.recursive) {
				options.files = Collections.singletonList(".");
			} else {
				throw new IllegalArgumentException("No files to search in given");
			}
		}
		
		String R = options.regex;
		if(options.fixed) {
			R = "\\Q" + R.replace("\\E", "\\E\\\\E\\Q") + "\\E";
		}
		final Pattern pattern = Pattern.compile(R, options.ignoreCase ? Pattern.CASE_INSENSITIVE : 0);
		
		grepResults.clear();

		frame.setTitle("JGrep – " + options.regex);
		frame.setRunning(true);

		AtomicInteger counter = new AtomicInteger();

		this.worker = new SwingWorker<Void, Hit>() {
			@Override
			protected Void doInBackground() throws Exception {
				for(String file: options.files) {
					doFile(file, pattern, options, counter, this::publish);
				}
				return null;
			}

			@Override
			protected void process(List<Hit> toAdd) {
				toAdd.forEach(grepResults::add);
			}

			@Override
			protected void done() {
				frame.setRunning(false);
				frame.repaint();
				worker = null;
			}
		};

		worker.execute();
		
	}

	private void doFile(String filename, Pattern pattern, Options options, AtomicInteger counter, Consumer<Hit> publisher) throws InterruptedException {
		File file = new File(filename);
		if(file.isDirectory()) {
			if(options.recursive) {
				for (String f : file.list()) {
					if(Thread.interrupted())
						throw new InterruptedException();
					doFile(filename + File.separatorChar + f, pattern, options, counter, publisher);
				}
			}
		} else {
			if(options.include != null && !options.include.matcher(file.getName()).matches()) {
				return;
			}

			try (InputStreamReader isr = new InputStreamReader(Files.newInputStream(Paths.get(filename)), options.charset)) {
				BufferedReader br = new BufferedReader(isr);
				String line = br.readLine();
				int lineno = 1;
				int filePos = 0;
				while (line != null) {

					if (Thread.interrupted())
						throw new InterruptedException();

					Matcher m = pattern.matcher(line);
					while (m.find()) {
						Hit h = new Hit(filename, lineno, m.start(), filePos, m.end() - m.start(), line);
						publisher.accept(h);
						if (counter.incrementAndGet() >= options.maxCount) {
							worker.cancel(true);
							return;
						}
					}
					lineno++;
					filePos += line.length() + 1;
					line = br.readLine();
				}
				br.close();
			} catch (FileNotFoundException | NoSuchFileException ex) {
				publisher.accept(new Hit(filename, "File not found"));
			} catch (IOException e) {
				publisher.accept(new Hit(filename, "IO exception: " + e.getMessage()));
				e.printStackTrace();
			}
		}
	}

	public List<Hit> getHitList() {
		return grepResults.getList();
	}

	public HitListModel getHitListModel() {
		return grepResults;
	}

	public Options getOptions() {
		return options;
	}

	public void interrupt() {
		if (worker != null) {
			worker.cancel(true);
		}
	}
}

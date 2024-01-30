package de.matul.jgrep;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.*;

@SuppressWarnings("serial")
public class HitListModel extends AbstractListModel<Hit> {

	private final List<Hit> list = new ArrayList<Hit>();
	
	public /*synchronized*/ Hit getElementAt(int index) {
		return list.get(index);
	}

	public /*synchronized*/ int getSize() {
		return list.size();
	}
	
	public /*synchronized*/ void add(final Hit hit) {
		assert SwingUtilities.isEventDispatchThread();
		list.add(hit);
		fireIntervalAdded(this, getSize()-1, getSize()-1);
	}
	
	public void clear() {
		assert SwingUtilities.isEventDispatchThread();
		int oldsize = list.size();
		list.clear();
		fireIntervalRemoved(this, 0, oldsize);
	}
	
	public Hit get(int index) {
		return list.get(index);
	}

	public List<Hit> getList() {
		return Collections.unmodifiableList(list);
	}

	public void fire() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				fireContentsChanged(this, 0, getSize());
			}
		});
	}

}

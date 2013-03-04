import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public class SimplyCSV {
	
	// TODO: Organize everything here! It's a mess!
	// TODO: Add in a right click menu with the following submenus:
	// TODO: - Row, which in turn has:
	// TODO:   - Freeze / Thaw Row (it toggles)
	// TODO:   - Collapse Row (Automatically reexpands when it's clicked.)
	// TODO:   - Add Row (it has submenus)
	// TODO:     - Above
	// TODO:     - Below
	// TODO:   - Cut Row (to a pasteboard)
	// TODO:   - Copy Row (to a pasteboard)
	// TODO:   - Paste Row (from a pasteboard)
	// TODO:     - Above
	// TODO:     - Replace
	// TODO:     - Swap (if prior row was cut)
	// TODO:     - Below
	// TODO:   - Delete Row
	// TODO: - Column
	// TODO:   - Freeze Column / Thaw Column (it toggles)
	// TODO:   - Collapse Column (Automatically reexpands when it's clicked.)
	// TODO:   - Add Column (it has submenus)
	// TODO:     - To Left
	// TODO:     - To Right
	// TODO:   - Cut Column (to a pasteboard)
	// TODO:   - Copy Column (to a pasteboard)
	// TODO:   - Paste Column (from a pasteboard)
	// TODO:     - To Left
	// TODO:     - Replace
	// TODO:     - Swap (if prior column was cut)
	// TODO:     - To Right
	// TODO:   - Delete Column
	// TODO: - Add in an edit menu with these exact same options
	// TODO: - Allow Multiple Documents to open in multiple windows
	// TODO: - Allow Multiple Documents to open in multiple tabs
	// TODO: - Include a find or search capacity
	// TODO:   - Include an ability to replace what is found
	// TODO:     - Using REGEX! Because that's fun!
	// TODO: - Include a file comparison functionality?
	// TODO: - Somehow allow text to wrap sometimes?
	// TODO: - Make the app icon more appropriate.
	
	private static Toolkit toolkit = Toolkit.getDefaultToolkit();
	private static JFrame frame = new JFrame("*SimplyCSV"); // Create the window.
	private static JMenuBar menuBar = new JMenuBar();
	private static DefaultTableModel model = new DefaultTableModel();
	private static JTable table = new JTable(model);
	private static File file;
	private static int keyMask = toolkit.getMenuShortcutKeyMask();
	
	private static void setFile(File f) {
		file = f;
		try {
			frame.setTitle(f == null?"*SimplyCSV":file.getCanonicalPath() + " - SimplyCSV");
		} catch (IOException e) {
			frame.setTitle("*SimplyCSV");
		}
	}
	
	private static ActionListener newListener = new ActionListener() {
		@Override public void actionPerformed(ActionEvent event) {
			// TODO: Implement this.
			System.out.println("Error: Not Implemented");
		}
	};
	
	private static ActionListener openListener = new ActionListener() {
		@Override public void actionPerformed(ActionEvent event) {
			JFileChooser fileOpen = new JFileChooser();
			switch (fileOpen.showOpenDialog(null)) {
				case JFileChooser.APPROVE_OPTION:
					setFile(fileOpen.getSelectedFile());
					open();
					break;
				default:
					break;
			}
		}
	};
	
	private static ActionListener revertListener = new ActionListener() {
		@Override public void actionPerformed(ActionEvent event) { open(); }
	};
	
	private static ActionListener clearListener = new ActionListener() {
		@Override public void actionPerformed(ActionEvent event) {
			model.removeTableModelListener(tableModelListener);
			model.setColumnCount(0);
			model.setRowCount(0);
			model.addColumn(null);
			model.addRow(new Object[]{});
			model.addTableModelListener(tableModelListener);
			setFile(null);
			addNewMenus();
		}
	};
	
	private static boolean open() {
		boolean success = false;
		model.removeTableModelListener(tableModelListener);
		try {
			CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			String[] nextLine;
			model.setColumnCount(0);
			model.setRowCount(0);
			while ((nextLine = reader.readNext()) != null) {
				for (int i = model.getColumnCount(), n = nextLine.length; i <= n; i++) {
					model.addColumn(null);
				}
				model.addRow(nextLine);
			}
			model.addRow(new Object[]{});
			
			for (int i = 0, n = model.getColumnCount(); i < n; i++) {
				resizeColumn(i);
			}
			
			// Make the window the same size as the table, if possible.
			// Make it the size of the screen, if it's not possible.
			
			// TODO: Make this auto resize work:
			
			/*double screenWidth = toolkit.getScreenSize().getWidth();
			double screenHeight = toolkit.getScreenSize().getHeight();
			double newWidth = Math.min(screenWidth, table.getPreferredSize().getWidth());
			double newHeight = Math.min(screenHeight, table.getPreferredSize().getHeight());
			
			if (newWidth == screenWidth) {
				frame.setExtendedState(Frame.MAXIMIZED_HORIZ);
				newWidth = frame.getWidth();
			}
			
			if (newHeight == screenHeight) {
				frame.setExtendedState(Frame.MAXIMIZED_VERT);
				newHeight = frame.getHeight();
			}
			
			if (frame.getExtendedState() != Frame.MAXIMIZED_BOTH) {
				frame.setSize((int)newWidth, (int)newHeight);
			}
			System.out.println("newWidth = " + newWidth + " frame " + frame.getSize());*/
			success = true;
		} catch (FileNotFoundException exception) {
			exception.printStackTrace();
		} catch (IOException exception) {
			exception.printStackTrace();
		}
		model.addTableModelListener(tableModelListener);
		addNewMenus();
		return success;
	}
	
	private static ActionListener saveListener = new ActionListener() {
		@Override public void actionPerformed(ActionEvent event) { save(); }
	};
	
	private static ActionListener saveAsListener = new ActionListener() {
		@Override public void actionPerformed(ActionEvent event) {
			JFileChooser fileSave = new JFileChooser();
			switch (fileSave.showSaveDialog(null)) {
				case JFileChooser.APPROVE_OPTION:
					setFile(fileSave.getSelectedFile());
					save();
					addNewMenus();
					break;
				default:
					break;
			}
		}
	};
	
	private static ActionListener saveOldListener = new ActionListener() {
		@Override public void actionPerformed(ActionEvent event) {
			JFileChooser fileSave = new JFileChooser();
			switch (fileSave.showSaveDialog(null)) {
				case JFileChooser.APPROVE_OPTION:
					try { // The huge amount of overhead on properly closing these in finally makes it not worth it, to me.
						FileChannel source = new FileInputStream(file).getChannel();
						FileChannel destination = new FileOutputStream(fileSave.getSelectedFile()).getChannel();
						destination.transferFrom(source, 0, source.size());
						source.close();
						destination.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					save();
					break;
				default:
					break;
			}
		}
	};
	
	private static boolean save() {
		boolean success = false;
		try {
			CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
			for (int i = 0, n = model.getRowCount()-1; i < n; i++) {
				List<String> list = new ArrayList<String>();
				for (int j = 0, m = model.getColumnCount()-1; j < m; j++) {
					list.add((String)model.getValueAt(i, j));
				}
				writer.writeNext(list.toArray(new String[list.size()]));
			}
			writer.flush();
			writer.close();
			success = true;
		} catch (FileNotFoundException exception) {
			exception.printStackTrace();
		} catch (IOException exception) {
			exception.printStackTrace();
		}
		setFile(file); // Just to remove the star.
		return success;
	}
	
	/**
	 * The method that automatically gets called when the app launches.
	 * @param args Completely ignored right now.
	 */
	public static void main(final String[] args) {
		frame.setIconImage(Toolkit.getDefaultToolkit().createImage(ClassLoader.getSystemResource("icon.png")));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Make the app quit when the window is closed.
		
		// If on a Mac, set the menu bar to go at the top of the screen.
		if (System.getProperty("os.name").contains("Mac")) {
			  System.setProperty("apple.laf.useScreenMenuBar", "true");
		}
		
		addNewMenus();
		frame.setSize(500, 500);
		frame.setMinimumSize(new Dimension(200, 100));
		Container content = frame.getContentPane();
		table.setTableHeader(null);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		model.addColumn(null); 
		model.addRow(new Object[]{});
		model.addTableModelListener(tableModelListener);
		JScrollPane scrollPane = new JScrollPane(table,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		content.add(scrollPane, BorderLayout.CENTER);
		new FileDrop(frame, new FileDrop.Listener() {
			@Override public void filesDropped(File[] files) {
				if (files.length > 0) {
					setFile(files[0]);
					open();
				}
			}
		});
		frame.setVisible(true);
	}
	
	private static void addNewMenus() {
		menuBar.removeAll();
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		addNewMenuItem(fileMenu, "New", KeyEvent.VK_N, newListener);
		addNewMenuItem(fileMenu, "Open...", KeyEvent.VK_O, openListener);
		if (file == null || !file.exists() || !file.isFile()) {
			addNewMenuItem(fileMenu, "Save...", KeyEvent.VK_S, saveAsListener);
		} else {
			addNewMenuItem(fileMenu, "Save", KeyEvent.VK_S, saveListener);
			addNewMenuItem(fileMenu, "Save As...", KeyEvent.VK_E, saveAsListener);
			addNewMenuItem(fileMenu, "Save & Rename Old...", KeyEvent.VK_D, saveOldListener);
			addNewMenuItem(fileMenu, "Revert to Saved", KeyEvent.VK_R, revertListener);
		}
		addNewMenuItem(fileMenu, "Clear", KeyEvent.VK_L, clearListener);
		menuBar.add(fileMenu);
		frame.setJMenuBar(menuBar);
		menuBar.revalidate();
	}
	
	private static void addNewMenuItem(JMenu rootMenu, String itemName,
			int itemMnemonic, ActionListener itemActionListener) {
		JMenuItem menuItem = new JMenuItem(itemName, itemMnemonic);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(itemMnemonic, keyMask));
		menuItem.addActionListener(itemActionListener);
		rootMenu.add(menuItem);
	}
	
	private static boolean checkEmpty(int r, int c) {
		if (r >= model.getRowCount() || c >= model.getColumnCount()) {
			System.out.println("Error: Cell out of bounds row: " + r + " column: " + c);
			return true;
		}
		String string = (String)model.getValueAt(r, c);
		if (string == null) {
			System.out.println("Error: Cell string is null row: " + r + " column: " + c);
			return true;
		}
		return string.length() == 0;
	}
	
	private static TableModelListener tableModelListener = new TableModelListener() {
		@Override public void tableChanged(TableModelEvent event) {
			model.removeTableModelListener(tableModelListener);
			int c = event.getColumn();
			boolean columnHasContents = resizeColumn(c);
			
			if (columnHasContents && c == model.getColumnCount()-1) {
				int[] widths = new int[model.getColumnCount()];
				for (int i = 0, n = model.getColumnCount(); i < n; i++) {
					widths[i] = table.getColumnModel().getColumn(i).getPreferredWidth();
				}
				model.addColumn(null);
				for (int i = 0, n = model.getColumnCount()-1; i < n; i++) {
					table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
				}
			}
			
			//Check to see if new rows are necessary
			int l = model.getRowCount()-1;
			for (int i = 0, n = model.getColumnCount(); i < n; i++) {
				if (!checkEmpty(l, i)) {
					model.addRow(new Object[]{});
					break;
				}
			}
			model.addTableModelListener(tableModelListener);
			
			try {
				frame.setTitle(file == null?"*SimplyCSV":"*" + file.getCanonicalPath() + " - SimplyCSV");
			} catch (IOException e) {
				frame.setTitle("*SimplyCSV");
			}
		}
	};
	
	private static boolean resizeColumn(int c) {
		int width = 0; boolean columnIsEmpty = true;
		for (int i = 0, n = model.getRowCount(); i < n; i++) {
			Component cell = table.prepareRenderer(table.getCellRenderer(i, c), i, c);
			width = Math.max(cell.getPreferredSize().width, width);
			columnIsEmpty = columnIsEmpty && checkEmpty(i, c);
		}
		TableColumn column = table.getColumnModel().getColumn(c);
		column.setPreferredWidth(width + 10);
		return !columnIsEmpty;
	}
}

package kaflib.applications;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.crypto.SecretKey;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import kaflib.gui.PasswordConfirmPanel;
import kaflib.gui.ProgressLabel;
import kaflib.types.Mutex;
import kaflib.types.Pair;
import kaflib.types.Worker;
import kaflib.utils.AESUtils;
import kaflib.utils.CheckUtils;
import kaflib.utils.FileUtils;
import kaflib.utils.TypeUtils;

/**
 * Creates a simple app to encrypt/decrypt files using AES CBC with nested 
 * passwords.
 */
public class FileCrypto {
	
	private static final String DEFAULT_FILE_EXTENSION = "oo2";
	
	private final JFrame main_frame;
	private final JPanel main_panel;
	
	private final JPanel password_panel;
	private final PasswordConfirmPanel inner;
	private final PasswordConfirmPanel outer;
	
	private final JPanel select_panel;
	private final JButton browse_button;
	private final JLabel location;
	
	private final JPanel execute_panel;
	private final ProgressLabel progress_panel;
	private final JButton go_button;
	
	private File file;
	private String file_extension;
	
	private SecretKey outer_key;
	private SecretKey inner_key;
	private Mutex mutex;
	
	/**
	 * Creates the executable.
	 * @throws Exception
	 */
	public FileCrypto() throws Exception {
		file = null;
		mutex = new Mutex();
		file_extension = DEFAULT_FILE_EXTENSION;
		
		main_frame = new JFrame();
		main_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		main_panel = new JPanel(new BorderLayout());

		password_panel = new JPanel(new GridLayout(1, 2));
		password_panel.setBorder(new EmptyBorder(4, 4, 4, 4));
		outer = new PasswordConfirmPanel("Outer");
		inner = new PasswordConfirmPanel("Inner");
		password_panel.add(outer);
		password_panel.add(inner);
		
		select_panel = new JPanel(new BorderLayout());
		select_panel.setBorder(new EmptyBorder(4, 4, 4, 4));
		browse_button = new JButton("Browse...");
		browse_button.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setMultiSelectionEnabled(false);
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				if (chooser.showOpenDialog(main_frame) == JFileChooser.APPROVE_OPTION) {
					file = chooser.getSelectedFile();
					if (file != null) {
						location.setText(file.getPath());
						go_button.setEnabled(true);
					}
				}
			}
		});
		select_panel.add(browse_button, BorderLayout.WEST);
		location = new JLabel();
		location.setText("[Select path]");
		location.setBorder(new EmptyBorder(0, 16, 0, 0));
		select_panel.add(location, BorderLayout.CENTER);
		
		execute_panel = new JPanel(new BorderLayout());
		execute_panel.setBorder(new EmptyBorder(4, 4, 4, 4));
		go_button = new JButton("Go");
		go_button.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Worker worker = new Worker() {
						@Override
						protected void process() throws Exception {
							execute();
						}
					};
					worker.start();
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		go_button.setEnabled(false);
		
		execute_panel.add(go_button, BorderLayout.WEST);
		progress_panel = new ProgressLabel(80);
		execute_panel.add(progress_panel, BorderLayout.CENTER);
		
		main_panel.add(password_panel, BorderLayout.NORTH);
		main_panel.add(select_panel, BorderLayout.CENTER);
		main_panel.add(execute_panel, BorderLayout.SOUTH);
		main_frame.getContentPane().add(main_panel);
		main_frame.pack();
		main_frame.setVisible(true);
	}
	
	/**
	 * Generates the SecretKey types to replace the passwords.
	 */
	private void setKeys() {
		if (outer.getText() == null || inner.getText() == null) {
			return;
		}
		
		if (outer_key == null && inner_key == null) {
			try {
				outer_key = AESUtils.generateKey(outer.getText(), 
												 inner.getText().substring(0, AESUtils.SALT_LENGTH).getBytes("UTF-8"));
				inner_key = AESUtils.generateKey(inner.getText(), 
						 						 outer.getText().substring(0, AESUtils.SALT_LENGTH).getBytes("UTF-8"));
				
				outer.clear();
				outer.setEnabled(false);
				inner.clear();
				inner.setEnabled(false);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * Returns true if we're encrypting, false if we're decrypting and the
	 * set of files to operate on.  Null if there are files names that'll
	 * probably base64 to be too long.
	 * @return
	 */
	private Pair<Boolean, Set<File>> getFiles() {
		Set<File> files = FileUtils.getRecursive(file);
		Set<File> encrypted_files = new HashSet<File>();
		Set<File> too_long = new HashSet<File>();
		
		for (File f : files) {
			if (f.getName().length() > 32) {
				too_long.add(f);
			}
			
			if (f.getName().endsWith("." + file_extension)) {
				encrypted_files.add(f);
			}
		}
		
		// If there are encrypted files, we're decrypting.  In this case there 
		// are no too-long file names.
		if (!encrypted_files.isEmpty()) {
			return new Pair<Boolean, Set<File>>(false, encrypted_files);
		}
		// We're either encrypting or doing nothing if the file names are
		// too long (b64 expands them).
		else if (!too_long.isEmpty()) {
			return new Pair<Boolean, Set<File>>(null, too_long);
		}
		// All good, encrypt away.
		else {
			return new Pair<Boolean, Set<File>>(true, files);
		}
	}
	
	/**
	 * Pop up a confirm dialog for the action.
	 * @param encrypt
	 * @param count
	 * @return
	 */
	private boolean confirmRun(final Boolean encrypt, final int count) {
		String message;
		if (encrypt == true) {
			message = "Encrypt all " + count + " files under directory:\n" + file + "?";
		}
		else if (encrypt == false) {
			message = "Decrypt all " + count + " files under directory:\n" + file + "?";
		}
		else {
			System.err.println("Encrypt flag ended up null.");
			return false;
		}
		
		// Confirm directory spec, prompt for dialog and do it.
		if (JOptionPane.showConfirmDialog(main_panel,
						    		    message,
						    		    "Proceed?",
						    		    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Checks the passwords for equivalence and length.
	 * @return
	 */
	private boolean checkPasswords() {
		if (outer_key != null && inner_key != null) {
			return true;
		}
		
		if (!outer.match() || !inner.match()) {
			JOptionPane.showMessageDialog(main_panel, 
										  "Passwords do not match.", 
										  "Input error", 
										  JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		if (!outer.isLongerOrEqual(AESUtils.SALT_LENGTH) ||
			!inner.isLongerOrEqual(AESUtils.SALT_LENGTH)) {
			JOptionPane.showMessageDialog(main_panel, 
										  "Passwords must be at least eight characters.", 
										  "Input error", 
										  JOptionPane.ERROR_MESSAGE);
			return false;
		}

		outer.setEnabled(false);
		inner.setEnabled(false);
		
		return true;
	}
	
	/**
	 * Run a crypto job.
	 */
	private void execute() {
		if (!checkPasswords()) {
			return;
		}
		
		
		// Ensure only one runs at a time.
		Integer lock = mutex.lock();
		if (lock == null) {
			return;
		}
		go_button.setEnabled(false);
		browse_button.setEnabled(false);
		setKeys();
		
		try {
			CheckUtils.check(outer_key, "outer key");
			CheckUtils.check(inner_key, "inner key");
			boolean proceed = true;
			
			Pair<Boolean, Set<File>> files = getFiles();
			if (files.getFirst() == null) {
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < 5; i++) {
					if (files.getSecond().isEmpty()) {
						break;
					}
					File file = TypeUtils.getItem(files.getSecond());
					files.getSecond().remove(file);
					buffer.append("\n" + file);
				}
				
				JOptionPane.showMessageDialog(main_panel, 
						  "File name(s) too long:" + buffer.toString(), 
						  "Input error", 
						  JOptionPane.ERROR_MESSAGE);
				proceed = false;
			}
			
			if (proceed) {
				proceed = confirmRun(files.getFirst(), files.getSecond().size());
			}
			
			if (proceed){ 
				Object o = new Object();
				progress_panel.register(o, files.getSecond().size());
				for (File f : files.getSecond()) {
					progress_panel.increment(o);
					progress_panel.setText(f.getPath());
					if (!f.canRead() || f.isDirectory()) {
						System.out.println("Skipping: " + f + ".");
						continue;
					}
					try {
						if (files.getFirst()) {
							AESUtils.doubleEncrypt(f, 
												   file_extension, 
												   outer_key, 
												   inner_key);
						}
						else {
							AESUtils.doubleDecrypt(f,
												   file_extension, 
												   outer_key, 
												   inner_key);
						}
					}
					catch (Exception e) {
						System.out.println("Failed to encrypt/decrypt: " + 
										   f + ":\n" + e.getMessage());
						e.printStackTrace();
					}
				}
				progress_panel.release(o);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			mutex.unlock(lock);
			browse_button.setEnabled(true);
			go_button.setEnabled(true);
		}
		catch(Exception e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * Main.
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			new FileCrypto();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
		
}

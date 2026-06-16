package jadx.gui.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jadx.gui.JadxWrapper;
import jadx.gui.device.protocol.ADBDevice;
import jadx.gui.device.protocol.AdbPackage;
import jadx.gui.device.protocol.AdbService;
import jadx.gui.device.protocol.AdbService.AdbUser;
import jadx.gui.device.protocol.ApkPath;
import jadx.gui.device.protocol.DeviceExplorerAssistant;
import jadx.gui.jobs.ExportTask;
import jadx.gui.jobs.ITaskInfo;
import jadx.gui.jobs.TaskStatus;
import jadx.gui.ui.MainWindow;
import jadx.gui.utils.NLS;
import jadx.gui.utils.UiUtils;

public class DeviceExplorerDialog extends JDialog {
	private static final Logger LOG = LoggerFactory.getLogger(DeviceExplorerDialog.class);
	private static final long serialVersionUID = 1L;

	private final transient MainWindow mainWindow;

	private JComboBox<ADBDevice> deviceComboBox;
	private JComboBox<AdbUser> userComboBox;
	private JButton refreshButton;

	private JTextField searchField;
	private JRadioButton allAppsRadio;
	private JRadioButton userAppsRadio;
	private JRadioButton systemAppsRadio;

	private JList<AdbPackage> packageList;
	private DefaultListModel<AdbPackage> packageListModel;
	private List<AdbPackage> allPackages = new ArrayList<>();

	private JPanel splitsPanel;
	private final List<JCheckBox> splitCheckBoxes = new ArrayList<>();
	private int packageListUserId;

	private JTextField outDirTextField;
	private JButton browseButton;
	private JCheckBox runAssistantCheckbox;

	private JButton pullButton;
	private JButton pullOpenButton;
	private JButton pullDecompileButton;
	private JButton closeButton;

	public DeviceExplorerDialog(MainWindow mainWindow) {
		super(mainWindow);
		this.mainWindow = mainWindow;
		setTitle(NLS.str("device_explorer.title"));
		initUI();
		initEvents();
		UiUtils.addEscapeShortCutToDispose(this);

		// Initialize default output folder
		String userHome = System.getProperty("user.home");
		File defaultOut = new File(userHome, "JadxDeviceExplorer");
		outDirTextField.setText(defaultOut.getAbsolutePath());

		SwingUtilities.invokeLater(this::loadDevices);
	}

	private void initUI() {
		// Top Panel: Device info and user profile selection
		JPanel topPanel = new JPanel(new GridBagLayout());
		topPanel.setBorder(BorderFactory.createTitledBorder("Android Device / User Selection"));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Connected Device
		gbc.gridx = 0;
		gbc.gridy = 0;
		topPanel.add(new JLabel(NLS.str("device_explorer.device")), gbc);

		gbc.gridx = 1;
		gbc.weightx = 1.0;
		deviceComboBox = new JComboBox<>();
		topPanel.add(deviceComboBox, gbc);

		gbc.gridx = 2;
		gbc.weightx = 0.0;
		refreshButton = new JButton(NLS.str("device_explorer.refresh"));
		topPanel.add(refreshButton, gbc);

		// Android User Profile
		gbc.gridx = 0;
		gbc.gridy = 1;
		topPanel.add(new JLabel(NLS.str("device_explorer.user")), gbc);

		gbc.gridx = 1;
		gbc.gridwidth = 2;
		gbc.weightx = 1.0;
		userComboBox = new JComboBox<>();
		topPanel.add(userComboBox, gbc);

		// Filter / Search Panel
		JPanel filterPanel = new JPanel(new GridBagLayout());
		filterPanel.setBorder(BorderFactory.createTitledBorder("Filter & Search"));
		GridBagConstraints fgbc = new GridBagConstraints();
		fgbc.insets = new Insets(5, 5, 5, 5);
		fgbc.fill = GridBagConstraints.HORIZONTAL;

		// Search Field
		fgbc.gridx = 0;
		fgbc.gridy = 0;
		filterPanel.add(new JLabel(NLS.str("device_explorer.search")), fgbc);

		fgbc.gridx = 1;
		fgbc.weightx = 1.0;
		fgbc.gridwidth = 3;
		searchField = new JTextField();
		filterPanel.add(searchField, fgbc);

		// Filter Radio Buttons
		fgbc.gridx = 0;
		fgbc.gridy = 1;
		fgbc.gridwidth = 1;
		fgbc.weightx = 0.0;
		filterPanel.add(new JLabel("Apps:"), fgbc);

		allAppsRadio = new JRadioButton(NLS.str("device_explorer.filter_all"), true);
		userAppsRadio = new JRadioButton(NLS.str("device_explorer.filter_user"));
		systemAppsRadio = new JRadioButton(NLS.str("device_explorer.filter_system"));

		ButtonGroup appGroup = new ButtonGroup();
		appGroup.add(allAppsRadio);
		appGroup.add(userAppsRadio);
		appGroup.add(systemAppsRadio);

		fgbc.gridx = 1;
		filterPanel.add(allAppsRadio, fgbc);
		fgbc.gridx = 2;
		filterPanel.add(userAppsRadio, fgbc);
		fgbc.gridx = 3;
		filterPanel.add(systemAppsRadio, fgbc);

		// Combine top controls
		JPanel northPanel = new JPanel();
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
		northPanel.add(topPanel);
		northPanel.add(filterPanel);

		// Center: Split Pane for Packages on Left and Splits Checklist on Right
		packageListModel = new DefaultListModel<>();
		packageList = new JList<>(packageListModel);
		packageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane packageScroll = new JScrollPane(packageList);
		packageScroll.setBorder(BorderFactory.createTitledBorder("Packages"));

		splitsPanel = new JPanel();
		splitsPanel.setLayout(new BoxLayout(splitsPanel, BoxLayout.Y_AXIS));
		JScrollPane splitsScroll = new JScrollPane(splitsPanel);
		splitsScroll.setBorder(BorderFactory.createTitledBorder("APK Components / Splits"));

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, packageScroll, splitsScroll);
		splitPane.setDividerLocation(400);

		// Bottom Panel: Output folder browse and action buttons
		JPanel southPanel = new JPanel();
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
		southPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// Output Dir panel
		JPanel outDirPanel = new JPanel(new BorderLayout(5, 5));
		outDirPanel.add(new JLabel(NLS.str("device_explorer.out_dir")), BorderLayout.WEST);
		outDirTextField = new JTextField();
		outDirPanel.add(outDirTextField, BorderLayout.CENTER);
		browseButton = new JButton(NLS.str("device_explorer.browse"));
		outDirPanel.add(browseButton, BorderLayout.EAST);

		// Buttons row
		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
		pullButton = new JButton(NLS.str("device_explorer.pull"));
		pullOpenButton = new JButton(NLS.str("device_explorer.pull_open"));
		pullDecompileButton = new JButton(NLS.str("device_explorer.pull_decompile"));
		closeButton = new JButton(NLS.str("tabs.close"));

		buttonsPanel.add(pullButton);
		buttonsPanel.add(pullOpenButton);
		buttonsPanel.add(pullDecompileButton);
		buttonsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonsPanel.add(closeButton);

		southPanel.add(outDirPanel);
		southPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		runAssistantCheckbox = new JCheckBox(NLS.str("device_explorer.run_assistant"), true);
		optionsPanel.add(runAssistantCheckbox);
		southPanel.add(optionsPanel);
		southPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		southPanel.add(buttonsPanel);

		// Main Layout assembly
		JPanel contentPane = new JPanel(new BorderLayout(10, 10));
		contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		contentPane.add(northPanel, BorderLayout.NORTH);
		contentPane.add(splitPane, BorderLayout.CENTER);
		contentPane.add(southPanel, BorderLayout.SOUTH);

		getContentPane().add(contentPane);
		pack();
		setSize(900, 600);
		setLocationRelativeTo(mainWindow);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setModalityType(ModalityType.MODELESS);
	}

	private void initEvents() {
		refreshButton.addActionListener(e -> loadDevices());

		deviceComboBox.addActionListener(e -> {
			ADBDevice device = (ADBDevice) deviceComboBox.getSelectedItem();
			if (device != null) {
				loadUsers(device);
			} else {
				userComboBox.removeAllItems();
				packageListModel.clear();
				clearSplitsPanel();
			}
		});

		userComboBox.addActionListener(e -> {
			ADBDevice device = (ADBDevice) deviceComboBox.getSelectedItem();
			AdbUser user = (AdbUser) userComboBox.getSelectedItem();
			if (device != null && user != null) {
				loadPackages(device, user);
			} else {
				packageListModel.clear();
				clearSplitsPanel();
			}
		});

		packageList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				loadSplits();
			}
		});

		searchField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				filterPackages();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				filterPackages();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				filterPackages();
			}
		});

		allAppsRadio.addActionListener(e -> filterPackages());
		userAppsRadio.addActionListener(e -> filterPackages());
		systemAppsRadio.addActionListener(e -> filterPackages());

		browseButton.addActionListener(e -> browseDirectory());

		pullButton.addActionListener(e -> startPullAction(false, false));
		pullOpenButton.addActionListener(e -> startPullAction(true, false));
		pullDecompileButton.addActionListener(e -> startPullAction(true, true));
		closeButton.addActionListener(e -> dispose());
	}

	private String getAdbPath() {
		String path = mainWindow.getSettings().getAdbDialogPath();
		return path.isEmpty() ? AdbService.detectAdbPath() : path;
	}

	private String getAdbHost() {
		String host = mainWindow.getSettings().getAdbDialogHost();
		return host.isEmpty() ? "127.0.0.1" : host;
	}

	private int getAdbPort() {
		String portStr = mainWindow.getSettings().getAdbDialogPort();
		if (portStr.isEmpty()) {
			return 5037;
		}
		try {
			return Integer.parseInt(portStr);
		} catch (NumberFormatException e) {
			return 5037;
		}
	}

	private void setControlsEnabled(boolean enabled) {
		deviceComboBox.setEnabled(enabled);
		userComboBox.setEnabled(enabled);
		refreshButton.setEnabled(enabled);
		searchField.setEnabled(enabled);
		allAppsRadio.setEnabled(enabled);
		userAppsRadio.setEnabled(enabled);
		systemAppsRadio.setEnabled(enabled);
		packageList.setEnabled(enabled);
		outDirTextField.setEnabled(enabled);
		browseButton.setEnabled(enabled);
		runAssistantCheckbox.setEnabled(enabled);
		pullButton.setEnabled(enabled);
		pullOpenButton.setEnabled(enabled);
		pullDecompileButton.setEnabled(enabled);
	}

	private void loadDevices() {
		setControlsEnabled(false);
		deviceComboBox.removeAllItems();
		userComboBox.removeAllItems();
		packageListModel.clear();
		clearSplitsPanel();

		UiUtils.bgRun(() -> {
			try {
				List<ADBDevice> devices = AdbService.listDevices(getAdbHost(), getAdbPort());
				UiUtils.uiRun(() -> {
					if (devices.isEmpty()) {
						UiUtils.errorMessage(this, NLS.str("device_explorer.no_devices"));
						setControlsEnabled(true);
						return;
					}
					DefaultComboBoxModel<ADBDevice> model = new DefaultComboBoxModel<>();
					for (ADBDevice d : devices) {
						model.addElement(d);
					}
					deviceComboBox.setModel(model);
					deviceComboBox.setSelectedIndex(0);
					setControlsEnabled(true);
				});
			} catch (Exception e) {
				LOG.warn("Failed to list devices: {}", e.getMessage());
				UiUtils.uiRun(() -> {
					UiUtils.errorMessage(this,
							"ADB list devices failed: " + getAdbErrorMessage(e) + "\n" + NLS.str("device_explorer.no_devices"));
					setControlsEnabled(true);
				});
			}
		});
	}

	private static String getAdbErrorMessage(Exception e) {
		Throwable cause = e;
		while (cause.getCause() != null) {
			cause = cause.getCause();
		}
		String message = e.getMessage();
		if (message == null || message.isBlank()) {
			message = cause.getMessage();
		}
		return message == null || message.isBlank() ? cause.getClass().getSimpleName() : message;
	}

	private void loadUsers(ADBDevice device) {
		setControlsEnabled(false);
		userComboBox.removeAllItems();
		packageListModel.clear();
		clearSplitsPanel();

		UiUtils.bgRun(() -> {
			try {
				List<AdbUser> users = AdbService.listUsers(device);
				UiUtils.uiRun(() -> {
					DefaultComboBoxModel<AdbUser> model = new DefaultComboBoxModel<>();
					for (AdbUser u : users) {
						model.addElement(u);
					}
					userComboBox.setModel(model);
					if (userComboBox.getItemCount() > 0) {
						userComboBox.setSelectedIndex(0);
					}
					setControlsEnabled(true);
				});
			} catch (Exception e) {
				LOG.error("Failed to list users", e);
				UiUtils.uiRun(() -> {
					UiUtils.errorMessage(this, "Failed to list users: " + e.getMessage());
					setControlsEnabled(true);
				});
			}
		});
	}

	private void loadPackages(ADBDevice device, AdbUser user) {
		setControlsEnabled(false);
		packageListModel.clear();
		clearSplitsPanel();

		int userId = user.getId();
		UiUtils.bgRun(() -> {
			try {
				boolean fallbackTriggered = (userId != 0);
				int effectiveUserId = userId;
				List<AdbPackage> packagesList;
				try {
					packagesList = AdbService.listPackages(device, userId, "all");
					fallbackTriggered = false;
				} catch (IOException ex) {
					if (userId != 0) {
						LOG.warn("Failed to list packages for user {}: {}. Falling back to user 0.", userId, ex.getMessage());
						packagesList = AdbService.listPackages(device, 0, "all");
						effectiveUserId = 0;
					} else {
						throw ex;
					}
				}

				final List<AdbPackage> finalPackages = packagesList;
				final boolean selectUser0 = fallbackTriggered;
				final int finalEffectiveUserId = effectiveUserId;
				UiUtils.uiRun(() -> {
					if (selectUser0) {
						for (int i = 0; i < userComboBox.getItemCount(); i++) {
							AdbUser u = userComboBox.getItemAt(i);
							if (u.getId() == 0) {
								userComboBox.setSelectedIndex(i);
								break;
							}
						}
					}
					packageListUserId = finalEffectiveUserId;
					allPackages = finalPackages;
					filterPackages();
					setControlsEnabled(true);
				});
			} catch (Exception e) {
				LOG.error("Failed to list packages", e);
				UiUtils.uiRun(() -> {
					UiUtils.errorMessage(this, "Failed to list packages: " + e.getMessage());
					setControlsEnabled(true);
				});
			}
		});
	}

	private void filterPackages() {
		String searchTxt = searchField.getText().trim().toLowerCase();
		String selectedFilter = "all";
		if (userAppsRadio.isSelected()) {
			selectedFilter = "user";
		} else if (systemAppsRadio.isSelected()) {
			selectedFilter = "system";
		}

		packageListModel.clear();
		for (AdbPackage pkg : allPackages) {
			boolean matchesSearch = pkg.getPackageName().toLowerCase().contains(searchTxt);
			boolean matchesFilter = false;
			if ("all".equalsIgnoreCase(selectedFilter)) {
				matchesFilter = true;
			} else if ("system".equalsIgnoreCase(selectedFilter)) {
				matchesFilter = pkg.isSystem();
			} else if ("user".equalsIgnoreCase(selectedFilter)) {
				matchesFilter = !pkg.isSystem();
			}

			if (matchesSearch && matchesFilter) {
				packageListModel.addElement(pkg);
			}
		}
	}

	private void clearSplitsPanel() {
		splitsPanel.removeAll();
		splitCheckBoxes.clear();
		splitsPanel.revalidate();
		splitsPanel.repaint();
	}

	private void loadSplits() {
		AdbPackage selectedPkg = packageList.getSelectedValue();
		if (selectedPkg == null) {
			clearSplitsPanel();
			return;
		}
		ADBDevice device = (ADBDevice) deviceComboBox.getSelectedItem();
		AdbUser user = (AdbUser) userComboBox.getSelectedItem();
		if (device == null || user == null) {
			clearSplitsPanel();
			return;
		}

		splitsPanel.removeAll();
		splitsPanel.add(new JLabel("Loading splits..."));
		splitsPanel.revalidate();
		splitsPanel.repaint();

		String pkgName = selectedPkg.getPackageName();
		int userId = user.getId();
		int effectiveUserId = packageListUserId;

		UiUtils.bgRun(() -> {
			try {
				List<ApkPath> paths = AdbService.resolveApkPaths(device, pkgName, effectiveUserId);
				UiUtils.uiRun(() -> {
					// Check if selection is still the same package to avoid race conditions
					AdbPackage currentSel = packageList.getSelectedValue();
					if (currentSel == null || !currentSel.getPackageName().equals(pkgName)) {
						return;
					}
					splitsPanel.removeAll();
					splitCheckBoxes.clear();

					if (paths.isEmpty()) {
						splitsPanel.add(new JLabel("No APK paths resolved."));
					} else {
						splitsPanel.setLayout(new GridBagLayout());
						GridBagConstraints gbc = new GridBagConstraints();
						gbc.gridx = 0;
						gbc.gridy = 0;
						gbc.anchor = GridBagConstraints.WEST;
						gbc.insets = new Insets(5, 5, 5, 5);

						splitsPanel.add(new JLabel(NLS.str("device_explorer.splits")), gbc);

						for (ApkPath path : paths) {
							gbc.gridy++;
							JCheckBox cb = new JCheckBox(path.getLocalName() + " (" + path.getType() + ")", true);
							cb.putClientProperty("apkPath", path);
							splitCheckBoxes.add(cb);
							splitsPanel.add(cb, gbc);
						}

						// Spacer to push components to the top
						gbc.gridy++;
						gbc.weighty = 1.0;
						splitsPanel.add(new JPanel(), gbc);
					}
					splitsPanel.revalidate();
					splitsPanel.repaint();
				});
			} catch (Exception ex) {
				LOG.error("Failed to resolve splits for package: {}", pkgName, ex);
				UiUtils.uiRun(() -> {
					splitsPanel.removeAll();
					splitsPanel.add(new JLabel("Error loading splits: " + ex.getMessage()));
					splitsPanel.revalidate();
					splitsPanel.repaint();
				});
			}
		});
	}

	private void browseDirectory() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle(NLS.str("device_explorer.select_dir"));
		File current = new File(outDirTextField.getText().trim());
		if (current.exists()) {
			chooser.setCurrentDirectory(current);
		}
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			outDirTextField.setText(chooser.getSelectedFile().getAbsolutePath());
		}
	}

	private void startPullAction(boolean openAfterPull, boolean decompileAfterPull) {
		ADBDevice device = (ADBDevice) deviceComboBox.getSelectedItem();
		AdbUser user = (AdbUser) userComboBox.getSelectedItem();
		AdbPackage pkg = packageList.getSelectedValue();

		if (device == null || user == null || pkg == null) {
			UiUtils.errorMessage(this, "Please select device, user, and package.");
			return;
		}

		List<ApkPath> selectedPaths = new ArrayList<>();
		for (JCheckBox cb : splitCheckBoxes) {
			if (cb.isSelected()) {
				selectedPaths.add((ApkPath) cb.getClientProperty("apkPath"));
			}
		}

		if (selectedPaths.isEmpty()) {
			UiUtils.errorMessage(this, "Please check at least one APK component to pull.");
			return;
		}

		// Validation checks (Milestone v0.3)
		boolean hasBase = false;
		for (ApkPath path : selectedPaths) {
			if (path.getType().equalsIgnoreCase("base")) {
				hasBase = true;
				break;
			}
		}
		if (!hasBase) {
			int response = javax.swing.JOptionPane.showConfirmDialog(this,
					NLS.str("device_explorer.warn_no_base"),
					NLS.str("device_explorer.warn_title_no_base"),
					javax.swing.JOptionPane.YES_NO_OPTION,
					javax.swing.JOptionPane.WARNING_MESSAGE);
			if (response != javax.swing.JOptionPane.YES_OPTION) {
				return;
			}
		} else if (selectedPaths.size() < splitCheckBoxes.size()) {
			int response = javax.swing.JOptionPane.showConfirmDialog(this,
					NLS.str("device_explorer.warn_incomplete_splits"),
					NLS.str("device_explorer.warn_title_incomplete_splits"),
					javax.swing.JOptionPane.YES_NO_OPTION,
					javax.swing.JOptionPane.WARNING_MESSAGE);
			if (response != javax.swing.JOptionPane.YES_OPTION) {
				return;
			}
		}

		String outDirStr = outDirTextField.getText().trim();
		if (outDirStr.isEmpty()) {
			UiUtils.errorMessage(this, "Please specify an output directory.");
			return;
		}

		File outDir = new File(outDirStr, pkg.getPackageName());

		final List<String> errors = new CopyOnWriteArrayList<>();
		final List<File> pulledApkFiles = new CopyOnWriteArrayList<>();

		String adbPath = getAdbPath();
		String serial = device.getSerial();
		int userId = user.getId();
		int effectiveUserId = packageListUserId;
		String pkgName = pkg.getPackageName();

		setControlsEnabled(false);

		mainWindow.getBackgroundExecutor().execute(
				NLS.str("device_explorer.pulling"),
				() -> {
					try {
						File apksDir = new File(outDir, "apks");
						if (!apksDir.exists()) {
							apksDir.mkdirs();
						}

						for (ApkPath path : selectedPaths) {
							File localFile = new File(apksDir, path.getLocalName());
							LOG.info("Pulling: {} -> {}", path.getRemotePath(), localFile.getAbsolutePath());
							AdbService.pullApk(adbPath, device, path.getRemotePath(), localFile);
							pulledApkFiles.add(localFile);
						}

						// Write metadata pull-report.json
						File reportsDir = new File(outDir, "reports");
						if (!reportsDir.exists()) {
							reportsDir.mkdirs();
						}

						Map<String, Object> report = new LinkedHashMap<>();
						report.put("packageName", pkgName);
						report.put("deviceSerial", serial);
						report.put("selectedAndroidUser", userId);
						report.put("effectiveAndroidUser", effectiveUserId);
						report.put("pulledAt", java.time.Instant.now().toString());

						List<Map<String, Object>> apkFilesList = new ArrayList<>();
						for (ApkPath path : selectedPaths) {
							File localFile = new File(apksDir, path.getLocalName());
							Map<String, Object> apkInfo = new LinkedHashMap<>();
							apkInfo.put("remotePath", path.getRemotePath());
							apkInfo.put("localPath", "apks/" + path.getLocalName());
							apkInfo.put("sizeBytes", localFile.exists() ? localFile.length() : 0);
							apkInfo.put("type", path.getType());
							apkFilesList.add(apkInfo);
						}
						report.put("apkFiles", apkFilesList);

						Gson gson = new GsonBuilder().setPrettyPrinting().create();
						File reportFile = new File(reportsDir, "pull-report.json");
						try (FileWriter writer = new FileWriter(reportFile)) {
							gson.toJson(report, writer);
						}
						LOG.info("Metadata pull report saved to {}", reportFile.getAbsolutePath());

					} catch (Exception ex) {
						LOG.error("Failed to pull APKs", ex);
						errors.add(ex.getMessage());
					}
				},
				status -> {
					setControlsEnabled(true);
					if (!errors.isEmpty()) {
						UiUtils.errorMessage(mainWindow, "Failed to pull APKs:\n" + String.join("\n", errors));
					} else if (status == TaskStatus.CANCEL_BY_USER) {
						// cancelled
					} else if (status == TaskStatus.COMPLETE) {
						UiUtils.showMessageBox(mainWindow, NLS.str("device_explorer.pull_success", outDir.getAbsolutePath()));
						if (openAfterPull) {
							List<Path> pathsToOpen = pulledApkFiles.stream()
									.map(File::toPath)
									.collect(Collectors.toList());
							if (decompileAfterPull) {
								File jadxOutputDir = new File(outDir, "jadx-output");
								File decompileReportFile = new File(outDir, "reports/decompile-report.json");
								boolean runAssistant = runAssistantCheckbox.isSelected();
								mainWindow.open(pathsToOpen, () -> {
									mainWindow.getBackgroundExecutor().execute(new DeviceExplorerExportTask(
											mainWindow, mainWindow.getWrapper(), jadxOutputDir, decompileReportFile, pulledApkFiles,
											runAssistant));
								});
							} else {
								mainWindow.open(pathsToOpen);
							}
						}
						dispose();
					} else {
						UiUtils.errorMessage(mainWindow, "Pull task finished with status: " + status);
					}
				});
	}

	private static class DeviceExplorerExportTask extends ExportTask {
		private final File saveDir;
		private final File reportFile;
		private final List<File> inputFiles;
		private final long startTime;
		private final JadxWrapper wrapper;
		private final boolean runAssistant;
		private final MainWindow mainWindow;

		public DeviceExplorerExportTask(MainWindow mainWindow, JadxWrapper wrapper, File saveDir, File reportFile, List<File> inputFiles,
				boolean runAssistant) {
			super(mainWindow, wrapper, saveDir);
			this.mainWindow = mainWindow;
			this.saveDir = saveDir;
			this.wrapper = wrapper;
			this.reportFile = reportFile;
			this.inputFiles = inputFiles;
			this.runAssistant = runAssistant;
			this.startTime = System.currentTimeMillis();
		}

		@Override
		public void onFinish(ITaskInfo taskInfo) {
			super.onFinish(taskInfo);
			long duration = System.currentTimeMillis() - startTime;

			try {
				File parent = reportFile.getParentFile();
				if (parent != null && !parent.exists()) {
					parent.mkdirs();
				}

				Map<String, Object> report = new LinkedHashMap<>();
				report.put("outputPath", saveDir.getAbsolutePath());
				report.put("durationMs", duration);
				report.put("status", taskInfo.getStatus().name());
				report.put("jobsSkipped", taskInfo.getJobsSkipped());

				List<String> inputs = new ArrayList<>();
				for (File f : inputFiles) {
					inputs.add(f.getName());
				}
				report.put("inputApkFiles", inputs);
				report.put("generatedAt", java.time.Instant.now().toString());

				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				try (FileWriter writer = new FileWriter(reportFile)) {
					gson.toJson(report, writer);
				}
				LOG.info("Decompilation report saved to {}", reportFile.getAbsolutePath());

				if (runAssistant && taskInfo.getStatus() == TaskStatus.COMPLETE) {
					File assistantReportFile = new File(reportFile.getParentFile(), "assistant-report.json");
					mainWindow.getBackgroundExecutor().execute("Security Assistant Scanning", () -> {
						try {
							DeviceExplorerAssistant.runAnalysis(saveDir, assistantReportFile);
						} catch (Exception e) {
							LOG.error("Failed to run Security Assistant analysis", e);
						}
					});
				}
			} catch (Exception e) {
				LOG.error("Failed to save decompilation report", e);
			}
		}
	}
}

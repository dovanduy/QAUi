package com.IOSGetScreen;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Util.CheckUE;
import com.Util.Excute;
import com.Util.HelperUtil;
import com.Viewer.MainRun;
import com.constant.CIOSCMD;
import com.constant.Cconfig;

public class ScreenMirrorUI extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2933121351477900086L;

	Logger logger = LoggerFactory.getLogger(ScreenMirrorUI.class);

	// private static final int DEFAULT_WIDTH = 320;
	// private static final int DEFAULT_HEIGHT = 480;
	// private static final String EXT_PNG = "png";
	private MainPanel mPanel;
	private JPopupMenu mPopupMenu;
	private int mRawImageWidth = 720;
	private int mRawImageHeight = 1280;
	private boolean mPortrait = true;
	private double mZoom = 0.5D;
	private boolean mAdjustColor = false;
	private JCheckBoxMenuItem mAdjustColorCheckBoxMenuItem;
	String selecteddevice = "";
	// private GetDevices mADB;

	// private MonitorThread mMonitorThread;
	boolean monitorthreadrunnable = false;
	String title = "Screen Mirror";
	String udid;

//	  private MouseListener mMouseListener = new MouseListener() {
//	    public void mouseReleased(MouseEvent e) {
//	    }
//
//	    public void mousePressed(MouseEvent e) {
//	    }
//
//	    public void mouseExited(MouseEvent e) {
//	    }
//
//	    public void mouseEntered(MouseEvent e) {
//	    }
//
//	    public void mouseClicked(MouseEvent e) {
//	      if (SwingUtilities.isRightMouseButton(e))
//	        ScreenMirrorUI.this.mPopupMenu.show(e.getComponent(), e.getX(), e.getY());
//	    }
//	  };

	private WindowListener mWindowListener = new WindowListener() {
		public void windowOpened(WindowEvent arg0) {
		}

		public void windowIconified(WindowEvent arg0) {
		}

		public void windowDeiconified(WindowEvent arg0) {
		}

		public void windowDeactivated(WindowEvent arg0) {
		}

		public void windowClosing(WindowEvent arg0) {
			logger.info("Screen Mirror windowClosing");
			stopMonitor();
			dispose();
		}

		public void windowClosed(WindowEvent arg0) {
		}

		public void windowActivated(WindowEvent arg0) {
		}
	};

	public ScreenMirrorUI(String udid, String[] args) {
		this.udid = udid;
		initializeall(args);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setIconImage(MainRun.imagelogo);
	}

	// 开始线程
	public void startMonitor() {
		MonitorThread monitorThread = new MonitorThread();
		new Thread(monitorThread).start();
		logger.info("Start to mirror thread");
	}

	// 停止线程
	public void stopMonitor() {
		monitorthreadrunnable = false;
	}

	// 选择设备
	public void selectDevice() {
		stopMonitor();// 先停止

		SelectDeviceDialog dialog = new SelectDeviceDialog(this, true, CheckUE.getIOSDevices());
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
		if (dialog.isOK()) {
			selecteddevice = dialog.getSelectedValue();
			if (!selecteddevice.equals("")) {

				title = "屏幕镜像: " + selecteddevice;
				setTitle(title);
				setImage(null);
				logger.info("Start to mirror with device: " + selecteddevice);
			}
		}
		startMonitor();
	}

	public void setOrientation(boolean portrait) {
		if (this.mPortrait != portrait) {
			this.mPortrait = portrait;
			updateSize();
		}
	}

	public void setZoom(double zoom) {
		if (this.mZoom != zoom) {
			this.mZoom = zoom;
			updateSize();
		}
	}

	public void saveImage() {
		BufferedImage inImage = this.mPanel.getBufferedImage();
		if (inImage != null) {
			BufferedImage outImage = new BufferedImage((int) (inImage.getWidth() * 1), // z
					(int) (inImage.getHeight() * 1), inImage.getType());// z
			if (outImage != null) {
				AffineTransformOp op = new AffineTransformOp(AffineTransform.getScaleInstance(1, 1), 2);// z
				op.filter(inImage, outImage);
				JFileChooser fileChooser = new JFileChooser(MainRun.QALogfile);
				fileChooser.setFileFilter(new FileFilter() {
					public String getDescription() {
						return "*.png";
					}

					public boolean accept(File f) {
						String ext = f.getName().toLowerCase();
						return ext.endsWith(".png");
					}
				});
				if (fileChooser.showSaveDialog(this) != 0)
					return;
				try {
					File file = fileChooser.getSelectedFile();
					String path = file.getAbsolutePath();
					if (!path.endsWith(".png")) {
						file = new File(path + "." + "png");
					}
					ImageIO.write(outImage, "png", file);
				} catch (Exception e) {
					logger.error("Exception", e);
					JOptionPane.showMessageDialog(this, "保存图片失败!", "消息", JOptionPane.ERROR_MESSAGE);
				}
			}
		} else {
			JOptionPane.showMessageDialog(this, "图片为空!", "消息", JOptionPane.ERROR_MESSAGE);
		}
	}

//	  public void about()
//	  {
//	    AboutDialog dialog = new AboutDialog(this, true);
//	    dialog.setLocationRelativeTo(this);
//	    dialog.setVisible(true);
//	  }

	public void updateSize() {
		int height;
		int width;
		if (this.mPortrait) {
			width = this.mRawImageWidth;
			height = this.mRawImageHeight;
		} else {
			width = this.mRawImageHeight;
			height = this.mRawImageWidth;
		}
		Insets insets = getInsets();
		int newWidth = (int) (width * this.mZoom) + insets.left + insets.right;
		int newHeight = (int) (height * this.mZoom) + insets.top + insets.bottom;

		if ((getWidth() != newWidth) || (getHeight() != newHeight))
			setSize(newWidth, newHeight);
	}

	public void setImage(BufferedImage bufferedImage) {
		if (bufferedImage != null) {
			this.mRawImageWidth = bufferedImage.getWidth();
			this.mRawImageHeight = bufferedImage.getHeight();
		}
		this.mPanel.setBufferedImage(bufferedImage);
		updateSize();
	}

	private void initializeall(String[] args) {
//	    this.mADB = new GetDevices();
//	    if (!this.mADB.initialize()) {
//	      JOptionPane.showMessageDialog(this, "Init adb failed, pls try again", "消息", JOptionPane.ERROR_MESSAGE);
//	    }

		parseArgs(args);

		initializeFrame();
		initializePanel();
		initializeMenu();
		initializeActionMap();

		// addMouseListener(this.mMouseListener);
		addWindowListener(this.mWindowListener);

		pack();
		setImage(null);
	}

	private void parseArgs(String[] args) {
		if (args != null)
			for (int i = 0; i < args.length; ++i) {
				String arg = args[i];
				if (arg.equals("-a"))
					this.mAdjustColor = true;
			}
	}

	private void initializeFrame() {
		setTitle("Screen Mirror");
		// setIconImage(Toolkit.getDefaultToolkit().getImage(super.getClass().getResource("icon.png")));
		setDefaultCloseOperation(3);
		setResizable(false);
	}

	private void initializePanel() {
		this.mPanel = new MainPanel();
		add(this.mPanel);
	}

	private void initializeMenu() {
		this.mPopupMenu = new JPopupMenu();

		initializeSelectDeviceMenu();
		this.mPopupMenu.addSeparator();
		initializeOrientationMenu();
		initializeZoomMenu();
		initializeAdjustColor();
		this.mPopupMenu.addSeparator();
		initializeSaveImageMenu();
		this.mPopupMenu.addSeparator();
		initializeExit();

		this.mPopupMenu.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}

			public void popupMenuCanceled(PopupMenuEvent e) {
			}
		});
	}

	private void initializeSelectDeviceMenu() {
		JMenuItem menuItemSelectDevice = new JMenuItem("选择设备");
		menuItemSelectDevice.setMnemonic(68);
		menuItemSelectDevice.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ScreenMirrorUI.this.selectDevice();
			}
		});
		this.mPopupMenu.add(menuItemSelectDevice);
	}

	private void initializeOrientationMenu() {
		JMenu menuOrientation = new JMenu("显示方向");
		menuOrientation.setMnemonic(79);
		this.mPopupMenu.add(menuOrientation);

		ButtonGroup buttonGroup = new ButtonGroup();

		JRadioButtonMenuItem radioButtonMenuItemPortrait = new JRadioButtonMenuItem("竖屏");
		radioButtonMenuItemPortrait.setSelected(true);
		radioButtonMenuItemPortrait.setMnemonic(80);
		radioButtonMenuItemPortrait.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ScreenMirrorUI.this.setOrientation(true);
			}
		});
		buttonGroup.add(radioButtonMenuItemPortrait);
		menuOrientation.add(radioButtonMenuItemPortrait);

		JRadioButtonMenuItem radioButtonMenuItemLandscape = new JRadioButtonMenuItem("横屏");
		radioButtonMenuItemLandscape.setMnemonic(76);
		radioButtonMenuItemLandscape.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ScreenMirrorUI.this.setOrientation(false);
			}
		});
		buttonGroup.add(radioButtonMenuItemLandscape);
		menuOrientation.add(radioButtonMenuItemLandscape);
	}

	private void initializeZoomMenu() {
		JMenu menuZoom = new JMenu("放大");
		menuZoom.setMnemonic(90);// z
		this.mPopupMenu.add(menuZoom);

		ButtonGroup buttonGroup = new ButtonGroup();
		JRadioButtonMenuItem radioButtonMenuItemZoom25 = new JRadioButtonMenuItem("25%");
		radioButtonMenuItemZoom25.setMnemonic(50);
		radioButtonMenuItemZoom25.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ScreenMirrorUI.this.setZoom(0.25D);
			}
		});
		buttonGroup.add(radioButtonMenuItemZoom25);
		menuZoom.add(radioButtonMenuItemZoom25);

		JRadioButtonMenuItem radioButtonMenuItemZoom50 = new JRadioButtonMenuItem("50%");
		radioButtonMenuItemZoom50.setSelected(true);
		radioButtonMenuItemZoom50.setMnemonic(53);
		radioButtonMenuItemZoom50.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ScreenMirrorUI.this.setZoom(0.5D);
			}
		});
		buttonGroup.add(radioButtonMenuItemZoom50);
		menuZoom.add(radioButtonMenuItemZoom50);

		JRadioButtonMenuItem radioButtonMenuItemZoom75 = new JRadioButtonMenuItem("75%");
		radioButtonMenuItemZoom75.setMnemonic(55);
		radioButtonMenuItemZoom75.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ScreenMirrorUI.this.setZoom(0.75D);
			}
		});
		buttonGroup.add(radioButtonMenuItemZoom75);
		menuZoom.add(radioButtonMenuItemZoom75);

		JRadioButtonMenuItem radioButtonMenuItemZoom100 = new JRadioButtonMenuItem("100%");
		radioButtonMenuItemZoom100.setMnemonic(49);
		radioButtonMenuItemZoom100.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ScreenMirrorUI.this.setZoom(1.0D);
			}
		});
		buttonGroup.add(radioButtonMenuItemZoom100);
		menuZoom.add(radioButtonMenuItemZoom100);

		JRadioButtonMenuItem radioButtonMenuItemZoom150 = new JRadioButtonMenuItem("150%");
		radioButtonMenuItemZoom150.setMnemonic(48);
		radioButtonMenuItemZoom150.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ScreenMirrorUI.this.setZoom(1.5D);
			}
		});
		buttonGroup.add(radioButtonMenuItemZoom150);
		menuZoom.add(radioButtonMenuItemZoom150);

		JRadioButtonMenuItem radioButtonMenuItemZoom200 = new JRadioButtonMenuItem("200%");
		radioButtonMenuItemZoom200.setMnemonic(50);
		radioButtonMenuItemZoom200.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ScreenMirrorUI.this.setZoom(2.0D);
			}
		});
		buttonGroup.add(radioButtonMenuItemZoom200);
		menuZoom.add(radioButtonMenuItemZoom200);
		// radioButtonMenuItemZoomDIY
		JRadioButtonMenuItem radioButtonMenuItemZoomDIY = new JRadioButtonMenuItem("自定义");
		radioButtonMenuItemZoomDIY.setMnemonic(100);
		radioButtonMenuItemZoomDIY.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String input = JOptionPane.showInputDialog(mPanel, "请输入放大倍数(0.1-1):", "输入",
						JOptionPane.INFORMATION_MESSAGE);
				if (input != null) {
					if (!input.equals("") && HelperUtil.isDecimal(input) && Double.parseDouble(input) >= 0.1
							&& Double.parseDouble(input) <= 1) {
						ScreenMirrorUI.this.setZoom(Double.parseDouble(input));
						logger.info("DIY set zoom ok");
					} else {
						logger.info("Pls input numbers(0.1-1)");
						JOptionPane.showMessageDialog(mPanel, "请输入数字(0.1-1)!", "消息", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		buttonGroup.add(radioButtonMenuItemZoomDIY);
		menuZoom.add(radioButtonMenuItemZoomDIY);
	}

	private void initializeAdjustColor() {
		this.mAdjustColorCheckBoxMenuItem = new JCheckBoxMenuItem("调整颜色");
		this.mAdjustColorCheckBoxMenuItem.setMnemonic(74);
		this.mAdjustColorCheckBoxMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ScreenMirrorUI.this.mAdjustColor = (!ScreenMirrorUI.this.mAdjustColor);
				ScreenMirrorUI.this.mAdjustColorCheckBoxMenuItem.setSelected(ScreenMirrorUI.this.mAdjustColor);
			}
		});
		this.mPopupMenu.add(this.mAdjustColorCheckBoxMenuItem);
	}

	private void initializeSaveImageMenu() {
		JMenuItem menuItemSaveImage = new JMenuItem("保存图片");
		menuItemSaveImage.setMnemonic(83);
		menuItemSaveImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ScreenMirrorUI.this.saveImage();
			}
		});
		this.mPopupMenu.add(menuItemSaveImage);
	}

	private void initializeActionMap() {
		AbstractAction actionSelectDevice = new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 7652795901030387346L;

			public void actionPerformed(ActionEvent e) {
				ScreenMirrorUI.this.selectDevice();
			}
		};
		AbstractAction actionPortrait = new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -7802425625272697909L;

			public void actionPerformed(ActionEvent e) {
				ScreenMirrorUI.this.setOrientation(true);
			}
		};
		AbstractAction actionLandscape = new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -9037217420779194766L;

			public void actionPerformed(ActionEvent e) {
				ScreenMirrorUI.this.setOrientation(false);
			}
		};
		AbstractAction actionZoom50 = new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 5144141966641792268L;

			public void actionPerformed(ActionEvent e) {
				ScreenMirrorUI.this.setZoom(0.5D);
			}
		};
		AbstractAction actionZoom75 = new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -920425380175007708L;

			public void actionPerformed(ActionEvent e) {
				ScreenMirrorUI.this.setZoom(0.75D);
			}
		};
		AbstractAction actionZoom100 = new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -3633076645383962687L;

			public void actionPerformed(ActionEvent e) {
				ScreenMirrorUI.this.setZoom(1.0D);
			}
		};
		AbstractAction actionZoom150 = new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -5460204617603493638L;

			public void actionPerformed(ActionEvent e) {
				ScreenMirrorUI.this.setZoom(1.5D);
			}
		};
		AbstractAction actionZoom200 = new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -8406173340134964632L;

			public void actionPerformed(ActionEvent e) {
				ScreenMirrorUI.this.setZoom(2.0D);
			}
		};
		AbstractAction actionAdjustColor = new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 2447777163473373635L;

			public void actionPerformed(ActionEvent e) {
				ScreenMirrorUI.this.mAdjustColor = (!ScreenMirrorUI.this.mAdjustColor);
				ScreenMirrorUI.this.mAdjustColorCheckBoxMenuItem.setSelected(ScreenMirrorUI.this.mAdjustColor);
			}
		};
		AbstractAction actionSaveImage = new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 3848113313604233492L;

			public void actionPerformed(ActionEvent e) {
				ScreenMirrorUI.this.saveImage();
			}
		};
		AbstractAction actionExit = new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -9104295019178999589L;

			public void actionPerformed(ActionEvent e) {
			}
		};
		JComponent targetComponent = getRootPane();
		InputMap inputMap = targetComponent.getInputMap();

		inputMap.put(KeyStroke.getKeyStroke(68, 128), "Select Device");
		inputMap.put(KeyStroke.getKeyStroke(80, 128), "Portrait");
		inputMap.put(KeyStroke.getKeyStroke(76, 128), "Landscape");
		inputMap.put(KeyStroke.getKeyStroke(53, 128), "50%");
		inputMap.put(KeyStroke.getKeyStroke(55, 128), "75%");
		inputMap.put(KeyStroke.getKeyStroke(49, 128), "100%");
		inputMap.put(KeyStroke.getKeyStroke(48, 128), "150%");
		inputMap.put(KeyStroke.getKeyStroke(50, 128), "200%");
		inputMap.put(KeyStroke.getKeyStroke(74, 128), "Adjust Color");
		inputMap.put(KeyStroke.getKeyStroke(83, 128), "Save Image");
//	    inputMap.put(KeyStroke.getKeyStroke(65, 
//	      128), "About ASM");

		targetComponent.setInputMap(1, inputMap);

		targetComponent.getActionMap().put("Select Device", actionSelectDevice);
		targetComponent.getActionMap().put("Portrait", actionPortrait);
		targetComponent.getActionMap().put("Landscape", actionLandscape);
		targetComponent.getActionMap().put("Select Device", actionSelectDevice);
		targetComponent.getActionMap().put("50%", actionZoom50);
		targetComponent.getActionMap().put("75%", actionZoom75);
		targetComponent.getActionMap().put("100%", actionZoom100);
		targetComponent.getActionMap().put("150%", actionZoom150);
		targetComponent.getActionMap().put("200%", actionZoom200);
		targetComponent.getActionMap().put("Adjust Color", actionAdjustColor);
		targetComponent.getActionMap().put("Save Image", actionSaveImage);
		targetComponent.getActionMap().put("Cancle", actionExit);
	}

	private void initializeExit() {
		JMenuItem menuItemAbout = new JMenuItem("取消");
		menuItemAbout.setMnemonic(99);
		menuItemAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logger.info("Cancle button ScreenMirror");
			}
		});
		this.mPopupMenu.add(menuItemAbout);
	}

	public class MainPanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4096083365643509777L;
		private BufferedImage bufferedImage;

		public MainPanel() {
			setBackground(Color.BLACK);
			// 把鼠标设置成十字形
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			// setCursor 设置鼠标的形状 ，getPredefinedCursor()返回一个具有指定类型的光标的对象
			addMouseListener(new MouseA());// 添加鼠标事件
			addMouseMotionListener(new MouseB());
		}

		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (this.bufferedImage == null)
				return;
			int srcHeight;
			int srcWidth;

			if (ScreenMirrorUI.this.mPortrait) {
				srcWidth = ScreenMirrorUI.this.mRawImageWidth;
				srcHeight = ScreenMirrorUI.this.mRawImageHeight;
			} else {
				srcWidth = ScreenMirrorUI.this.mRawImageHeight;
				srcHeight = ScreenMirrorUI.this.mRawImageWidth;
			}
			int dstWidth = (int) (srcWidth * ScreenMirrorUI.this.mZoom);
			int dstHeight = (int) (srcHeight * ScreenMirrorUI.this.mZoom);
			if (ScreenMirrorUI.this.mZoom == 1.0D) {
				g.drawImage(this.bufferedImage, 0, 0, dstWidth, dstHeight, 0, 0, srcWidth, srcHeight, null);
			} else {
				Image image = this.bufferedImage.getScaledInstance(dstWidth, dstHeight, 4);
				if (image != null)
					g.drawImage(image, 0, 0, dstWidth, dstHeight, 0, 0, dstWidth, dstHeight, null);
			}
		}

		public void setBufferedImage(BufferedImage bufferedImage) {
			this.bufferedImage = bufferedImage;
			repaint();
		}

		public BufferedImage getBufferedImage() {
			return this.bufferedImage;
		}

		// TODO 鼠标事件MouseA类继承了MouseAdapter
		// 用来完成鼠标的响应事件的操作（鼠标的按下、释放、单击、移动、拖动、何时进入一个组件、何时退出、何时滚动鼠标滚轮 )
		class MouseA extends MouseAdapter {
			@Override
			public void mouseEntered(MouseEvent me) {
				// TODO 鼠标进入
				ScreenMirrorUI.this
						.setTitle("(" + (int) (me.getX() / mZoom) + "," + (int) (me.getY() / mZoom) + ")  " + title);
			}

			@Override
			public void mouseExited(MouseEvent me) {
				// TODO 鼠标退出
				ScreenMirrorUI.this
						.setTitle("(" + (int) (me.getX() / mZoom) + "," + (int) (me.getY() / mZoom) + ")  " + title);
			}

			@Override
			public void mousePressed(MouseEvent me) {
				// TODO 鼠标按下

			}

			@Override
			public void mouseReleased(MouseEvent me) {
				// TODO 鼠标松开
				ScreenMirrorUI.this.mPopupMenu.show(me.getComponent(), me.getX(), me.getY());
			}

		}

		// 鼠标事件MouseB继承了MouseMotionAdapter
		// 用来处理鼠标的滚动与拖动
		class MouseB extends MouseMotionAdapter {
			public void mouseDragged(MouseEvent me)// 鼠标的拖动
			{

			}

			public void mouseMoved(MouseEvent me)// 鼠标的移动
			{
				ScreenMirrorUI.this
						.setTitle("(" + (int) (me.getX() / mZoom) + "," + (int) (me.getY() / mZoom) + ")  " + title);
			}
		}
	}

	// ****************线程****************************************************************************
	BufferedImage tempbufferedImage;

	public class MonitorThread implements Runnable {
		public MonitorThread() {
		}

		public void run() {
			monitorthreadrunnable = true;
			if (selecteddevice.equals(""))
				return;
			// long imagecurrenttime;
			ExecutorService executor = Executors.newSingleThreadExecutor();
			Future<BufferedImage> future;
			// 在这里可以做别的任何事情
			// 同上面取得结果的代码
			while (monitorthreadrunnable == true) {
				// imagecurrenttime=System.currentTimeMillis();
				try {
					future = executor.submit(new Callable<BufferedImage>() {// 使用Callable接口作为构造参数
						public BufferedImage call() throws IOException {
							// 真正的任务在这里执行，这里的返回值类型为String，可以为任意类型
							return getDeviceImage();// 概率卡住
						}
					});
					tempbufferedImage = future.get(8000, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					logger.error("Exception", e);
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					logger.error("Exception", e);
				} catch (java.util.concurrent.TimeoutException e) {
					// TODO Auto-generated catch block
					// timeout
					logger.error("Exception", e);
					tempbufferedImage = null;
				}
				// time=System.currentTimeMillis()-imagecurrenttime;
//				if(time<500){
//					try {
//						Thread.sleep(500-time);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated c atch block
//						logger.error("Exception",e)
//					}
//				}
				if (tempbufferedImage == null && MainRun.adbBridge.getDevice(udid) != null) {
					logger.info("get tempfbImage timeout, restart!");
					stopMonitor();
					startMonitor();
					break;
				} else {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							setImage(tempbufferedImage);
						}
					});
				}
			}
			executor.shutdown();
			logger.info("end mirrorui thread.");
		}
	}

	/**
	 * 得到截图
	 * 
	 * @return
	 */
	private BufferedImage getDeviceImage() {
		BufferedImage bimg = null;
		String picpath = MainRun.datalocation + "/tempmirror.png";
		String[] strings_Pull = Excute.execcmd(udid,
				(MainRun.OStype == Cconfig.WINDOWS ? "" : MainRun.paramsBean.getMACcmd() + "/") + CIOSCMD.SCREEN_CAP_IOS
						.replaceAll("#udid#", selecteddevice).replaceAll("#savepath#", picpath.replaceAll("\\\\", "/")),
				CIOSCMD.SYSCMD, true);
		if (strings_Pull[0].contains("Screenshot saved") && (new File(picpath).exists())) {
			File picfile = new File(picpath);
			try {
				bimg = ImageIO.read(picfile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				logger.error("EXCEPTION", e);
			}
		}
		return bimg;
	}

}

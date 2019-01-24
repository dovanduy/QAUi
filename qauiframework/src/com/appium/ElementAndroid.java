package com.appium;

import java.awt.Color;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constant.CAndroidCMD;
import com.constant.Cconfig;
import com.constant.Coperation;
import com.helper.ADBUtil;
import com.review.getscreen.AndroidShot;

import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.ElementOption;
import io.appium.java_client.touch.offset.PointOption;

public class ElementAndroid extends BaseElement {
	Logger logger = LoggerFactory.getLogger(ElementAndroid.class);
	AndroidDriver<WebElement> driver;

	public ElementAndroid(WebElement webElement, By by, String Text, Map<String, Object> elementMap) {
		// TODO Auto-generated constructor stub
		super(webElement, by, Text, elementMap);
		this.driver = (AndroidDriver<WebElement>) elementMap.get("Driver");
		this.Shot = (AndroidShot) elementMap.get("Shot");
	}

	// @Override
	// public boolean zoom(){
	// if(webElement!=null){
	// Point point=webElement.getLocation();
	// Dimension dimension=webElement.getSize();
	// Shot.drawRect(Cappium.ZOOM,point.getX(),point.getY(),dimension.getWidth(),dimension.getHeight());//操作后界面变化
	// oplog.logStep("放大("+Text+")");
	// driver.zoom(webElement);
	// return true;
	// }else{
	// oplog.logInfo("放大失败("+Text+")");
	// return false;
	// }
	// }
	// @Override
	// public boolean pinch(){
	// if(webElement!=null){
	// Point point=webElement.getLocation();
	// Dimension dimension=webElement.getSize();
	// Shot.drawRect(Cappium.PINCH,point.getX(),point.getY(),dimension.getWidth(),dimension.getHeight());//操作后界面变化
	// oplog.logStep("缩小("+Text+")");
	// driver.pinch(webElement);
	// return true;
	// }else{
	// oplog.logInfo("缩小失败("+Text+")");
	// return false;
	// }
	// }
	@Override
	public ElementAndroid findElement(By by) {
		// TODO Auto-generated method stub
		Text = Text + "//" + translation.getName(by);
		if (webElement != null) {
			try {
				webElement = webElement.findElement(by);
			} catch (RuntimeException e) {
				// TODO: handle exception
				webElement = null;
				logger.warn("Exception", e);
				oplog.logWarn("未找到子元素:" + Text);
				Shot.drawText(Coperation.NOT_FIND_CHILDELEMENT, "未找到子元素:" + Text);
			}
		}
		return this;
	}

	@Override
	public ElementAndroid findElementByElements(By by, int index) {
		// TODO Auto-generated method stub
		Text = Text + "//" + translation.getName(by) + "元素组," + index;
		if (webElement != null) {
			try {
				List<WebElement> webElements = webElement.findElements(by);
				if (index <= webElements.size() - 1) {
					webElement = webElements.get(index);
				} else {
					webElement = null;
					oplog.logWarn("(" + Text + ")只有" + webElements.size() + "个元素");
				}
			} catch (RuntimeException e) {
				// TODO: handle exception
				webElement = null;
				logger.warn("Exception", e);
				oplog.logWarn("未找到子元素:" + Text);
				Shot.drawText(Coperation.NOT_FIND_CHILDELEMENT, "未找到子元素:" + Text);
			}
		}
		return this;
	}

	@Override
	public List<ElementAndroid> findElements(By by) {
		// TODO Auto-generated method stub
		Text = Text + "//" + translation.getName(by) + "元素组";
		List<ElementAndroid> elements = new ArrayList<>();
		if (webElement != null) {
			List<WebElement> webElements = webElement.findElements(by);
			if (webElements.size() == 0) {
				webElement = null;
				oplog.logWarn("未找到子元素组:" + Text);
			}
			for (WebElement webElement : webElements) {
				elements.add(new ElementAndroid(webElement, by, Text, elementMap));
			}
		}
		return elements;
	}

	@Override
	public boolean longtap(int duration) {
		String picpath = null;
		try {
			if (initInfo()) {
				// Point point = webElement.getLocation();
				// Dimension dimension = webElement.getSize();
				picpath = Shot.drawRect(Coperation.LONGTAP, point.getX(), point.getY(), dimension.getWidth(),
						dimension.getHeight(), Color.decode(Cconfig.GREEN_DEEP));// 操作后界面变化
				oplog.logStep(Coperation.LONGTAP + "(" + Text + ")," + duration + "毫秒");
				// driver.tap(fingers, webElement, duration);
				new TouchAction(driver).press(ElementOption.element(webElement))
						.waitAction(WaitOptions.waitOptions(Duration.ofMillis(duration))).release().perform();
				return true;
			}
		} catch (Exception e) {
			oplog.logWarn(Coperation.OPEXCEPITON);
			if (picpath != null)
				Shot.drawTextPicture(picpath, Coperation.LONGTAP + "失败", false);
		}
		oplog.logInfo(Coperation.LONGTAP + "失败(" + Text + ")");
		return false;
	}

	@Override
	public boolean dragTo(int addx, int addy) {
		String picpath = null;
		addx = X_multiple(addx);
		addy = Y_multiple(addy);
		try {
			if (initInfo()) {
				// Point point = webElement.getLocation();
				// Dimension dimension = webElement.getSize();
				int startx = point.getX() + dimension.getWidth() / 2;
				int starty = point.getY() + dimension.getHeight() / 2;
				int endx = startx + addx;
				int endy = starty + addy;
				picpath = Shot.drawArrow(Coperation.SWIPE, startx, starty, endx, endy);
				oplog.logStep(Coperation.DRAGTO + ":" + Text + ",从(" + startx + "," + starty + ")到(" + endx + "," + endy
						+ ")");
				new TouchAction(driver).press(PointOption.point(startx, starty))
						.waitAction(WaitOptions.waitOptions(Duration.ofMillis(500)))
						.moveTo(PointOption.point(endx, endy)).release().perform();
				return true;
			}
		} catch (Exception e) {
			oplog.logWarn(Coperation.OPEXCEPITON);
			if (picpath != null)
				Shot.drawTextPicture(picpath, Coperation.DRAGTO + "失败", false);
		}
		oplog.logInfo(Coperation.DRAGTO + "失败(" + Text + ")");
		return false;
	}

	@Override
	public boolean dragTo(BaseElement elementAndroid) {
		String picpath = null;
		try {
			if (initInfo()) {
				int startx = point.getX() + dimension.getWidth() / 2;
				int starty = point.getY() + dimension.getHeight() / 2;
				int endx = elementAndroid.getPosition()[0];
				int endy = elementAndroid.getPosition()[1];
				picpath = Shot.drawArrow(Coperation.SWIPE, startx, starty, endx, endy);
				oplog.logStep(Coperation.DRAGTO + ":" + Text + ",从(" + startx + "," + starty + ")到(" + endx + "," + endy
						+ ")");
				new TouchAction(driver).press(PointOption.point(startx, starty))
						.waitAction(WaitOptions.waitOptions(Duration.ofMillis(1000)))
						.moveTo(PointOption.point(endx, endy)).release().perform();
				return true;
			}
		} catch (Exception e) {
			oplog.logWarn(Coperation.OPEXCEPITON);
			if (picpath != null)
				Shot.drawTextPicture(picpath, Coperation.DRAGTO + "失败", false);
		}
		oplog.logInfo(Coperation.DRAGTO + "失败(" + Text + ")");
		return false;
	}

	@Override
	public boolean exist() {
		if (webElement != null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 如果该元素不存在(说明已进入目标界面),则返回
	 * 
	 * @return
	 */
	// public boolean Back() {
	// if (!exist()) {
	// Shot.drawText("返回", "按下BACK按钮");
	// oplog.logStep(Text + "元素不存在,按下BACK按钮");
	// driver.pressKeyCode(AndroidKeyCode.BACK);
	// return true;
	// } else {
	// oplog.logWarn("元素" + Text + "仍存在,未进入目标界面,则不" + Coperation.BACK);
	// }
	// return false;
	// }

	/**
	 * 点击
	 * 
	 * @return
	 */
	public boolean click() {
		String picpath = null;
		try {
			if (initInfo()) {
				// Point point = webElement.getLocation();
				// Dimension dimension = webElement.getSize();
				picpath = Shot.drawRect(Coperation.CLICK, point.getX(), point.getY(), dimension.getWidth(),
						dimension.getHeight());// 操作后界面变化
				oplog.logStep(Coperation.CLICK + "(" + Text + ")");
				ADBUtil.execcmd(udid, CAndroidCMD.INPUT_TAP.replace("#x#", getPosition()[0] + "").replace("#y#",
						getPosition()[1] + ""));
				// new
				// TouchAction(driver).press(point.getX()+dimension.getWidth()/2,point.getY()+dimension.getHeight()/2).release().perform();
				return true;
			}
		} catch (Exception e) {
			oplog.logWarn(Coperation.OPEXCEPITON);
			if (picpath != null)
				Shot.drawTextPicture(picpath, Coperation.CLICK + "失败", false);
		}
		oplog.logInfo(Coperation.CLICK + "失败(" + Text + ")");
		return false;
	}

	/**
	 * 点击
	 * 
	 * @param times       点击次数
	 * @param millisecond 毫秒,每次点击间隔,最大8000ms
	 * @return
	 */
	public boolean click(int times, int millisecond) {
		String picpath = null;
		if (millisecond > 8000)
			millisecond = 8000;
		if (millisecond < 0)
			millisecond = 0;
		int count = 0;
		try {
			if (initInfo()) {
//				 Point point = webElement.getLocation();
//				 Dimension dimension = webElement.getSize();
				picpath = Shot.drawRect(Coperation.CLICK, point.getX(), point.getY(), dimension.getWidth(),
						dimension.getHeight());// 操作后界面变化
				oplog.logStep(Coperation.CLICK + "(" + Text + ")," + times + "次,间隔" + millisecond + "ms");
				if (times < 1)
					times = 1;
				for (int i = 0; i < times; i++) {
//					new TouchAction(driver).press(PointOption.point(getPosition()[0], getPosition()[1])).release()
//							.perform();//Coordinate [x=940.0, y=1824.0] is outside of element rect: [0,0][1080,1794]
					ADBUtil.execcmd(udid, CAndroidCMD.INPUT_TAP.replace("#x#", getPosition()[0] + "").replace("#y#",
							getPosition()[1] + ""));
					Thread.sleep(millisecond);
					count++;
				}
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			oplog.logWarn(Coperation.OPEXCEPITON + ",已点击" + count + "次");
			if (picpath != null)
				Shot.drawTextPicture(picpath, Coperation.CLICK + "失败", false);
		}
		oplog.logInfo(Coperation.CLICK + "失败(" + Text + ")," + times + "次");
		return false;
	}

	/**
	 * 双击
	 * 
	 * @return
	 */
	@Deprecated
	public boolean doubleclick() {
		String picpath = null;
		try {
			if (initInfo()) {
				// Point point = webElement.getLocation();
				// Dimension dimension = webElement.getSize();
				// logger.info(point.getX()+" "+point.getY()+" "+dimension.getWidth()+"
				// "+dimension.getHeight());
				picpath = Shot.drawRect(Coperation.DOUBLECLICK, point.getX(), point.getY(), dimension.getWidth(),
						dimension.getHeight());// 操作后界面变化
				oplog.logStep(Coperation.DOUBLECLICK + "(" + Text + ")");
				webElement.click();
				webElement.click();
				return true;
			}
		} catch (Exception e) {
			oplog.logWarn(Coperation.OPEXCEPITON);
			if (picpath != null)
				Shot.drawTextPicture(picpath, Coperation.DOUBLECLICK + "失败", false);
		}
		oplog.logInfo(Coperation.DOUBLECLICK + "失败(" + Text + ")");
		return false;
	}

	@Override
	public String getText() {
		if (initInfo()) {
			return webElement.getAttribute("text");
		}
		oplog.logWarn("未获取元素属性Text,返回\"null\"");
		return "null";
	}
}
package com.bsoft.controller;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.bsoft.dao.sqlite.impl.SqliteDAOImpl;


/**
 * 
 *@author zhuzz
 * @类功能说明:验证码
 */
@Controller
public class VCodeConroller extends JSONOutputMVCConroller{
	private static final Log logger = LogFactory.getLog(VCodeConroller.class);
	
	@Autowired
	@Qualifier("sqliteDao")
	SqliteDAOImpl sqliteDao;
	
	// 验证码图片的宽度。  
    private int width = 80;  
    // 验证码图片的高度。  
    private int height = 20;  
    // 验证码字符个数  
    private int codeCount = 4;  
  
    private int x = 0;  
    // 字体高度  
    private int fontHeight;  
    private int codeY;  
  
    char[] codeSequence = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',  
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',  
            'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
    /**
     * 
     * @方法说明：登录获取验证码
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/validateCode.action", method = RequestMethod.GET)
	public void validateCode(HttpServletRequest request,
			HttpServletResponse response)
			throws IOException {
		HttpSession session = request.getSession();
		x = width / (codeCount + 1);
		fontHeight = height - 2;
		codeY = height - 4;

		// 定义图像buffer
		BufferedImage buffImg = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = buffImg.createGraphics();

		// 创建一个随机数生成器类
		Random random = new Random();

		// 将图像填充为白色
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);

		// 创建字体，字体的大小应该根据图片的高度来定。
		Font font = new Font("Fixedsys", Font.PLAIN, fontHeight);
		// 设置字体。
		g.setFont(font);

		// 画边框。
		g.setColor(Color.BLACK);
		g.drawRect(0, 0, width - 1, height - 1);

		// 随机产生20条干扰线，使图象中的认证码不易被其它程序探测到。
		g.setColor(Color.BLACK);
		for (int i = 0; i < 6; i++) {
			int x = random.nextInt(width);
			int y = random.nextInt(height);
			int xl = random.nextInt(12);
			int yl = random.nextInt(12);
			g.drawLine(x, y, x + xl, y + yl);
		}

		// randomCode用于保存随机产生的验证码，以便用户登录后进行验证。
		StringBuffer randomCode = new StringBuffer();
		int red = 0, green = 0, blue = 0;

		// 随机产生codeCount数字的验证码。
		for (int i = 0; i < codeCount; i++) {
			// 得到随机产生的验证码数字。
			String strRand = String.valueOf(codeSequence[random.nextInt(36)]);
			// 产生随机的颜色分量来构造颜色值，这样输出的每位数字的颜色值都将不同。
			red = random.nextInt(128);
			green = random.nextInt(128);
			blue = random.nextInt(128);

			// 用随机产生的颜色将验证码绘制到图像中。
			g.setColor(new Color(red, green, blue));
			g.drawString(strRand, (i) * x + 8 + 2 * i, codeY);

			// 将产生的四个随机数组合在一起。
			randomCode.append(strRand);
		}
		// 将四位数字的验证码保存到Session中。
		session.setAttribute("validateCodeRecruit", randomCode.toString()
				.toUpperCase());

		// 禁止图像缓存。
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);

		response.setContentType("image/jpeg");

		// 清空缓存
		g.dispose();

		// 将图像输出到Servlet输出流中。
		ServletOutputStream sos = response.getOutputStream();
		ImageIO.write(buffImg, "jpeg", sos);
		sos.close();
	}
    
    @RequestMapping(value = "/validate.action", method = RequestMethod.POST)
	public void validate(
			@RequestParam(value = "user") String userId,
			@RequestParam(value = "password") String password,
			@RequestParam(value = "validateCode") String validateCode,
			HttpServletRequest request, HttpServletResponse response)
					throws IOException {
		HttpSession session = request.getSession();
		HashMap<String, Object> resData = new HashMap<String, Object>();
		String msg = "LoginSucess";
		int code = 200;
		try {
			Boolean user = sqliteDao.getUser(userId);//验证用户是否存在
			if(user == false){
				code = 504;
				msg = "当前用户不存在";
			}else {
				if(!sqliteDao.validatePassword(userId, password)){//验证密码是否正确
					code = 501;
					msg = "密码输入错误";
				}else if(!validateCode.toUpperCase().equals(session.getAttribute("validateCodeRecruit").toString())) {
					code = 503;
					msg = "验证码错误";
				}else {
					session.setAttribute("user", user);
					session.setAttribute("uid", userId);
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			resData.put("code", code);
			resData.put("msg", msg);
			jsonOutput(response, resData, false);
		}
	}
    
    @RequestMapping(value = "/login.action", method = RequestMethod.POST)
	public void login(
			HttpServletRequest request, HttpServletResponse response)
					throws IOException {
		response.sendRedirect("html/main.htm");
	}
}

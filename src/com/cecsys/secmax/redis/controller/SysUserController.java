package com.cecsys.secmax.redis.controller;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.cecsys.secmax.redis.bean.User;
import com.cecsys.secmax.redis.service.UserService;





/**
 * 最近开发一个系统，有个需求就是，忘记密码后通过邮箱找回。现在的系统在注册的时候都会强制输入邮箱，其一目的就是通过邮件绑定找回，可以进行密码找回。
 * 通过java发送邮件的功能我就不说了，重点讲找回密码。参考别人的思路：发送邮件→请求邮件里的URL→验证url→{验证成功修改密码，不成功跳转到失败页面}
 * 重点就是如何生成这个url和如何解析这个url.
 * 需要注意的是一个url只能修改一次密码,当同一帐号发送多封邮件,只有最后一封邮件的url加密能防止伪造攻击,一次url只能验证一次，并且绑定了用户。
 * 生成url: 可以用UUID生成随机密钥。数字签名 = MD5(用户名+'$'+过期时间+‘$'+密钥key)
 * 数据库字段(用户名(主键),密钥key,过期时间) url参数(用户名,数字签名)
 * ,密钥key的生成：在每一个用户找回密码时候为这个用户生成一个密钥key ，url example:
 * http://localhost:8080/user/reset_password?sid=
 * D622D6A23FBF86FFE696B593D55351A54AEAEA77&userName=test4
 * 生成过期时间,生成数字签名,生成url,发送邮件. saveOrUpdate(用户名,密钥key,过期时间)
 * 补充1：Timestamp类型对象在保存到数据的时候毫秒精度会丢失。
 * 比如:2013-10-08 10:29:10.234 存到mysql数据库的时候 变成 2013-10-0810:29:10.0。
 * 时间变得不相同了,sid 匹配的时候不会相等。所以我做了忽略精度的操作。
 * 补充2：解决linux下面title中文乱码
 * sun.misc.BASE64Encoder enc = new sun.misc.BASE64Encoder();
 * mailMessage.setSubject(MimeUtility.encodeText(mailInfo.getSubject(), "UTF-8", "B"));//解决linux邮件title乱码
 * 补充3:怎么不直接把sid插入到user表呢。验证的时候直接比较sid就ok了。
 * 
 * @author lingzg
 *
 */
public class SysUserController {

	private UserService userService;
	@RequestMapping(value = "/user/i_forget_password")
	@ResponseBody
	public Map forgetPass(HttpServletRequest request, String userName) {
		User users = userService.findUserByName(userName);
		Map map = new HashMap<String, String>();
		String msg = "";
		if (users == null) { // 用户名不存在
			msg = "用户名不存在,你不会忘记用户名了吧?";
			map.put("msg", msg);
			return map;
		}
		try {
			String secretKey = UUID.randomUUID().toString(); // 密钥
			Timestamp outDate = new Timestamp(System.currentTimeMillis() + 30 * 60 * 1000);// 30分钟后过期
			long date = outDate.getTime() / 1000 * 1000; // 忽略毫秒数
			users.setValidataCode(secretKey);
			users.setRegisterDate(outDate);
			userService.update(users); // 保存到数据库
			String key = users.getUserName() + "$" + date + "$" + secretKey;
			String digitalSignature = getMD5(key); // 数字签名

			String emailTitle = "有方云密码找回";
			String path = request.getContextPath();
			String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
					+ path + "/";
			String resetPassHref = basePath + "user/reset_password?sid=" + digitalSignature + "&userName="
					+ users.getUserName();
			String emailContent = "请勿回复本邮件.点击下面的链接,重设密码<br/><a href=" + resetPassHref + " target='_BLANK'>点击我重新设置密码</a>"
					+ "<br/>tips:本邮件超过30分钟,链接将会失效，需要重新申请'找回密码'" + key + "\t" + digitalSignature;
			System.out.print(resetPassHref);
//			SendMail.getInstatnce().sendHtmlMail(emailTitle, emailContent, users.getEmail());
			msg = "操作成功,已经发送找回密码链接到您邮箱。请在30分钟内重置密码";
//			logInfo(request, userName, "申请找回密码");
		} catch (Exception e) {
			e.printStackTrace();
			msg = "邮箱不存在？未知错误,联系管理员吧。";
		}
		map.put("msg", msg);
		return map;
	}

	@RequestMapping(value = "/user/reset_password", method = RequestMethod.GET)
	public ModelAndView checkResetLink(String sid, String userName) {
		ModelAndView model = new ModelAndView("error");
		String msg = "";
		if (sid.equals("") || userName.equals("")) {
			msg = "链接不完整,请重新生成";
			model.addObject("msg", msg);
//			logInfo(userName, "找回密码链接失效");
			return model;
		}
		User users = userService.findUserByName(userName);
		if (users == null) {
			msg = "链接错误,无法找到匹配用户,请重新申请找回密码.";
			model.addObject("msg", msg);
//			logInfo(userName, "找回密码链接失效");
			return model;
		}
		Timestamp outDate = users.getRegisterDate();
		if (outDate.getTime() <= System.currentTimeMillis()) { // 表示已经过期
			msg = "链接已经过期,请重新申请找回密码.";
			model.addObject("msg", msg);
//			logInfo(userName, "找回密码链接失效");
			return model;
		}
		String key = users.getUserName() + "$" + outDate.getTime() / 1000 * 1000 + "$" + users.getValidataCode(); // 数字签名
		String digitalSignature = getMD5(key);
		System.out.println(key + "\t" + digitalSignature);
		if (!digitalSignature.equals(sid)) {
			msg = "链接不正确,是否已经过期了?重新申请吧";
			model.addObject("msg", msg);
//			logInfo(userName, "找回密码链接失效");
			return model;
		}
		model.setViewName("user/reset_password"); // 返回到修改密码的界面
		model.addObject("userName", userName);
		return model;
	}
	
	public static String getMD5(String str) {
        try {
            // 生成一个MD5加密计算摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算md5函数
            md.update(str.getBytes());
            System.out.println("str:"+str);
            System.out.println("digest:"+md.digest());
            // digest()最后确定返回md5 hash值，返回值为8为字符串。因为md5 hash值是16位的hex值，实际上就是8位的字符
            // BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值
            return new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {
            e.printStackTrace();
            return str;
        }
    }
}

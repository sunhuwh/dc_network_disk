package com.dcnetworkdisk.user.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import com.dcnetworkdisk.common.constant.Constants;
import com.dcnetworkdisk.common.vo.OutputWrapper;
import com.dcnetworkdisk.common.vo.UserOutput;
import com.dcnetworkdisk.user.dao.UserDao;
import com.dcnetworkdisk.user.model.User;
import com.dcnetworkdisk.web.utils.WebCache;

@Service
public class UserService {

	@Autowired
	private WebCache webCache;
	
	@Autowired
	private UserDao userDao;
	
	public OutputWrapper<UserOutput> getUserInfo(int uid){
		OutputWrapper<UserOutput> output = new OutputWrapper<UserOutput>();
		UserOutput userOutput = new UserOutput(1,"董龙成","123456","IMG001.jpg","what the hell?");
		output.setResult(userOutput);
		return output;
	}

	/**
	 * 获取用户信息
	 * @param secureToken
	 * @return
	 */
	public ModelAndView getUserInfo_web(String secureToken){
		ModelAndView modelAndView = new ModelAndView();
		String username = webCache.getUsername(secureToken);
		if(username != null){
			User user = userDao.findByUsername(username);
			if(user != null){
				modelAndView.addObject("success", true);
				modelAndView.addObject("user",user);
				return modelAndView;
			}
			else{
				modelAndView.addObject("success", false);
				modelAndView.addObject("errorMsg", "no such user.");
				return modelAndView;
			}
		}
		else{
			modelAndView.addObject("success", false);
			modelAndView.addObject("errorMsg", "token expired.");
			return modelAndView;
		}
	}
	
	public LoginOutput ensureUser(String username, String password){
		LoginOutput output = new LoginOutput();
		if(userDao.ensureUser(username, password) == null){
			output.setLoginCode(300);
			output.setLoginMsg(Constants.LOGINOUTPUT_WRONG_PASSWORD);
			return output;
		}
		String secureToken = webCache.getSecureToken(username);
		if(secureToken == null){
			secureToken = Integer.toHexString(username.hashCode()) + Long.toHexString(UUID.randomUUID().getMostSignificantBits());
			webCache.setSecureToken(username, secureToken);
		}
		webCache.setUserContext(secureToken, username);
		output.setLoginCode(200);
		output.setLoginMsg(Constants.LOGINOUTPUT_SUCCESS);
		output.setToken(secureToken);
		return output;
	}
	
	public QuotaOutput getQuota(String secureToken){
		QuotaOutput output = new QuotaOutput();
		String username = webCache.getUsername(secureToken);
		if(username == null){
			return null;
		}
		output.setQuota(BigDecimal.valueOf(2000000000));
		output.setUsed(BigDecimal.valueOf(500000000));
		return output;
	}
	
}

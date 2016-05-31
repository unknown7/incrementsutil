package com.proxy.annotations;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;

import com.service.Service;
import com.service.Service.Config;

public class AnnotationHandler {

	public static void updateProps (Service service) {
		try {
			Class<?> classType = service.getClass();
			Field field = classType.getDeclaredField("prop");
			String fieldName = field.getName();
			String firstLetter = fieldName.substring(0, 1).toUpperCase();
			String getMethodName = "get" + firstLetter + fieldName.substring(1);
			Method getMethod = classType.getMethod(getMethodName);
			Properties prop = (Properties) getMethod.invoke(service);
			FileOutputStream fos = new FileOutputStream(Config.COFNIG.getPath());
			prop.store(fos, "");
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void readProps (Service service) {
		try {
			Class<?> classType = service.getClass();
			Field field = classType.getDeclaredField("prop");
			Class<?> propClass = field.getType();
			Properties prop = (Properties) propClass.newInstance();
			FileInputStream fis = new FileInputStream(Config.COFNIG.getPath());
			prop.load(fis);
			fis.close();
			String fieldName = field.getName();
			String firstLetter = fieldName.substring(0, 1).toUpperCase();
			String setMethodName = "set" + firstLetter + fieldName.substring(1);
			Method setMethod = classType.getMethod(setMethodName, propClass);
			setMethod.invoke(service, prop);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static AnnoType getAnnoType (Service service, Method method) {
		try {
			Class<?> classType = service.getClass();
			Method methodImpl = classType.getMethod(method.getName(), method.getParameterTypes());
			if (methodImpl.isAnnotationPresent(ReadProperties.class))
				return AnnoType.READ;
			else if (methodImpl.isAnnotationPresent(UpdateProperties.class))
				return AnnoType.UPDATE;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return AnnoType.OTHER;
	}
}

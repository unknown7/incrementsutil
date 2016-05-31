package com.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.JTextField;

import com.proxy.annotations.ReadProperties;
import com.proxy.annotations.UpdateProperties;
import com.service.Service;
import com.utils.StringUtils;

public class ServiceImpl implements Service {
	private String projectName;
	private Properties prop;

	public void setProp(Properties prop) {
		this.prop = prop;
	}

	public Properties getProp() {
		return prop;
	}

	public void createIncrementsFolder(String filePath, String projectPath, String createPath) throws Exception {
		projectName = projectPath.substring(projectPath.lastIndexOf(SpecialSymbol.SLANT.getSymbol()) + 1, projectPath.length());
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		String line = br.readLine();
		while (StringUtils.isNotBlank(line)) {
			String packagePath = makePackagePath(line);
			String classPath = projectPath + packagePath;
			String[] split = classPath.substring(classPath.indexOf(projectName), classPath.length()).split(SpecialSymbol.SLANT.getSymbol());
			StringBuffer curPath = new StringBuffer(createPath + SpecialSymbol.SLANT.getSymbol());
			for (int i = 0; i < split.length; i++) {
				String curStr = split[i];
				curPath.append(curStr);
				switch (toType(i, split)) {
				case FILE:
					if (!curStr.contains("."))
						continue;
					try {
						copyFile(classPath, curPath.toString());
						//若当前文件是.class文件，遍历当前目录，寻找是否存在当前文件的内部类文件
						if (FileType.JAVA.equals(fileTypeAfterCompile(curStr)) || FileType.TEST_JAVA.equals(fileTypeAfterCompile(curStr))) {
							File parentFile = new File(classPath).getParentFile();
							for (File f : parentFile.listFiles()) {
								if (f.getName().contains(curStr.substring(0, curStr.length() - 6) + SpecialSymbol.INNER_CLASS_SEPARATOR.getSymbol())) {
									classPath = classPath.replace(curStr, f.getName());
									copyFile(classPath, curPath.toString().replace(curStr, f.getName()));
								}
							}
						}
					} catch (FileNotFoundException e) {
						e.printStackTrace();
						continue;
					}
					break;
				case DIR:
					File dir = new File(curPath.toString());
					dir.mkdir();
					curPath.append(SpecialSymbol.SLANT.getSymbol());
					break;
				default:
					break;
				}
			}
			line = br.readLine();
		}
		br.close();
	}
	
	@Override
	@UpdateProperties
	public void updateProps(String projectPath, String createPath) {
		prop.setProperty(Config.PROJECTPATH.getPath(), projectPath);
		prop.setProperty(Config.CREATEPATH.getPath(), createPath);
	}
	
	@Override
	@ReadProperties
	public void fillInText(JTextField projectPath, JTextField createPath) {
		projectPath.setText(prop.getProperty(Config.PROJECTPATH.getPath()));
		createPath.setText(prop.getProperty(Config.CREATEPATH.getPath()));
	}
	
	private Type toType (int index, String[] split) {
		return split.length > 1
				?
					(index == split.length - 1 ? Type.FILE : Type.DIR)
				:
					Type.DIR;
	}
	
	private FileType fileTypeBeforeCompile (String line) {
		for (FileType fileType : FileType.values())
			if (line.contains(fileType.getMatcherBeforeCompile()))
				return fileType;
		return FileType.NULL;
	}
	
	private FileType fileTypeAfterCompile (String line) {
		for (FileType fileType : FileType.values())
			if (line.contains(fileType.getMatcherAfterCompile()))
				return fileType;
		return FileType.NULL;
	}

	private void copyFile(String oldPath, String newPath) throws IOException {
		InputStream inStream = new FileInputStream(oldPath); // 读入原文件
		FileOutputStream fs = new FileOutputStream(newPath);
		int byteread = 0;
		File oldfile = new File(oldPath);
		if (oldfile.exists()) { // 文件存在时
			byte[] buffer = new byte[1024];
			while ((byteread = inStream.read(buffer)) != -1)
				fs.write(buffer, 0, byteread);
			inStream.close();
		}
		inStream.close();
		fs.close();
	}
	
	private String makePackagePath (String line) {
		String packagePath = "";
		switch (fileTypeBeforeCompile(line)) {
		case JAVA:
			if (line.contains("Mapper.xml") || !line.substring(line.lastIndexOf(SpecialSymbol.SLANT.getSymbol()), line.length()).contains("."))
				packagePath = ("/WEB-INF/classes" + line.substring(line.indexOf("/com/"), line.length()));
			else
				packagePath = ("/WEB-INF/classes" + line.substring(line.indexOf("/com/"), line.length() - 5) + FileType.JAVA.getMatcherAfterCompile());
			break;
		case TEST_JAVA:
			packagePath = ("/WEB-INF/classes" + line.substring(line.indexOf("/test/"), line.length() - 5));
			break;
		case CONFIG:
			packagePath = ("/WEB-INF/classes/" + line.substring(line.indexOf("/src/") + 5, line.length()));
			break;
		case FILE:
			packagePath = (line.substring(line.indexOf(projectName) + projectName.length() + FileType.FILE.getMatcherBeforeCompile().length() - 1, line.length()));
			break;
		default:
			break;
		}
		return packagePath;
	}
}

package org.test.web.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.yx.exception.BizException;
import org.yx.http.EncryptType;
import org.yx.http.HttpHeadersHolder;
import org.yx.http.HttpSessionHolder;
import org.yx.http.Upload;
import org.yx.http.Web;
import org.yx.http.handler.UploadFile;
import org.yx.http.handler.UploadFileHolder;
import org.yx.rpc.Soa;
import org.yx.validate.Param;

public class PlainServer {
	
	@Soa("a.b.repeat")
	public String repeat(String s){
		return s;
	}

	@Web(value = "echo")
	@Soa
	public List<String> echo(String echo, List<String> names) {
		List<String> list = new ArrayList<>();
		for (String name : names) {
			list.add(echo + " " + name);
		}
		return list;
	}

	@Web(value = "base64", requestEncrypt = EncryptType.BASE64, responseEncrypt = EncryptType.BASE64)
	public List<String> base64(@Param(maxLength = 20) String echo, List<String> names) {
		List<String> list = new ArrayList<>();
		for (String name : names) {
			list.add(echo + " " + name);
		}
		return list;
	}

	@Web(value = "upload", requestEncrypt = EncryptType.BASE64)
	@Upload
	public String upload(String name, @Param(required = true) Integer age) throws FileNotFoundException, IOException {
		Assert.assertEquals("张三", name);
		Assert.assertEquals(Integer.valueOf(23), age);
		List<UploadFile> files=UploadFileHolder.getFiles();
		Assert.assertEquals(1, files.size());
		UploadFile f=files.get(0);
		Assert.assertEquals("logo_bluce.jpg", f.getName());
		byte[] data=IOUtils.toByteArray(f.getInputStream());
		byte[] exp=Files.readAllBytes(new File("logo_bluce.jpg").toPath());
		Assert.assertArrayEquals(exp, data);
		return "姓名:"+name+",年龄:"+age;
	}

	//本接口要等陆后才能用
	@Web(value = "plain_sign", sign = true,requireLogin=true)
	public String plain_sign(String name) {
		return "hello " + name+"，来自"+HttpSessionHolder.getUserObject(DemoSessionObject.class).getUserId()+"的问候";
	}

	@Web(requestEncrypt = EncryptType.AES_BASE64, responseEncrypt = EncryptType.AES_BASE64)
	public String bizError() {
		System.out.println("req:" + HttpHeadersHolder.getHttpRequest());
		BizException.throwException(12345, "业务异常");
		return "";
	}

}

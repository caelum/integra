/***
 * 
 * Copyright (c) 2009 Caelum - www.caelum.com.br/opensource All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of the
 * copyright holders nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written
 * permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package br.com.caelum.integracao.http;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes a web method.
 * 
 * @author guilherme silveira
 */
public class Method {

	private final Logger logger = LoggerFactory.getLogger(Method.class);

	private final PostMethod post;
	private final HttpClient client;
	private int result;
	
	private List<Part> parts = new ArrayList<Part>();

	private boolean released;

	public Method(HttpClient client, PostMethod post) {
		this.client = client;
		this.post = post;
	}

	public Method with(String key, String value) {
		logger.debug("with " + key + "=" + value);
		parts.add(new StringPart(key, value));
		return this;
	}

	public Method with(String key, File f) throws FileNotFoundException {
		logger.debug("with " + key + "=" + f.getAbsolutePath());
		parts.add(new FilePart(key, f));
		return this;
	}

	public void send() throws IOException {
		post.setRequestEntity(new MultipartRequestEntity(parts.toArray(new Part[0]), post.getParams()));
		this.result = client.executeMethod(post);
	}

	public int getResult() {
		return result;
	}

	public String getContent() throws IOException {
		try {
			return post.getResponseBodyAsString();
		} finally {
			released = true;
			post.releaseConnection();
		}
	}

	/**
	 * Saves the result of the http request to the disk.
	 */
	public void saveContentToDisk(File target) throws IOException {
		try {
			InputStream is = post.getResponseBodyAsStream();
			FileOutputStream fos = new FileOutputStream(target);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			while (true) {
				int b = is.read();
				if (b == -1) {
					break;
				}
				bos.write(b);
			}
			bos.close();
			fos.close();
			is.close();
		} finally {
			released = true;
			post.releaseConnection();
		}
	}

	public void close() {
		if (!released) {
			post.releaseConnection();
		}
	}

	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

}

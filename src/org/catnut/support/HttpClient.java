package org.catnut.support;

/*
 * Copyright (C) 2013 Surviving with Android (http://www.survivingwithandroid.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Scanner;

/**
 * 文件上传
 */
public class HttpClient {
	private String url;
	private HttpURLConnection httpURLConnection;
	private OutputStream outputStream;

	private String delimiter = "--";
	private String boundary = "SwA" + Long.toString(System.currentTimeMillis()) + "SwA";

	public HttpClient(String url) {
		this.url = url;
	}

	public void connectForMultipart(Map<String, String> headers) throws Exception {
		httpURLConnection = (HttpURLConnection) (new URL(url)).openConnection();
		httpURLConnection.setRequestMethod("POST");
		httpURLConnection.setDoInput(true);
		httpURLConnection.setDoOutput(true);
		for (String key : headers.keySet()) {
			httpURLConnection.setRequestProperty(key, headers.get(key));
		}
		httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
		httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
		httpURLConnection.connect();
		outputStream = httpURLConnection.getOutputStream();
	}

	public void addFormPart(String paramName, String value) throws Exception {
		writeParamData(paramName, value);
	}

	public void addFilePart(String paramName, String fileName, byte[] data) throws Exception {
		outputStream.write((delimiter + boundary + "\r\n").getBytes());
		outputStream.write(("Content-Disposition: form-data; name=\"" + paramName + "\"; filename=\"" + fileName + "\"\r\n").getBytes());
		outputStream.write(("Content-Type: application/octet-stream\r\n").getBytes());
		outputStream.write(("Content-Transfer-Encoding: binary\r\n").getBytes());
		outputStream.write("\r\n".getBytes());

		outputStream.write(data);

		outputStream.write("\r\n".getBytes());
	}

	public void finishMultipart() throws Exception {
		outputStream.write((delimiter + boundary + delimiter + "\r\n").getBytes());
	}

	public UploadResponse getResponse() throws Exception {
		int responseCode = httpURLConnection.getResponseCode();
		InputStream is;
		if (responseCode >= 200 && responseCode < 400) {
			is = httpURLConnection.getInputStream();
		} else {
			is = httpURLConnection.getErrorStream();
		}

		Scanner in = new Scanner(is).useDelimiter("\\A");
		String response = in.hasNext() ? in.next() : "error";

		httpURLConnection.disconnect();
		in.close();

		return new UploadResponse(responseCode, response);
	}

	private void writeParamData(String paramName, String value) throws Exception {
		outputStream.write((delimiter + boundary + "\r\n").getBytes());
		outputStream.write("Content-Type: text/plain\r\n".getBytes());
		outputStream.write(("Content-Disposition: form-data; name=\"" + paramName + "\"\r\n").getBytes());
		outputStream.write(("\r\n" + value + "\r\n").getBytes());
	}

	public static class UploadResponse {
		public final int statusCode;
		public final String response;

		public UploadResponse(int statusCode, String response) {
			this.statusCode = statusCode;
			this.response = response;
		}
	}
}

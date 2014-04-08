/*******************************************************************************
 * Copyright 2014 Celartem, Inc., dba LizardTech
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.lizardtech.expresszip.vaadin;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Properties;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.vaadin.Application;
import com.vaadin.ui.Window.Notification;

@SuppressWarnings("serial")
public class ExpressZipApplication extends Application {
	public static final Logger logger = Logger.getLogger(ExpressZipApplication.class);
	public static Properties Configuration = null;
	private static int uniqueID = 0;
	private String applicationID;

	public class UnmatchedHostsAllowedVerifier implements HostnameVerifier {
		@Override
		public boolean verify(String hostname, SSLSession session) { return true; }
	}
	private static TrustManager[] trustAllCerts = new X509TrustManager[] {
		new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() { return null; }
			public void checkClientTrusted(X509Certificate[] certs, String authType) {}
			public void checkServerTrusted(X509Certificate[] certs, String authType) {}
		}
	};

	@Override
	public void init() {
		if (Configuration == null) {
			Configuration = new Properties();
			InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("ExpressZip.properties");
			if (stream != null) {
				try {
					Configuration.load(stream);
				} catch (IOException e) {
					logger.error("Failed to read configuration", e);
				}
			} else {
				String msg = "Could not open configuration file.  This is likely due to a corrupt installation.  Application will run but will not operate as expected.  Please report this to the System Administrator.";
				logger.error(msg);
				getMainWindow().showNotification("Corrupt Installation?", msg, Notification.TYPE_ERROR_MESSAGE);
			}
			Configuration.put("applicationURL", getURL().toString());
		}
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			keyStore.load(null);
			String sslstrict = Configuration.getProperty("sslstrictness");
			if (sslstrict != null && sslstrict.equals("strict")) {
				InputStream certBundleStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("ca-bundle.crt");
				if (certBundleStream != null) {
					BufferedInputStream bufferedCertBundle = new BufferedInputStream(certBundleStream);
					CertificateFactory cf = CertificateFactory.getInstance("X.509");
					while (bufferedCertBundle.available() > 0) {
						Certificate cert = cf.generateCertificate(bufferedCertBundle);
						System.out.println("Certificate: " + cert.toString());
						keyStore.setCertificateEntry(Integer.toHexString(cert.hashCode()), cert);
					}
					bufferedCertBundle.close();
					certBundleStream.close();
					TrustManagerFactory trustMgrFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
					trustMgrFactory.init(keyStore);
					sc.init(null, trustMgrFactory.getTrustManagers(), new SecureRandom());
				}
			} else {
				sc.init(null, trustAllCerts, new SecureRandom());
				HttpsURLConnection.setDefaultHostnameVerifier(new UnmatchedHostsAllowedVerifier());
			}
			if (sslstrict != null && (sslstrict.equals("strict") || sslstrict.equals("noverify"))) {
				HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			}
		} catch (Exception e) {
			logger.error("An exception occurred: ", e);
		}
		setMainWindow(new ExpressZipWindow());
		BasicConfigurator.configure();
		initApplicationID();
		logger.debug("ExpressZipApplication Logger enabled.");
	}

	@Override
	public void close() {
		super.close();
		((ExpressZipWindow) getMainWindow()).getSetupMapModel().ShapefileRefCount_DecrementAll();
	}

	/**
	 * Gets a globally unique positive int between 0 and Integer.MAX_VALUE
	 */
	private synchronized void initApplicationID() {
		int id = uniqueID++;
		if (id < 0) {
			id = 0;
		}
		applicationID = Integer.toString(uniqueID);
	}

	public String getApplicationID() {
		return applicationID;
	}

}

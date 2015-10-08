/**
 * This file was taken from Antenna (http://antenna.sourceforge.net) and 
 * heavily modified to suit my needs.
 * 
 * It is not exactly clear what license this file was under as there was
 * no copyright header attached to this file 
 * (src/de/pleumann/antenna/WtkSign.java). The rest of the files in that
 * directory are licensed under the LGPLv2+ so we assume this one as well.
 */
package net.tuxed.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.security.auth.x500.X500Principal;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class Sign extends Task {
	private final static int ISSUER = 0;
	private final static int SUBJECT = 1;

	/**
	 * Key Store (OPTIONAL)
	 * 
	 * Defaults to ".keystore" in the user’s home directory
	 */
	private File keyStoreFile;

	/**
	 * The JAR file to sign (REQUIRED)
	 */
	private File jarFile;

	/**
	 * The JAD file to add the certificates and signatures to (REQUIRED)
	 */
	private File jadFile;

	/**
	 * The password for the Key Store (OPTIONAL)
	 * 
	 * Defaults to empty string
	 */
	private String keyStorePass;

	/**
	 * The password for the key (OPTIONAL)
	 * 
	 * Defaults to empty string
	 */
	private String keyPass;

	/**
	 * The alias for the key entry (REQUIRED)
	 */
	private String keyEntryAlias;

	public Sign() {
		keyStoreFile = new File(System.getProperty("user.home")
				+ File.separator + ".keystore");
		keyPass = "";
		keyStorePass = "";
	}

	public void execute() throws BuildException {
		if (jarFile == null) {
			throw new BuildException("JAR file is not specified");
		}

		if (jadFile == null) {
			throw new BuildException("JAD file is not specified");
		}

		if (keyEntryAlias == null) {
			throw new BuildException("Key entry alias is not specified");
		}

		if (!keyStoreFile.exists())
			throw new BuildException("Key Store does not exist");

		if (!jadFile.exists())
			throw new BuildException("JAD file does not exist");

		if (!jarFile.exists())
			throw new BuildException("JAR file does not exist");

		KeyStore keyStore = null;
		try {
			/* construct a new MANIFEST from the existing JAD file */
			FileInputStream jadFis = new FileInputStream(jadFile);
			Manifest m = new Manifest(jadFis);
			jadFis.close();
			Attributes a = m.getMainAttributes();

			/* add the certificate chain to the MANIFEST */
			keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			FileInputStream kis = new FileInputStream(keyStoreFile);
			keyStore.load(kis, keyStorePass.toCharArray());
			kis.close();
			Certificate[] certs = keyStore.getCertificateChain(keyEntryAlias);
			if (certs == null) {
				throw new BuildException(
						"key entry alias not found in the key store");
			} else {
				log("Certificate Issued By: "
						+ getCertificateCN(certs[0], ISSUER));

				log("Certificate Issued To: "
						+ getCertificateCN(certs[0], SUBJECT));

				for (int i = 0; i < certs.length; i++) {
					a.putValue("MIDlet-Certificate-1" + "-" + (i + 1), Base64
							.encodeBytes(certs[i].getEncoded()));
				}
			}
			/* add the signature to the MANIFEST */
			FileInputStream jarFis = new FileInputStream(jarFile);
			Signature signature = Signature.getInstance("SHA1withRSA");
			PrivateKey key = (PrivateKey) keyStore.getKey(keyEntryAlias,
					keyPass.toCharArray());
			signature.initSign(key);
			byte buffer[] = new byte[4096];
			int length;
			while ((length = jarFis.read(buffer)) != -1) {
				signature.update(buffer, 0, length);
			}
			byte[] sign = signature.sign();
			jarFis.close();
			String sigStr = Base64.encodeBytes(sign);
			a.putValue("MIDlet-Jar-RSA-SHA1", sigStr);

			/*
			 * write the updated JAD file, we need to take a special approach
			 * here as the signature and the certificates need to be added
			 * without any line breaks as specified by section 4.1.5 and 4.1.6
			 * of the MIDP 2.1 specification. Using the write method of the
			 * manifest would result in just that. So we write this ourselves
			 * now and apply it to the whole JAD file at once.
			 */
			log("Writing the JAD file...");
			FileWriter fw = new FileWriter(jadFile);
			for (Entry<Object, Object> e : a.entrySet()) {
				fw.write(e.getKey() + ": " + e.getValue() + "\r\n");
			}
			fw.write("\r\n");
			fw.close();
		} catch (CertificateException e) {
			throw new BuildException(e.getMessage());
		} catch (KeyStoreException e) {
			throw new BuildException(e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			throw new BuildException(e.getMessage());
		} catch (FileNotFoundException e) {
			throw new BuildException(e.getMessage());
		} catch (IOException e) {
			throw new BuildException(e.getMessage());
		} catch (SignatureException e) {
			throw new BuildException(e.getMessage());
		} catch (UnrecoverableKeyException e) {
			throw new BuildException(e.getMessage());
		} catch (InvalidKeyException e) {
			throw new BuildException(e.getMessage());
		}
	}

	/**
	 * Extract the common name (CN) of the certificate from the distinguished
	 * name (DN).
	 * 
	 * @param c
	 *            the certificate
	 * @param entity
	 *            whether to return the CN of the issuer or subject
	 * 
	 * @return the common name (CN) or "Unknown Identity" if the CN field does
	 *         not exist
	 */
	private String getCertificateCN(Certificate c, int entity) {
		X509Certificate x = (X509Certificate) c;
		String dn = null;
		if (entity == ISSUER)
			dn = x.getIssuerX500Principal().getName(X500Principal.RFC1779);
		else if (entity == SUBJECT)
			dn = x.getSubjectX500Principal().getName(X500Principal.RFC1779);
		else
			return "Unknown Entity?";

		String[] dns = dn.split(",");
		for (int i = 0; i < dns.length; i++) {
			if (dns[i].trim().startsWith("CN="))
				return dns[i].trim().substring(3);
		}
		return "Unknown Identity";
	}

	public void setJad(File f) {
		jadFile = f;
	}

	public void setJar(File j) {
		jarFile = j;
	}

	public void setKeyEntryAlias(String alias) {
		keyEntryAlias = alias;
	}

	public void setKeyPass(String keypass) {
		keyPass = keypass;
	}

	public void setKeyStore(File keyStore) {
		keyStoreFile = keyStore;
	}

	public void setKeyStorePass(String storePass) {
		keyStorePass = storePass;
	}
}
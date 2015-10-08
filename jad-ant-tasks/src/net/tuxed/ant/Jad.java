package net.tuxed.ant;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Simple Ant Task for creating an Application Descriptor (JAD file) for Java ME
 * MIDlet suites.
 * 
 * @author F. Kooman <F.Kooman@student.science.ru.nl>
 * 
 */
public class Jad extends Task {
	private String jarFileName;
	private String jadFileName;

	public void execute() throws BuildException {
		if (jarFileName == null)
			throw new BuildException("no JAR file specified");
		if (jadFileName == null)
			throw new BuildException("no JAD file specified");

		/* read required information from JAR file MANIFEST */
		File jarFile = new File(jarFileName);
		String jarFileSize = Long.toString(jarFile.length());
		Manifest jarManifest = null;
		try {
			JarFile jf = new JarFile(jarFileName);
			jarManifest = jf.getManifest();
		} catch (IOException e) {
			throw new BuildException(
					"error while extracting MANIFEST from JAR file");
		}

		if (jarManifest == null)
			throw new BuildException(
					"unable to retrieve MANIFEST from JAR file");

		/* add additional attributes to the MANIFEST */
		Attributes a = jarManifest.getMainAttributes();
		a.putValue("MIDlet-Jar-Size", jarFileSize);
		a.putValue("MIDlet-Jar-URL", jarFile.getName());

		/*
		 * Write the JAD file.
		 * 
		 * We write the JAD file in a special way, different from the normal
		 * MANIFEST files as it seems that some phones cannot handle wrapped
		 * lines, so we write them without wrapping lines. Besides, when we add
		 * certificates and signatures we can't wrap as stated in sections 4.1.5
		 * and 4.1.6 of the MIDP 2.1 specification.
		 */
		log("Writing the JAD file...");
		try {
			FileWriter fw = new FileWriter(jadFileName);
			for (Entry<Object, Object> e : a.entrySet()) {
				fw.write(e.getKey() + ": " + e.getValue() + "\r\n");
			}
			fw.write("\r\n");
			fw.close();
		} catch (IOException e1) {
			throw new BuildException("error while writing JAD file");
		}
	}

	public void setJad(String jadName) {
		this.jadFileName = jadName;
	}

	public void setJar(String jarName) {
		this.jarFileName = jarName;
	}
}
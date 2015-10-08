package com.nokia.nfc.p2p;

import java.io.IOException;

public interface NFCIPConnection {

	public static final int MAX_LEN = 65524;
	public static final int MODE_INITIATOR = 2;
	public static final int MODE_TARGET = 1;

	public int getMode();

	public byte[] getUID();

	public byte[] receive() throws java.io.IOException,
			javax.microedition.contactless.ContactlessException;

	public void send(byte[] data) throws java.io.IOException,
			javax.microedition.contactless.ContactlessException;

	public void close() throws IOException;
}

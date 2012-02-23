package com.allrightsms.shared.Cripto;

import java.util.Arrays;

public class AES
{
	private static String ALGORITHM = "AES";

	private AESCrypt cipherEncrypt = null;
	private AESCrypt cipherDecrypt = null;

	public AES()
	{
		cipherEncrypt = new AESCrypt();
		cipherDecrypt = new AESCrypt();
	}
	
	public AES(String key) throws Exception
	{
		cipherEncrypt = new AESCrypt();
		cipherDecrypt = new AESCrypt();
		this.setKey(key.substring(0, 5));
	}

	public void setKey( String key ) throws Exception
	{
		boolean ok = false;
		for( int i=0; i<cipherEncrypt.AES_KEYSIZES.length; i++ )
		{
			if( key.length() == cipherEncrypt.AES_KEYSIZES[i] )
				break;
			if( key.length() > cipherEncrypt.AES_KEYSIZES[i] )
				continue;

			//int pad = cipherEncrypt.AES_KEYSIZES[i] - key.length();

			//key = padRight( key, pad );
			//Window.alert( "pad: " + pad );
			// pad it to take up to n character...
			key = paddingString( key, cipherEncrypt.AES_KEYSIZES[i], ' ', false );
			ok = true;
			break;
		}

		if( !ok )
		{
			throw new Exception( "Key too big!" );
		}

		cipherEncrypt.init( false, ALGORITHM, key.getBytes() );
		cipherDecrypt.init( true, ALGORITHM, key.getBytes() );
	}

	/**
	* Pads a String <code>s</code> to take up <code>n</code>
	* characters, padding with char <code>c</code> on the
	* left (<code>true</code>) or on the right (<code>false</code>).
	* Returns <code>null</code> if passed a <code>null</code>
	* String.
	**/
	public static String paddingString(String s, int n, char c, boolean paddingLeft) {
		if (s == null) {
			return s;
		}
		int add = n - s.length(); // may overflow int size... should not be a problem in real life
		if(add <= 0){
			return s;
		}
		StringBuffer str = new StringBuffer(s);
		char[] ch = new char[add];
		Arrays.fill(ch, c);
		if(paddingLeft){
			str.insert(0, ch);
		} else {
			str.append(ch);
		}
		return str.toString();
	}

	/*
	public static String padRight( String s, int n ) {
		return String.format("%1$-" + n + "s", s);  
	}
	*/

	public byte[] encrypt( byte[] data )
	{
		final int blockSize = cipherEncrypt.getBlockSize();
		// data.length rounded + 1 block - size % blockSize = 0
		final int size = data.length + ( (data.length % blockSize != 0) ? (blockSize - (data.length % blockSize)) : 0 );

		byte[] in = new byte[ size ];
		byte[] out = new byte[ size ];

		int n = data.length;
		while( n-- > 0 )
		{
			in[n] = data[n];
		}
		Arrays.fill( in, data.length, in.length, (byte)0 );
		Arrays.fill( out, (byte)0 );

		int nbBlock = size / blockSize;
		int inOffset = 0;
		int outOffset = 0;

		while( nbBlock > 0 )
		{
			cipherEncrypt.encryptBlock( in, inOffset, out, outOffset );
			inOffset += blockSize;
			outOffset += blockSize;
			nbBlock--;
		}

		return out;
	}

	public byte[] decrypt( byte[] data )
	{
		final int blockSize = cipherDecrypt.getBlockSize();
		// data.length rounded + 1 block - size % blockSize = 0
		final int size = data.length + ( (data.length % blockSize != 0) ? (blockSize - (data.length % blockSize)) : 0 );

		byte[] in = new byte[ size ];
		byte[] out = new byte[ size ];

		int n = data.length;
		while( n-- > 0 )
		{
			in[n] = data[n];
		}
		Arrays.fill( in, data.length, in.length, (byte)0 );
		Arrays.fill( out, (byte)0 );

		int nbBlock = size / blockSize;
		int inOffset = 0;
		int outOffset = 0;

		while( nbBlock > 0 )
		{
			cipherDecrypt.decryptBlock( in, inOffset, out, outOffset );
			inOffset += blockSize;
			outOffset += blockSize;
			nbBlock--;
		}

		return out;
	}

	public String encryptBase64( String data )
	{
		return Base64.byteArrayToBase64( encrypt( data.getBytes() ) );
	}

	public String decryptBase64( String data )
	{
		return new String( decrypt( Base64.base64ToByteArray(data) ) );
	}
};


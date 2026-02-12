package sv.mh.fe.models;

import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonProperty;
import sv.mh.fe.constantes.TipoLlave;

public class Llave {
	
	private TipoLlave keyType;
	
	private String algorithm;
	
	@JsonProperty("encoded")
	private byte[]	encoded;
	
	private String format;
	
	private String clave;

	public TipoLlave getKeyType() {
		return keyType;
	}
	public void setKeyType(TipoLlave keyType) {
		this.keyType = keyType;
	}

	public String getAlgorithm() {
		return algorithm;
	}
	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public byte[] getEncoded() {
		return encoded;
	}

	public void setEncoded(byte[] encoded) {
		this.encoded = encoded;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getClave() {
		return clave;
	}
	public void setClave(String clave) {
		this.clave = clave;
	}
	
	@Override
	public String toString() {
		return "Key [keyType=" + keyType + ", algorithm=" + algorithm + ", encoded=" + Arrays.toString(encoded)
				+ ", format=" + format + "]";
	}
	
}

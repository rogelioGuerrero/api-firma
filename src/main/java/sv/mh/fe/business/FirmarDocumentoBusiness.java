package sv.mh.fe.business;

import java.nio.file.Path;
import java.security.PrivateKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sv.mh.fe.models.CertificadoMH;
import sv.mh.fe.security.KeyGenerator;
import sv.mh.fe.utils.FileUtils;

@Service
public class FirmarDocumentoBusiness {
	
	final static Logger logger = LoggerFactory.getLogger(FirmarDocumentoBusiness.class);
	
	@Autowired
	private FileUtils fileUtils;
	
	@Autowired
	private KeyGenerator keyGenerator;
		
	/**
	 * Método para crear un JSON Web Signing (JWS).
	 * @param certificado
	 * @param ruta
	 * @throws Exception
	 */
	public void firmarJSON(CertificadoMH certificado, Path ruta) throws Exception {		
		String contenido = fileUtils.LeerArchivo(ruta);		
		JsonWebSignature jws = new JsonWebSignature();		
		jws.setPayload(contenido);	
		jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA512);				
		PrivateKey key =  keyGenerator.ByteToPrivateKey(certificado.getPrivateKey().getEncodied());		
		jws.setKey(key);
		fileUtils.crearArchivo(ruta.toString(), jws.getCompactSerialization());
	}	
	
	/**
	 * Método para crear un JSON Web Signing (JWS).
	 * @param certificado
	 * @param contenido, DTE que se quiere firmar
	 * @throws Exception
	 */
	public String firmarJSON(CertificadoMH certificado, String contenido) throws Exception {
		JsonWebSignature jws = new JsonWebSignature();		
		jws.setPayload(contenido);	
		jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA512);	
		PrivateKey key =  keyGenerator.ByteToPrivateKey(certificado.getPrivateKey().getEncodied());		
		jws.setKey(key);
		return jws.getCompactSerialization();
	}			
	
	/**
	 * Método para crear un JSON Web Signing (JWS) a partir de un certificado en Base64.
	 * @param certificadoB64
	 * @param password
	 * @param contenido, DTE que se quiere firmar
	 * @throws Exception
	 */
	public String firmarJSONBase64(String certificadoB64, String password, String contenido) throws Exception {
		byte[] certBytes = java.util.Base64.getDecoder().decode(certificadoB64);
		
		try {
			// Intentar primero como PKCS12
			java.security.KeyStore keyStore = java.security.KeyStore.getInstance("PKCS12");
			keyStore.load(new java.io.ByteArrayInputStream(certBytes), password.toCharArray());
			
			String alias = null;
			java.util.Enumeration<String> aliases = keyStore.aliases();
			if (aliases.hasMoreElements()) {
				alias = aliases.nextElement();
			}
			
			if (alias == null) {
				throw new Exception("No se encontró un alias en el certificado");
			}
			
			PrivateKey key = (PrivateKey) keyStore.getKey(alias, password.toCharArray());
			
			JsonWebSignature jws = new JsonWebSignature();		
			jws.setPayload(contenido);	
			jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA512);	
			jws.setKey(key);
			return jws.getCompactSerialization();
		} catch (Exception e) {
			// Si falla como PKCS12 (ej. "toDerInputStream rejects tag type 60"), 
			// asumimos que es el XML del Ministerio de Hacienda codificado en Base64
			try {
				String xmlContent = new String(certBytes);
				com.fasterxml.jackson.dataformat.xml.XmlMapper xmlMapper = new com.fasterxml.jackson.dataformat.xml.XmlMapper();
				com.fasterxml.jackson.datatype.jsr310.JavaTimeModule module = new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule();
				xmlMapper.registerModule(module);
				
				CertificadoMH cert = xmlMapper.readValue(xmlContent, CertificadoMH.class);
				return firmarJSON(cert, contenido);
			} catch (Exception xmlEx) {
				throw new Exception("El certificadoB64 no es un PKCS12 válido ni un XML de CertificadoMH válido. Error original: " + e.getMessage());
			}
		}
	}
}
